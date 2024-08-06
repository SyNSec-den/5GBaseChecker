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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReflectUtilTest {

    private static final Class<?>[] SIGNATURE_1 =
            new Class[] {Integer.class, Boolean.class, Character.class, Byte.class};
    private static final Class<?>[] SIGNATURE_2 = new Class[] {int.class, boolean.class, char.class, byte.class};
    private static final Class<?>[] SIGNATURE_3 = new Class[] {Integer.class, Boolean.class, char.class, byte.class};
    private static final Class<?>[] SIGNATURE_4 = new Class[] {Integer.class, byte.class};

    private Method methodToFind;
    private String methodName;

    @BeforeMethod
    public void setUp() throws NoSuchMethodException {
        methodToFind = TestClass.class.getDeclaredMethod("method1", SIGNATURE_3);
        methodName = methodToFind.getName();

    }

    @Test
    public void testConstructors() {

        @SuppressWarnings("unchecked")
        final Constructor<TestClass> actualConstructor = (Constructor<TestClass>) TestClass.class.getConstructors()[0];

        final Constructor<TestClass> constructor1 = ReflectUtil.findConstructor(TestClass.class, SIGNATURE_1);
        final Constructor<TestClass> constructor2 = ReflectUtil.findConstructor(TestClass.class, SIGNATURE_2);
        final Constructor<TestClass> constructor3 = ReflectUtil.findConstructor(TestClass.class, SIGNATURE_3);
        final Constructor<TestClass> constructor4 = ReflectUtil.findConstructor(TestClass.class, SIGNATURE_4);

        Assert.assertEquals(constructor1, actualConstructor);
        Assert.assertEquals(constructor2, actualConstructor);
        Assert.assertEquals(constructor3, actualConstructor);
        Assert.assertNull(constructor4);
    }

    @Test
    public void testMethod() {

        final Method method1 = ReflectUtil.findMethod(TestClass.class, methodName, SIGNATURE_1);
        final Method method2 = ReflectUtil.findMethod(TestClass.class, methodName, SIGNATURE_2);
        final Method method3 = ReflectUtil.findMethod(TestClass.class, methodName, SIGNATURE_3);
        final Method method4 = ReflectUtil.findMethod(TestClass.class, methodName, SIGNATURE_4);
        final Method method5 = ReflectUtil.findMethod(TestClass.class, "randomName", SIGNATURE_1);

        Assert.assertEquals(method1, methodToFind);
        Assert.assertEquals(method2, methodToFind);
        Assert.assertEquals(method3, methodToFind);
        Assert.assertNull(method4);
        Assert.assertNull(method5);
    }

    @Test
    public void testMethodByArgs() {

        final Method method1 =
                ReflectUtil.findMatchingMethod(TestClass.class, methodName, 1, Boolean.TRUE, 'c', (byte) 3);
        final Method method2 = ReflectUtil.findMatchingMethod(TestClass.class, methodName, 1, null, 'c', (byte) 3);
        final Method method3 = ReflectUtil.findMatchingMethod(TestClass.class, methodName, 1, true, null, null);
        final Method method4 = ReflectUtil.findMatchingMethod(TestClass.class, methodName, 4L, false);
        final Method method5 = ReflectUtil.findMatchingMethod(TestClass.class, "randomName", 1, true, 'c', (byte) 3);

        Assert.assertEquals(method1, methodToFind);
        Assert.assertEquals(method2, methodToFind);
        Assert.assertNull(method3);
        Assert.assertNull(method4);
        Assert.assertNull(method5);
    }

    @Test
    public void testMethodWithReturnType() {

        final Method method1 = ReflectUtil.findMethodRT(TestClass.class, methodName, null, SIGNATURE_1);
        final Method method2 = ReflectUtil.findMethodRT(TestClass.class, methodName, Void.class, SIGNATURE_1);
        final Method method3 = ReflectUtil.findMethodRT(TestClass.class, methodName, Integer.class, SIGNATURE_1);

        Assert.assertEquals(method1, methodToFind);
        Assert.assertEquals(method2, methodToFind);
        Assert.assertNull(method3);

        final Method method4 = ReflectUtil.findMethodRT(TestClass.class, methodName, null, SIGNATURE_2);
        final Method method5 = ReflectUtil.findMethodRT(TestClass.class, methodName, Void.class, SIGNATURE_2);
        final Method method6 = ReflectUtil.findMethodRT(TestClass.class, methodName, Integer.class, SIGNATURE_2);

        Assert.assertEquals(method4, methodToFind);
        Assert.assertEquals(method5, methodToFind);
        Assert.assertNull(method6);

        final Method method7 = ReflectUtil.findMethodRT(TestClass.class, methodName, null, SIGNATURE_4);
        final Method method8 = ReflectUtil.findMethodRT(TestClass.class, methodName, Void.class, SIGNATURE_4);
        final Method method9 = ReflectUtil.findMethodRT(TestClass.class, methodName, Integer.class, SIGNATURE_4);

        Assert.assertNull(method7);
        Assert.assertNull(method8);
        Assert.assertNull(method9);

        final Method method10 = ReflectUtil.findMethodRT(TestClass.class, "randomName", null, SIGNATURE_1);
        final Method method11 = ReflectUtil.findMethodRT(TestClass.class, "randomName", Void.class, SIGNATURE_1);
        final Method method12 = ReflectUtil.findMethodRT(TestClass.class, "randomName", Integer.class, SIGNATURE_1);

        Assert.assertNull(method10);
        Assert.assertNull(method11);
        Assert.assertNull(method12);
    }

    public static final class TestClass {

        public TestClass(Integer boxed1, Boolean boxed2, char primitive1, byte primitive2) {}

        public void method1(Integer boxed1, Boolean boxed2, char primitive1, byte primitive2) {}

    }
}
