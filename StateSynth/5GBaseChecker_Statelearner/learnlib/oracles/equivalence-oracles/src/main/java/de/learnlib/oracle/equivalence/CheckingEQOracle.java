package de.learnlib.oracle.equivalence;

import com.google.common.collect.Streams;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.words.Word;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class CheckingEQOracle <A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    public List<Word<I>> wordCEs;

    public Iterator<Word<I>> CEiterator;

    public CheckingEQOracle(MembershipOracle<I, D> sulOracle, int batchSize, List<Word<I>> wordCEs) {
        super(sulOracle, batchSize);
        this.wordCEs = wordCEs;
        this.CEiterator = wordCEs.iterator();
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        System.out.println("CheckingEQOracle generatesTestWords!");
        return Streams.stream(CEiterator);
//        return Streams.stream(new WpMethodTestsIterator<>(hypothesis,
//                inputs,
//                Math.max(lookahead, expectedSize - hypothesis.size())));
    }
}
