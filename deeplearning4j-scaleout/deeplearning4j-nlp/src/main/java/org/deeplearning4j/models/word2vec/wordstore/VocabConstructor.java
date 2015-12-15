package org.deeplearning4j.models.word2vec.wordstore;

import lombok.Data;
import lombok.NonNull;
import org.deeplearning4j.models.abstractvectors.interfaces.SequenceIterator;
import org.deeplearning4j.models.abstractvectors.sequence.Sequence;
import org.deeplearning4j.models.abstractvectors.sequence.SequenceElement;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.word2vec.Huffman;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.interoperability.SentenceIteratorConverter;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * This class can be used to build joint vocabulary from special sources, that should be treated separately.
 * I.e. words from one source should have minWordFrequency set to 1, while the rest of corpus should have minWordFrequency set to 5.
 * So, here's the way to deal with it.
 *
 * It also can be used to simply build vocabulary out of arbitrary number of Sequences derived from arbitrary number of SequenceIterators
 *
 * @author raver119@gmail.com
 */
public class VocabConstructor<T extends SequenceElement> {
    private List<VocabSource<T>> sources = new ArrayList<>();
    private VocabCache<T> cache;
    private List<String> stopWords;
    private boolean useAdaGrad = false;
    private boolean fetchLabels = false;

    protected static final Logger log = LoggerFactory.getLogger(VocabConstructor.class);

    private VocabConstructor() {

    }

    /**
     * Placeholder for future implementation
     * @return
     */
    protected WeightLookupTable<T> buildExtendedLookupTable() {
        return null;
    }

    /**
     * Placeholder for future implementation
     * @return
     */
    protected VocabCache<T> buildExtendedVocabulary() {
        return null;
    }

    /**
     * This method scans all sources passed through builder, and returns all words as vocab.
     * If TargetVocabCache was set during instance creation, it'll be filled too.
     *
     *
     * @return
     */
    public VocabCache<T> buildJointVocabulary(boolean resetCounters, boolean buildHuffmanTree) {
        if (resetCounters && buildHuffmanTree) throw new IllegalStateException("You can't reset counters and build Huffman tree at the same time!");

        if (cache == null) throw new IllegalStateException("Cache is null, building fresh one");
        if (cache == null) cache = new AbstractCache.Builder<T>().build();
        log.debug("Target vocab size before building: [" + cache.numWords() + "]");
        final AtomicLong sequenceCounter = new AtomicLong(0);
        final AtomicLong elementsCounter = new AtomicLong(0);

        AbstractCache<T> topHolder = new AbstractCache.Builder<T>()
                .minElementFrequency(0)
                .build();

        int cnt = 0;
        for(VocabSource<T> source: sources) {
            SequenceIterator<T> iterator = source.getIterator();
            iterator.reset();

            log.debug("Trying source iterator: ["+ cnt+"]");
            log.debug("Target vocab size before building: [" + cache.numWords() + "]");
            cnt++;

            AbstractCache<T> tempHolder = new AbstractCache.Builder<T>().build();

            int sequences = 0;
            long counter = 0;
            while (iterator.hasMoreSequences()) {
                Sequence<T> document = iterator.nextSequence();
                sequenceCounter.incrementAndGet();
              //  log.info("Sequence length: ["+ document.getElements().size()+"]");
             //   Tokenizer tokenizer = tokenizerFactory.create(document.getContent());




                if (fetchLabels) {
                    T labelWord = document.getSequenceLabel();
                    labelWord.setSpecial(true);
                    labelWord.setElementFrequency(1);

                    tempHolder.addToken(labelWord);
                }

                List<String> tokens = document.asLabels();
                for (String token: tokens) {
                    if (stopWords !=null && stopWords.contains(token)) continue;
                    if (token == null || token.isEmpty()) continue;

                    if (!tempHolder.containsWord(token)) {
                        tempHolder.addToken(document.getElementByLabel(token));
                        elementsCounter.incrementAndGet();
                        counter++;
                        // TODO: this line should be uncommented only after AdaGrad is fixed, so the size of AdaGrad array is known
                        /*
                        if (useAdaGrad) {
                            VocabularyWord word = tempHolder.getVocabularyWordByString(token);

                            word.setHistoricalGradient(new double[layerSize]);
                        }
                        */
                    } else {
                        counter++;
                        tempHolder.incrementWordCount(token);
                    }
                }

                sequences++;
                if (sequenceCounter.get() % 100000 == 0) log.info("Sequences checked: [" + sequenceCounter.get() +"], Current vocabulary size: [" + elementsCounter.get() +"]");
            }
            // apply minWordFrequency set for this source
            log.debug("Vocab size before truncation: [" + tempHolder.numWords() + "],  NumWords: [" + tempHolder.totalWordOccurrences()+ "], sequences parsed: [" + sequences+ "], counter: ["+counter+"]");
            if (source.getMinWordFrequency() > 0) {
                LinkedBlockingQueue<String> labelsToRemove = new LinkedBlockingQueue<>();
                for (T element : tempHolder.vocabWords()) {
                    if (element.getElementFrequency() < source.getMinWordFrequency() && !element.isSpecial())
                        labelsToRemove.add(element.getLabel());
                }

                for (String label: labelsToRemove) {
                    log.debug("Removing label: '" + label + "'");
                    tempHolder.removeElement(label);
                }
            }

            log.debug("Vocab size after truncation: [" + tempHolder.numWords() + "],  NumWords: [" + tempHolder.totalWordOccurrences()+ "], sequences parsed: [" + sequences+ "], counter: ["+counter+"]");

            // at this moment we're ready to transfer
            topHolder.importVocabulary(tempHolder);
            log.debug("Top holder size: ["+ topHolder.numWords()+"]");
            log.debug("Target vocab size before building: [" + cache.numWords() + "]");
        }

        // at this moment, we have vocabulary full of words, and we have to reset counters before transfer everything back to VocabCache

            //topHolder.resetWordCounters();



        cache.importVocabulary(topHolder);

        if (resetCounters) {
            for (T element: cache.vocabWords()) {
                element.setElementFrequency(0);
            }
            cache.updateWordsOccurencies();
        }

        if (buildHuffmanTree) {
            Huffman huffman = new Huffman(cache.vocabWords());
            huffman.build();
            huffman.applyIndexes(cache);
            //topHolder.updateHuffmanCodes();
        }

        log.info("Sequences checked: [" + sequenceCounter.get() +"], Current vocabulary size: [" + cache.numWords() +"]");
        return cache;
    }

