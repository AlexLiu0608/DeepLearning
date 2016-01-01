/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.spark.impl.multilayer;

import org.apache.spark.Accumulator;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.spark.impl.common.BestScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Iterative reduce with
 * flat map using map partitions
 *
 * @author Adam Gibson
 */
public class IterativeReduceFlatMap implements FlatMapFunction<Iterator<DataSet>,Tuple3<INDArray,Updater,Double>> {

    private String json;
    private Broadcast<INDArray> params;
    private Broadcast<Updater> updater;
    private static Logger log = LoggerFactory.getLogger(IterativeReduceFlatMap.class);

    private final Accumulator<Double> best_score_acc;

    /**
     * Pass in json configuration and baseline parameters
     * @param json json configuration for the network
     * @param params the parameters to use for the network
     * @param bestScoreAcc accumulator which tracks best score seen
     */
    public IterativeReduceFlatMap(String json, Broadcast<INDArray> params, Broadcast<Updater> updater,
                                  Accumulator<Double> bestScoreAcc) {
        this.json = json;
        this.params = params;
        this.updater = updater;
        if(updater.getValue() == null)
            throw new IllegalArgumentException("Updater shouldn't be null");
        this.best_score_acc = bestScoreAcc;
    }



    @Override
    public Iterable<Tuple3<INDArray,Updater,Double>> call(Iterator<DataSet> dataSetIterator) throws Exception {
        if(!dataSetIterator.hasNext()) {
            return Collections.singletonList(new Tuple3<INDArray,Updater,Double>(Nd4j.zeros(params.value().shape()),null,0.0));
        }
        List<DataSet> collect = new ArrayList<>();
        while(dataSetIterator.hasNext()) {
            collect.add(dataSetIterator.next());
        }

        DataSet data = DataSet.merge(collect,false);
        if(log.isDebugEnabled()) {
            log.debug("Training on {} examples with data {}",data.numExamples(), data.labelCounts());
        }
        MultiLayerNetwork network = new MultiLayerNetwork(MultiLayerConfiguration.fromJson(json));
        network.init();
        network.setListeners(new ScoreIterationListener(1), new BestScoreIterationListener(best_score_acc));
        INDArray val = params.value();
        Updater upd = updater.getValue();
        if(val.length() != network.numParams(false))
            throw new IllegalStateException("Network did not have same number of parameters as the broadcasted set parameters");
        network.setParameters(val);
        network.setUpdater(upd);
        network.fit(data);

        return Collections.singletonList(new Tuple3<>(network.params(false),network.getUpdater(),network.score()));

    }
}
