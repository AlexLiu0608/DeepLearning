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

package org.deeplearning4j.spark.models.embeddings.word2vec;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.scaleout.perform.models.word2vec.Word2VecPerformer;
import org.deeplearning4j.spark.text.BaseSparkTest;
import org.deeplearning4j.spark.text.TextPipeline;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by agibsonccc on 1/31/15.
 */
public class Word2VecTest extends BaseSparkTest {





    @Test
    public void testConcepts() throws Exception {
        JavaRDD<String> corpus = sc.textFile(new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath()).map(new Function<String, String>() {
            @Override
            public String call(String s) throws Exception {
                return s.toLowerCase();
            }
        }).cache();

        Word2Vec word2Vec = new Word2Vec();
        sc.getConf().set(Word2VecVariables.NEGATIVE, String.valueOf(0));
        Pair<VocabCache,WeightLookupTable> table = word2Vec.train(corpus);
        WordVectors vectors = WordVectorSerializer.fromPair(new Pair<>((InMemoryLookupTable) table.getSecond(), table.getFirst()));
        Collection<String> words = vectors.wordsNearest("day", 10);
        System.out.println(Arrays.toString(words.toArray()));
//        assertTrue(words.contains("week"));
    }


    /**
     *
     * @return
     */
    @Override
    public JavaSparkContext getContext() {
        if(sc != null) {
            return sc;
        }
        // set to test mode
        SparkConf sparkConf = new SparkConf().set(org.deeplearning4j.spark.models.embeddings.word2vec.Word2VecVariables.NUM_WORDS,"5")
                .set(Word2VecPerformerVoid.ITERATIONS,"5")
                .setMaster("local[8]").set(Word2VecPerformer.NEGATIVE, String.valueOf(0)).set(TextPipeline.MIN_WORDS,String.valueOf("1"))
                .setAppName("sparktest");


        sc = new JavaSparkContext(sparkConf);
        return sc;

    }


}