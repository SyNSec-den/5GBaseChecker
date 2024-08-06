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
package net.automatalib.commons.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.google.common.primitives.Primitives;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility methods for using Java reflection.
 *
 * @author frohme
 * @author Malte Isberner
 */
public final class ReflectUtil {

    private ReflectUtil() {}

    /**
     * Tries to find a constructor that is able to accept parameters of the given types. First tries to find the
     * constructor matching the exact parameter types. If such a constructor does not exist, tries to find any (the
     * first match of arbitrary order) constructor that is able to accept the parameter types by means of auto-boxing,
     * i.e. a {@code Constructor(int)} would be returned for the parameters class {@code Integer.class}.
     * <p>
     * Returns {@code null} if no such constructor could be found.
     *
     * @param clazz
     *         the class which should be scanned for constructors
     * @param params
     *         the types of the constructor arguments
     * @param <T>
     *         the class type
     *
     * @return A constructor that is able of accepting parameters of the specified types, {@code null} if such a
     * constructor could not be found.
     */
    public static <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Class<?>... params) {
        try {
            return clazz.getConstructor(params);
        } catch (NoSuchMethodException e) {
            @SuppressWarnings("unchecked")
            Constructor<T>[] ctors = (Constructor<T>[]) clazz.getConstructors();

            for (Constructor<T> candidate : ctors) {
                if (w2pEquals(candidate.getParameterTypes(), params)) {
                    return candidate;
                }
            }

            return null;
        }
    }

    /**
     * Tries to find a method of the given name that is able to accept parameters of the given types. First tries to
     * find the method matching the exact parameter types. If such a method does not exist, tries to find any (the first
     * match of arbitrary order) method that is able to accept the parameter types by means of auto-boxing, i.e. a
     * {@code Method(int)} would be returned for the parameters class {@code Integer.class}.
     * <p>
     * Returns {@code null} if no such method could be found.
     *
     * @param clazz
     *         the class which should be scanned for methods
     * @param name
     *         the name of the method
     * @param params
     *         the types of the method arguments
     *
     * @return A method that is able to accept parameters of the specified types, {@code null} if such a method could
     * not be found.
     */
    public static @Nullable Method findMethod(Class<?> clazz, String name, Class<?>... params) {
        try {
            return clazz.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            Method[] methods = clazz.getMethods();

            for (Method candidate : methods) {
                if (candidate.getName().equals(name) && w2pEquals(candidate.getParameterTypes(), params)) {
                    return candidate;
                }
            }

            return null;
        }
    }

    /**
     * See {@link #findMethod(Class, String, Class...)}. This variation does not required the types of input parameters,
     * but can handle the actual objects, which should be passed to the method.
     *
     * @param clazz
     *         the class which should be scanned for methods
     * @param name
     *         the name of the method
     * @param args
     *         the objects that should be passed to the method, may contain {@code null}s
     *
     * @return A method that is able to accept of the specified objects, {@code null} if such a method could not be
     * found.
     */
    public static @Nullable Method findMatchingMethod(Class<?> clazz, String name, @Nullable Object... args) {
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(name)) {
                continue;
            }

            if (isMatch(m.getParameterTypes(), args)) {
                return m;
            }
        }

        return null;
    }

    /**
     * See {@link #findMethod(Class, String, Class...)}. This variation allows to additionally narrow the method by
     * specifying its return type.
     *
     * @param clazz
     *         the class which should be scanned for methods
     * @param name
     *         the name of the method
     * @param returnType
     *         the type of the returned object, if {@code null}, the return type will be ignored
     * @param params
     *         the types of the method arguments
     *
     * @return A method that is able to accept of the specified objects, {@code null} if such a method could not be
     * found.
     */
    public static @Nullable Method findMethodRT(Class<?> clazz,
                                                String name,
                                                @Nullable Class<?> returnType,
                                                Class<?>... params) {
        Method m = findMethod(clazz, name, params);

        if (m == null) {
            return null;
        } else if (returnType == null) {
            return m;
        }

        Class<?> rt = m.getReturnType();

        if (w2pEquals(rt, returnType) || returnType.isAssignableFrom(rt)) {
            return m;
        }

        return null;
    }

    private static boolean w2pEquals(Class<?>[] a, Class<?>... b) {
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (!w2pEquals(a[i], b[i])) {
                return false;
            }
        }

        return true;
    }

    private static boolean w2pEquals(Class<?> a, Class<?> b) {
        final Class<?> wrappedA = Primitives.unwrap(a);
        final Class<?> wrappedB = Primitives.unwrap(b);
        return wrappedA.equals(wrappedB);
    }

    private static boolean isMatch(Class<?>[] paramTypes, @Nullable Object... args) {
        if (paramTypes.length != args.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object arg = args[i];
            if (paramType.isPrimitive()) {
                if (arg == null) {
                    return false;
                }
                Class<?> argType = arg.getClass();
                if (paramType != Primitives.unwrap(argType)) {
                    return false;
                }
            } else {
                if (arg != null) {
                    Class<?> argType = arg.getClass();
                    if (!paramType.isAssignableFrom(argType)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
