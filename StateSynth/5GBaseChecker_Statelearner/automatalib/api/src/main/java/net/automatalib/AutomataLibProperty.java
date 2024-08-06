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
package net.automatalib;

/**
 * An enum of all the system properties currently used by AutomataLib.
 *
 * @author frohme
 */
public enum AutomataLibProperty {

    /**
     * {@code automatalib.dot.exe.dir}.
     * <p>
     * The path to the dot installation directory.
     */
    DOT_EXE_DIR("dot.exe.dir"),

    /**
     * {@code automatalib.dot.exe.name}.
     * <p>
     * The name of the DOT executable.
     */
    DOT_EXE_NAME("dot.exe.name"),

    /**
     * {@code automatalib.ltsmin.path}.
     * <p>
     * Path to the binary folder of the LTSmin installation.
     */
    LTSMIN_PATH("ltsmin.path"),

    /**
     * {@code ltsmin.verbose}.
     * <p>
     * Whether to make LTSmin's output more verbose.
     * <p>
     * If set to anything else but "false", LTSmin's output will be more verbose.
     */
    LTSMIN_VERBOSE("ltsmin.verbose"),

    /**
     * {@code automatalib.visualization.provider}.
     * <p>
     * Property for setting the implementation of the visualization provider. The implementation must be available on
     * the classpath.
     */
    VISUALIZATION_PROVIDER("visualization.provider"),

    /**
     * {@code automatalib.word.delim.left}.
     * <p>
     * When printing words, the left delimiter for a word.
     */
    WORD_DELIM_LEFT("word.delim.left"),

    /**
     * {@code automatalib.word.delim.right}.
     * <p>
     * When printing words, the right delimiter for a word.
     */
    WORD_DELIM_RIGHT("word.delim.right"),

    /**
     * {@code automatalib.word.empty}.
     * <p>
     * When printing words, the representative for the empty word.
     */
    WORD_EMPTY_REP("word.empty"),

    /**
     * {@code automatalib.word.symbol.delim.left}.
     * <p>
     * When printing words, the left delimiter of a symbol.
     */
    WORD_SYMBOL_DELIM_LEFT("word.symbol.delim.left"),

    /**
     * {@code automatalib.word.symbol.delim.right}.
     * <p>
     * When printing words, the left delimiter of a symbol.
     */
    WORD_SYMBOL_DELIM_RIGHT("word.symbol.delim.right"),

    /**
     * {@code automatalib.word.symbol.separator}.
     * <p>
     * When printing words, the separator in between symbols.
     */
    WORD_SYMBOL_SEPARATOR("word.symbol.separator");

    private final String key;

    AutomataLibProperty(String key) {
        this.key = "automatalib." + key;
    }

    /**
     * Returns the actual system property key of the property.
     *
     * @return the system property key of the property.
     */
    public String getPropertyKey() {
        return this.key;
    }
}
