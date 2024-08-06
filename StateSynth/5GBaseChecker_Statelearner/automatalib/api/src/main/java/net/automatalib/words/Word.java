/* Copyright (C) 2013-2022 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.words;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.automatalib.AutomataLibProperty;
import net.automatalib.AutomataLibSettings;
import net.automatalib.commons.smartcollections.ArrayWritable;
import net.automatalib.commons.smartcollections.IntSeq;
import net.automatalib.commons.util.array.AWUtil;
import net.automatalib.commons.util.strings.AbstractPrintable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A word is an ordered sequence of symbols. {@link Word}s are generally immutable, i.e., a single {@link Word} object
 * will never change (unless symbol objects are modified, which is however highly discouraged).
 * <p>
 * This class provides the following static methods for creating words in the most common scenarios: <ul> <li> {@link
 * #epsilon()} returns the empty word of length 0 <li> {@link #fromLetter(Object)} turns a single letter into a word of
 * length 1 <li> {@link #fromSymbols(Object...)} creates a word from an array of symbols <li> {@link
 * #fromArray(Object[], int, int)} creates a word from a subrange of a symbols array <li> {@link #fromList(List)}
 * creates a word from a {@link List} of symbols </ul>
 * <p>
 * Modification operations like {@link #append(Object)} or {@link #concat(Word...)} create new objects, subsequently
 * invoking these operations on the respective objects returned is therefore highly inefficient. If words need to be
 * dynamically created, a {@link WordBuilder} should be used.
 * <p>
 * This is an abstract base class for word representations. Implementing classes only need to implement <ul> <li> {@link
 * #getSymbol(int)} <li> {@link #length()} </ul>
 * <p>
 * However, for the sake of efficiency it is highly encouraged to overwrite the other methods as well, providing
 * specialized realizations.
 *
 * @param <I>
 *         symbol type
 *
 * @author Malte Isberner
 */
public abstract class Word<I> extends AbstractPrintable implements ArrayWritable<I>, Iterable<I> {

    private static final String EMPTY_WORD_REP;
    private static final String WORD_DELIM_LEFT;
    private static final String WORD_DELIM_RIGHT;
    private static final String WORD_SYMBOL_SEPARATOR;
    private static final String WORD_SYMBOL_DELIM_LEFT;
    private static final String WORD_SYMBOL_DELIM_RIGHT;

    static {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();
        EMPTY_WORD_REP = settings.getProperty(AutomataLibProperty.WORD_EMPTY_REP, "ε");
        WORD_DELIM_LEFT = settings.getProperty(AutomataLibProperty.WORD_DELIM_LEFT, "");
        WORD_DELIM_RIGHT = settings.getProperty(AutomataLibProperty.WORD_DELIM_RIGHT, "");
        WORD_SYMBOL_SEPARATOR = settings.getProperty(AutomataLibProperty.WORD_SYMBOL_SEPARATOR, " ");
        WORD_SYMBOL_DELIM_LEFT = settings.getProperty(AutomataLibProperty.WORD_SYMBOL_DELIM_LEFT, "");
        WORD_SYMBOL_DELIM_RIGHT = settings.getProperty(AutomataLibProperty.WORD_SYMBOL_DELIM_RIGHT, "");
    }

    public static <I> Comparator<Word<I>> canonicalComparator(Comparator<? super I> symComparator) {
        return new CanonicalWordComparator<>(symComparator);
    }

    /**
     * Creates a word from an array of symbols.
     *
     * @param symbols
     *         the symbol array
     *
     * @return a word containing the symbols in the specified array
     */
    @SafeVarargs
    public static <I> Word<I> fromSymbols(I... symbols) {
        if (symbols.length == 0) {
            return epsilon();
        }
        if (symbols.length == 1) {
            return fromLetter(symbols[0]);
        }
        return new SharedWord<>(symbols.clone());
    }

    /**
     * Retrieves the empty word.
     *
     * @return the empty word.
     *
     * @see Collections#emptyList()
     */
    @SuppressWarnings("unchecked")
    public static <I> Word<I> epsilon() {
        return (Word<I>) EmptyWord.INSTANCE;
    }

    /**
     * Constructs a word from a single letter.
     *
     * @param letter
     *         the letter
     *
     * @return a word consisting of only this letter
     */
    public static <I> Word<I> fromLetter(I letter) {
        return new LetterWord<>(letter);
    }

