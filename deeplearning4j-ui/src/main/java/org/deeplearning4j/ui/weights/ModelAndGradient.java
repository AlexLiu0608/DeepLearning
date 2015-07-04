package org.deeplearning4j.ui.weights;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.deeplearning4j.nn.api.Model;
import org.nd4j.linalg.api.ndarray.INDArray;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gibson
 */

public class ModelAndGradient implements Serializable {
    private Map<String,INDArray> parameters;
    private Map<String,INDArray> gradients;
    private double score;

    public ModelAndGradient() {
        parameters = new HashMap<>();
        gradients = new HashMap<>();
    }

    public ModelAndGradient(Model model) {
        this.gradients = model.gradient().gradientForVariable();
        this.parameters = model.paramTable();
        this.score = model.score();
    }




    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }


    public Map<String, INDArray> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, INDArray> parameters) {
        this.parameters = parameters;
    }


    public Map<String, INDArray> getGradients() {
        return gradients;
    }

    public void setGradients(Map<String, INDArray> gradients) {
        this.gradients = gradients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelAndGradient that = (ModelAndGradient) o;

        if (Double.compare(that.score, score) != 0) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        return !(gradients != null ? !gradients.equals(that.gradients) : that.gradients != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = parameters != null ? parameters.hashCode() : 0;
        result = 31 * result + (gradients != null ? gradients.hashCode() : 0);
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
