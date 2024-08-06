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
package net.automatalib.exception;

/**
 * This specialized exception can be thrown if during the traversal of an automaton or transition system an undefined
 * property (e.g. a state or a transition output) is accessed, that is otherwise required for returning a valid result.
 *
 * @author frohme
 */
public class UndefinedPropertyAccessException extends IllegalStateException {

    public UndefinedPropertyAccessException() {}

    public UndefinedPropertyAccessException(String var1) {
        super(var1);
    }

    public UndefinedPropertyAccessException(String var1, Throwable var2) {
        super(var1, var2);
    }

    public UndefinedPropertyAccessException(Throwable var1) {
        super(var1);
    }
}