    /**
     * Creates a word from a subrange of an array of symbols. Note that to ensure immutability, internally a copy of the
     * array is made.
     *
     * @param symbols
     *         the symbols array
     * @param offset
     *         the starting index in the array
     * @param length
     *         the length of the resulting word (from the starting index on)
     *
     * @return the word consisting of the symbols in the range
     */
    public static <I> Word<I> fromArray(I[] symbols, int offset, int length) {
        if (length == 0) {
            return epsilon();
        }
        if (length == 1) {
            return fromLetter(symbols[offset]);
        }
        Object[] array = new Object[length];
        System.arraycopy(symbols, offset, array, 0, length);
        return new SharedWord<>(array);
    }

    /**
     * Creates a word from a list of symbols.
     *
     * @param symbolList
     *         the list of symbols
     *
     * @return the resulting word
     */
    public static <I> Word<I> fromList(List<? extends I> symbolList) {
        int siz = symbolList.size();
        if (siz == 0) {
            return epsilon();
        }
        if (siz == 1) {
            return Word.fromLetter(symbolList.get(0));
        }
        return new SharedWord<>(symbolList);
    }

    public static Word<Character> fromString(String str) {
        return fromCharSequence(str);
    }

    public static Word<Character> fromCharSequence(CharSequence cs) {
        int len = cs.length();
        Character[] chars = new Character[len];
        for (int i = 0; i < len; i++) {
            chars[i] = cs.charAt(i);
        }
        return new SharedWord<>(chars);
    }

    @SafeVarargs
    public static <I> Word<I> fromWords(Word<? extends I>... words) {
        return fromWords(Arrays.asList(words));
    }

    public static <I> Word<I> fromWords(Collection<? extends Word<? extends I>> words) {
        int totalLength = 0;
        for (Word<?> w : words) {
            totalLength += w.length();
        }

        if (totalLength == 0) {
            return epsilon();
        }

        Object[] array = new Object[totalLength];

        int currOfs = 0;
        for (Word<? extends I> w : words) {
            AWUtil.safeWrite(w, array, currOfs);
            currOfs += w.length();
        }

        return new SharedWord<>(array);
    }

    /**
     * Retrieves the length of this word.
     *
     * @return the length of this word.
     */
    public abstract int length();

