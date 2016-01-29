package org.deeplearning4j.nn.conf.graph;


import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.preprocessor.*;
import org.deeplearning4j.nn.graph.ComputationGraph;

/** PreprocessorVertex is a simple adaptor class that allows a {@link InputPreProcessor} to be used in a ComputationGraph
 * GraphVertex, without it being associated with a layer.
 * @author Alex Black
 */
public class PreprocessorVertex extends GraphVertex {

    private InputPreProcessor preProcessor;
    private InputType outputType;

    public PreprocessorVertex(InputPreProcessor preProcessor) {
        this(preProcessor, null);
    }

    public PreprocessorVertex(InputPreProcessor preProcessor, InputType outputType) {
        this.preProcessor = preProcessor;
        this.outputType = outputType;
    }

    @Override
    public GraphVertex clone() {
        return new PreprocessorVertex(preProcessor.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PreprocessorVertex)) return false;
        return ((PreprocessorVertex) o).preProcessor.equals(preProcessor);
    }

    @Override
    public int hashCode() {
        return preProcessor.hashCode();
    }

    @Override
    public org.deeplearning4j.nn.graph.vertex.GraphVertex instantiate(ComputationGraph graph, String name, int idx) {
        return new org.deeplearning4j.nn.graph.vertex.impl.PreprocessorVertex(graph, name, idx, preProcessor);
    }

    @Override
    public InputType getOutputType(InputType... vertexInputs) throws InvalidInputTypeException {
        if (vertexInputs.length != 1) throw new InvalidInputTypeException("Invalid input: Preprocessor vertex expects "
                + "exactly one input");
        if (outputType != null) return outputType;   //Allows user to override for custom preprocessors

        //Otherwise, try to infer:
        switch (vertexInputs[0].getType()) {
            case FF:
                if (preProcessor instanceof FeedForwardToCnnPreProcessor) {
                    FeedForwardToCnnPreProcessor ffcnn = (FeedForwardToCnnPreProcessor) preProcessor;
                    return InputType.convolutional(ffcnn.getNumChannels(), ffcnn.getInputWidth(), ffcnn.getInputHeight());
                } else if (preProcessor instanceof FeedForwardToRnnPreProcessor) {
                    return InputType.recurrent();
                } else {
                    //Assume preprocessor doesn't change the type of activations
                    return InputType.feedForward();
                }
            case RNN:
                if (preProcessor instanceof RnnToCnnPreProcessor) {
                    RnnToCnnPreProcessor ffcnn = (RnnToCnnPreProcessor) preProcessor;
                    return InputType.convolutional(ffcnn.getNumChannels(), ffcnn.getInputWidth(), ffcnn.getInputHeight());
                } else if (preProcessor instanceof RnnToFeedForwardPreProcessor) {
                    return InputType.feedForward();
                } else {
                    //Assume preprocessor doesn't change the type of activations
                    return InputType.recurrent();
                }
            case CNN:
                if (preProcessor instanceof CnnToFeedForwardPreProcessor) {
                    return InputType.feedForward();
                } else if (preProcessor instanceof CnnToRnnPreProcessor) {
                    return InputType.recurrent();
                } else {
                    //Assume preprocessor doesn't change the type of activations
                    return vertexInputs[0];
                }
            default:
                throw new RuntimeException("Unknown InputType: " + vertexInputs[0]);
        }

    }
}