    public static class Builder<T extends SequenceElement> {
        private List<VocabSource<T>> sources = new ArrayList<>();
        private VocabCache<T> cache;
        private List<String> stopWords = new ArrayList<>();
        private boolean useAdaGrad = false;
        private boolean fetchLabels = false;

        public Builder() {

        }

        /**
         * Defines, if adaptive gradients should be created during vocabulary mastering
         *
         * @param useAdaGrad
         * @return
         */
        protected Builder<T> useAdaGrad(boolean useAdaGrad) {
            this.useAdaGrad = useAdaGrad;
            return this;
        }

        /**
         * After temporary internal vocabulary is built, it will be transferred to target VocabCache you pass here
         *
         * @param cache target VocabCache
         * @return
         */
        public Builder<T> setTargetVocabCache(@NonNull VocabCache<T> cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Adds SequenceIterator for vocabulary construction.
         * Please note, you can add as many sources, as you wish.
         *
         * @param iterator SequenceIterator to build vocabulary from
         * @param minElementFrequency elements with frequency below this value will be removed from vocabulary
         * @return
         */
        public Builder<T> addSource(@NonNull SequenceIterator<T> iterator, int minElementFrequency) {
            sources.add(new VocabSource<T>(iterator, minElementFrequency));
            return this;
        }
/*
        public Builder<T> addSource(LabelAwareIterator iterator, int minWordFrequency) {
            sources.add(new VocabSource(iterator, minWordFrequency));
            return this;
        }

        public Builder<T> addSource(SentenceIterator iterator, int minWordFrequency) {
            sources.add(new VocabSource(new SentenceIteratorConverter(iterator), minWordFrequency));
            return this;
        }
        */
/*
        public Builder setTokenizerFactory(@NonNull TokenizerFactory factory) {
            this.tokenizerFactory = factory;
            return this;
        }
*/
        public Builder<T> setStopWords(@NonNull List<String> stopWords) {
            this.stopWords = stopWords;
            return this;
        }

        /**
         * Sets, if labels should be fetched, during vocab building
         *
         * @param reallyFetch
         * @return
         */
        public Builder<T> fetchLabels(boolean reallyFetch) {
            this.fetchLabels = reallyFetch;
            return this;
        }

        public VocabConstructor<T> build() {
            VocabConstructor<T> constructor = new VocabConstructor<T>();
            constructor.sources = this.sources;
            constructor.cache = this.cache;
            constructor.stopWords = this.stopWords;
            constructor.useAdaGrad = this.useAdaGrad;
            constructor.fetchLabels = this.fetchLabels;

            return constructor;
        }
    }

    @Data
    private static class VocabSource<T extends SequenceElement> {
        @NonNull private SequenceIterator<T> iterator;
        @NonNull private int minWordFrequency;
    }
}
