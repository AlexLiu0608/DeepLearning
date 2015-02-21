/*
 * Copyright 2015 Skymind,Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.deeplearning4j.optimize.distance;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.distancefunction.BaseDistanceFunction;
import org.nd4j.linalg.distancefunction.CosineSimilarity;

public class CosineDistance extends BaseDistanceFunction {

	private static final long	serialVersionUID	= 693813798951786016L;
	
	private CosineSimilarity similarity;
	
	public CosineDistance(INDArray base) {
		super(base);
		similarity = new CosineSimilarity(base);
	}

	@Override
	public Float apply(INDArray input) {
    return 1 - similarity.apply(input);
	}

}
