package org.deeplearning4j.linalg.ops.transforms;

import org.deeplearning4j.linalg.api.complex.IComplexNumber;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.linalg.ops.BaseElementWiseOp;

/**
 * Sigmoid operation
 * @author Adam Gibson
 */
public class Sigmoid extends BaseElementWiseOp {
    /**
     * The transformation for a given value (a scalar ndarray)
     *
     * @param input the value to applyTransformToOrigin (a scalar ndarray)
     * @param i     the index of the element being acted upon
     * @return the transformed value based on the input
     */
    @Override
    public Object apply(INDArray from,Object input, int i) {
        if (input instanceof IComplexNumber) {
            IComplexNumber number = (IComplexNumber) input;
            float arg = number.complexArgument().floatValue();
            float sigArg = 1 / 1 + ((float) Math.exp(-arg)) - 1 + .5f;
            float ret = (float) Math.exp(sigArg);
            return NDArrays.createDouble(ret, 0);

        } else {
            float inputf = (float) input;
            float val = 1 / (1 + (float) Math.exp(-inputf));
            return val;
        }
    }
}