    /**
     * Performs an upcast of the generic type parameter of the word. Since words are immutable, the type parameter
     * {@code <I>} is covariant (even though it is not possible to express this in Java), making this a safe operation.
     *
     * @param word
     *         the word to upcast
     *
     * @return the upcasted word (reference identical to {@code word})
     */
    @SuppressWarnings("unchecked")
    public static <I> Word<I> upcast(Word<? extends I> word) {
        return (Word<I>) word;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        for (I sym : this) {
            hash *= 89;
            hash += (sym != null) ? sym.hashCode() : 0;
        }
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Word)) {
            return false;
        }
        Word<?> otherWord = (Word<?>) other;
        int len = otherWord.length();
        if (len != length()) {
            return false;
        }
        java.util.Iterator<I> thisIt = iterator();
        java.util.Iterator<?> otherIt = otherWord.iterator();
        while (thisIt.hasNext()) {
            I thisSym = thisIt.next();
            Object otherSym = otherIt.next();
            if (!Objects.equals(thisSym, otherSym)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public java.util.Iterator<I> iterator() {
        return new Iterator();
    }

    @Override
    public Spliterator<I> spliterator() {
        return Spliterators.spliterator(iterator(),
                                        length(),
                                        Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SUBSIZED);
    }

    @Override
    public void print(Appendable a) throws IOException {
        if (isEmpty()) {
            a.append(EMPTY_WORD_REP);
        } else {
            a.append(WORD_DELIM_LEFT);
            java.util.Iterator<? extends I> symIt = iterator();
            assert symIt.hasNext();
            appendSymbol(a, symIt.next());
            while (symIt.hasNext()) {
                a.append(WORD_SYMBOL_SEPARATOR);
                appendSymbol(a, symIt.next());
            }
            a.append(WORD_DELIM_RIGHT);
        }
    }

    /**
     * Checks if this word is empty, i.e., contains no symbols.
     *
     * @return {@code true} if this word is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return length() == 0;
    }

    private static void appendSymbol(Appendable a, @Nullable Object symbol) throws IOException {
        a.append(WORD_SYMBOL_DELIM_LEFT);
        a.append(String.valueOf(symbol));
        a.append(WORD_SYMBOL_DELIM_RIGHT);
    }

    public Stream<I> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Retrieves the subword of this word starting at the given index and extending until the end of this word. Calling
     * this method is equivalent to calling
     * <pre>w.subWord(fromIndex, w.length())</pre>
     *
     * @param fromIndex
     *         the first index, inclusive
     *
     * @return the word representing the specified subrange
     */
    public final Word<I> subWord(int fromIndex) {
        if (fromIndex <= 0) {
            if (fromIndex == 0) {
                return this;
            }
            throw new IndexOutOfBoundsException("Invalid subword range [" + fromIndex + ",)");
        }
        return subWordInternal(fromIndex, length());
    }

    /**
     * Retrieves a word representing the specified subrange of this word. As words are immutable, this function usually
     * can be realized quite efficient (implementing classes should take care of this).
     *
     * @param fromIndex
     *         the first index, inclusive.
     * @param toIndex
     *         the last index, exclusive.
     *
     * @return the word representing the specified subrange.
     */
    public final Word<I> subWord(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < fromIndex || toIndex > length()) {
            throw new IndexOutOfBoundsException("Invalid subword range [" + fromIndex + ", " + toIndex + ")");
        }

        return subWordInternal(fromIndex, toIndex);
    }

    /**
     * Internal subword operation implementation. In contrast to {@link #subWord(int, int)}, no range checks need to be
     * performed. As this method is flagged as {@code protected}, implementations may rely on the specified indices
     * being valid.
     *
     * @param fromIndex
     *         the first index, inclusive (guaranteed to be valid)
     * @param toIndex
     *         the last index, exclusive (guaranteed to be valid)
     *
     * @return the word representing the specified subrange
     */
    protected Word<I> subWordInternal(int fromIndex, int toIndex) {
        int len = toIndex - fromIndex;
        Object[] array = new Object[len];
        writeToArray(fromIndex, array, 0, len);
        return new SharedWord<>(array);
    }

    @Override
    public void writeToArray(int offset, @Nullable Object[] array, int tgtOffset, int length) {
        int idx = offset, arrayIdx = tgtOffset;

        for (int i = length; i > 0; i--) {
            array[arrayIdx++] = getSymbol(idx++);
        }
    }

    /**
     * Return symbol that is at the specified position.
     *
     * @param index
     *         the position
     *
     * @return symbol at position i.
     *
     * @throws IndexOutOfBoundsException
     *         if there is no symbol with this index.
     */
    public abstract I getSymbol(int index);

    @Override
    public final int size() {
        return length();
    }

    /**
     * Retrieves a {@link List} view on the contents of this word.
     *
     * @return an unmodifiable list of the contained symbols.
     */
    public List<I> asList() {
        return new AsList();
    }

    /**
     * Retrieves a {@link IntSeq} view on the contents of this word for a given indexing function (e.g. an {@link Alphabet}).
     *
     * @return an {@link IntSeq} view of the contained symbols.
     */
    public IntSeq asIntSeq(ToIntFunction<I> indexFunction) {
        return new AsIntSeq(indexFunction);
    }

    /**
     * Retrieves the list of all prefixes of this word. In the default implementation, the prefixes are lazily
     * instantiated upon the respective calls of {@link List#get(int)} or {@link Iterator#next()}.
     *
     * @param longestFirst
     *         whether to start with the longest prefix (otherwise, the first prefix in the list will be the shortest).
     *
     * @return a (non-materialized) list containing all prefixes
     */
    public List<Word<I>> prefixes(boolean longestFirst) {
        return new SubwordList<>(this, true, longestFirst);
    }

    /**
     * Retrieves the list of all suffixes of this word. In the default implementation, the suffixes are lazily
     * instantiated upon the respective calls of {@link List#get(int)} or {@link Iterator#next()}.
     *
     * @param longestFirst
     *         whether to start with the longest suffix (otherwise, the first suffix in the list will be the shortest).
     *
     * @return a (non-materialized) list containing all suffix
     */
    public List<Word<I>> suffixes(boolean longestFirst) {
        return new SubwordList<>(this, false, longestFirst);
    }

    /**
     * Retrieves the next word after this in canonical order. Figuratively speaking, if there are {@code k} alphabet
     * symbols, one can think of a word of length {@code n} as an {@code n}-digit radix-{@code k} representation of the
     * number. The next word in canonical order is the representation for the number represented by this word plus one.
     *
     * @param sigma
     *         the alphabet
     *
     * @return the next word in canonical order
     */
    public Word<I> canonicalNext(Alphabet<I> sigma) {
        int len = length();
        @Nullable Object[] symbols = new Object[len];
        writeToArray(0, symbols, 0, len);

        int alphabetSize = sigma.size();

        int i = 0;
        boolean overflow = true;
        for (I sym : this) {
            int nextIdx = (sigma.getSymbolIndex(sym) + 1) % alphabetSize;
            symbols[i++] = sigma.getSymbol(nextIdx);
            if (nextIdx != 0) {
                overflow = false;
                break;
            }
        }

        while (i < len) {
            symbols[i] = getSymbol(i);
            i++;
        }

        if (overflow) {
            @Nullable Object[] newSymbols = new Object[len + 1];
            newSymbols[0] = sigma.getSymbol(0);
            System.arraycopy(symbols, 0, newSymbols, 1, len);
            symbols = newSymbols;
        }

        return new SharedWord<>(symbols);
    }

    /**
     * Retrieves the last symbol of this word.
     *
     * @return the last symbol of this word.
     */
    public I lastSymbol() {
        return getSymbol(length() - 1);
    }

    /**
     * Retrieves the first symbol of this word.
     *
     * @return the first symbol of this word
     */
    public I firstSymbol() {
        return getSymbol(0);
    }

    /**
     * Appends a symbol to this word and returns the result as a new word.
     *
     * @param symbol
     *         the symbol to append
     *
     * @return the word plus the given symbol
     */
    public Word<I> append(I symbol) {
        int len = length();
        @Nullable Object[] array = new Object[len + 1];
        writeToArray(0, array, 0, len);
        array[len] = symbol;
        return new SharedWord<>(array);
    }

    /**
     * Prepends a symbol to this word and returns the result as a new word.
     *
     * @param symbol
     *         the symbol to prepend
     *
     * @return the given symbol plus to word.
     */
    public Word<I> prepend(I symbol) {
        int len = length();
        @Nullable Object[] array = new Object[len + 1];
        array[0] = symbol;
        writeToArray(0, array, 1, len);

        return new SharedWord<>(array);
    }

    /**
     * Concatenates this word with several other words and returns the result as a new word.
     * <p>
     * Note that this method cannot be overridden. Implementing classes need to override the {@link
     * #concatInternal(Word...)} method instead.
     *
     * @param words
     *         the words to concatenate with this word
     *
     * @return the result of the concatenation
     *
     * @see #concatInternal(Word...)
     */
    @SafeVarargs
    public final Word<I> concat(Word<? extends I>... words) {
        return concatInternal(words);
    }

    /**
     * Realizes the concatenation of this word with several other words.
     *
     * @param words
     *         the words to concatenate
     *
     * @return the results of the concatenation
     */
    @SuppressWarnings("unchecked")
    protected Word<I> concatInternal(Word<? extends I>... words) {
        if (words.length == 0) {
            return this;
        }

        int len = length();

        int totalSize = len;
        for (Word<? extends I> word : words) {
            totalSize += word.length();
        }

        Object[] array = new Object[totalSize];
        writeToArray(0, array, 0, len);
        int currOfs = len;
        for (Word<? extends I> w : words) {
            int wLen = w.length();
            w.writeToArray(0, array, currOfs, wLen);
            currOfs += wLen;
        }

        return new SharedWord<>(array);
    }

    /**
     * Checks if this word is a prefix of another word.
     *
     * @param other
     *         the other word
     *
     * @return {@code true} if this word is a prefix of the other word, {@code false} otherwise.
     */
    public boolean isPrefixOf(Word<?> other) {
        int len = length(), otherLen = other.length();
        if (otherLen < len) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            I sym1 = getSymbol(i);
            Object sym2 = other.getSymbol(i);

            if (!Objects.equals(sym1, sym2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines the longest common prefix of this word and another word.
     *
     * @param other
     *         the other word
     *
     * @return the longest common prefix of this word and the other word
     */
    public Word<I> longestCommonPrefix(Word<?> other) {
        int len = length(), otherLen = other.length();
        int maxIdx = Math.min(len, otherLen);

        int i = 0;
        while (i < maxIdx) {
            I sym1 = getSymbol(i);
            Object sym2 = other.getSymbol(i);

            if (!Objects.equals(sym1, sym2)) {
                break;
            }
            i++;
        }

        return prefix(i);
    }

    /**
     * Retrieves a prefix of the given length. If <code>length</code> is negative, then a prefix consisting of all but
     * the last <code>-length</code> symbols is returned.
     *
     * @param prefixLen
     *         the length of the prefix (may be negative, see above).
     *
     * @return the prefix of the given length.
     */
    public final Word<I> prefix(int prefixLen) {
        final int length = prefixLen < 0 ? length() + prefixLen : prefixLen;
        return subWord(0, length);
    }

    /**
     * Checks if this word is a suffix of another word.
     *
     * @param other
     *         the other word
     *
     * @return {@code true} if this word is a suffix of the other word, {@code false} otherwise.
     */
    public boolean isSuffixOf(Word<?> other) {
        int len = length(), otherLen = other.length();
        if (otherLen < len) {
            return false;
        }

        int ofs = otherLen - len;
        for (int i = 0; i < len; i++) {
            I sym1 = getSymbol(i);
            Object sym2 = other.getSymbol(ofs + i);
            if (!Objects.equals(sym1, sym2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines the longest common suffix of this word and another word.
     *
     * @param other
     *         the other word
     *
     * @return the longest common suffix
     */
    public Word<I> longestCommonSuffix(Word<?> other) {
        int len = length(), otherLen = other.length();
        int minLen = Math.min(len, otherLen);

        int idx1 = len, idx2 = otherLen;
        int i = 0;
        while (i < minLen) {
            I sym1 = getSymbol(--idx1);
            Object sym2 = other.getSymbol(--idx2);

            if (!Objects.equals(sym1, sym2)) {
                break;
            }

            i++;
        }

        return suffix(i);
    }

    /**
     * Retrieves a suffix of the given length. If <code>length</code> is negative, then a suffix consisting of all but
     * the first <code>-length</code> symbols is returned.
     *
     * @param suffixLen
     *         the length of the suffix (may be negative, see above).
     *
     * @return the suffix of the given length.
     */
    public final Word<I> suffix(int suffixLen) {
        int wordLen = length();
        int startIdx = suffixLen < 0 ? -suffixLen : wordLen - suffixLen;

        return subWord(startIdx, wordLen);
    }

    /**
     * Retrieves a "flattened" version of this word, i.e., without any hierarchical structure attached. This can be
     * helpful if {@link Word} is subclassed to allow representing, e.g., a concatenation dynamically, but due to
     * performance concerns not too many levels of indirection should be introduced.
     *
     * @return a flattened version of this word.
     */
    public Word<I> flatten() {
        int len = length();
        Object[] array = new Object[len];
        writeToArray(0, array, 0, len);
        return new SharedWord<>(array);
    }

    public Word<I> trimmed() {
        int len = length();
        Object[] array = new Object[len];
        writeToArray(0, array, 0, len);
        return new SharedWord<>(array);
    }

    /**
     * Transforms this word into an array of integers, using the specified function for translating an individual symbol
     * to an integer.
     *
     * @param toInt
     *         the function for translating symbols to integers
     *
     * @return an integer-array representation of the word, according to the specified translation function
     */
    public int[] toIntArray(ToIntFunction<? super I> toInt) {
        int len = length();
        int[] result = new int[len];
        int i = 0;
        for (I sym : this) {
            int symIdx = toInt.applyAsInt(sym);
            result[i++] = symIdx;
        }
        return result;
    }

    /**
     * Transforms a word symbol-by-symbol, using the specified transformation function.
     *
     * @param transformer
     *         the transformation function
     *
     * @return the transformed word
     */
    public <T> Word<T> transform(Function<? super I, ? extends T> transformer) {
        int len = length();
        @Nullable Object[] array = new Object[len];
        int i = 0;
        for (I symbol : this) {
            array[i++] = transformer.apply(symbol);
        }
        return new SharedWord<>(array);
    }

    /**
     * Returns a {@link Collector} that collects individual symbols (in order) and aggregates them in a {@link Word}.
     *
     * @param <I>
     *         input symbol type
     *
     * @return a {@link Collector} that collects individual symbols in order and aggregates them in a {@link Word}
     */
    public static <I> Collector<I, ?, Word<I>> collector() {
        return new WordCollector<>();
    }

    /*
     * General word iterator
     */
    private class Iterator implements java.util.Iterator<I> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < Word.this.length();
        }

        @Override
        public I next() {
            if (index >= Word.this.length()) {
                throw new NoSuchElementException();
            }
            return Word.this.getSymbol(index++);
        }
    }

    /*
     * Representing a word as a list.
     */
    private class AsList extends AbstractList<I> {

        @Override
        public I get(int index) {
            return Word.this.getSymbol(index);
        }

        @Override
        public java.util.Iterator<I> iterator() {
            return Word.this.iterator();
        }

        @Override
        public int size() {
            return Word.this.length();
        }
    }

    /*
     * Representing a word as an IntSeq.
     */
    private class AsIntSeq implements IntSeq {

        private final ToIntFunction<I> indexFunction;

        AsIntSeq(ToIntFunction<I> indexFunction) {
            this.indexFunction = indexFunction;
        }

        @Override
        public int size() {
            return Word.this.size();
        }

        @Override
        public int get(int index) {
            return indexFunction.applyAsInt(Word.this.getSymbol(index));
        }

        @Override
        public String toString() {
            return Word.this.toString();
        }
    }
}
