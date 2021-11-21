package com.github.lernejo.korekto.grader.decoupling.parts;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Modifier;

record TestLoggerClassBuilder(Class<?> loggerClass) {

    Class<?> buildTestLoggerClass() {
        Class<?> loaded = new ByteBuddy()
            .subclass(Object.class)
            .name("TestLogger")
            .implement(TestLoggerAccess.class, loggerClass)
            .defineField("message", String.class, Modifier.PUBLIC)
            .method(ElementMatchers.named("log"))
            .intercept(FieldAccessor.ofField("message").setsArgumentAt(0))
            .method(ElementMatchers.named("getLastMessage"))
            .intercept(FieldAccessor.ofField("message"))
            .method(ElementMatchers.named("reset"))
            .intercept(FieldAccessor.ofField("message").setsValue((Object) null))
            .make()
            .load(loggerClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
        return loaded;
    }

    static TestLoggerAccess buildTestLogger(Class<?> clazz) {
        try {
            Object testLogger = clazz.getConstructor().newInstance();
            return (TestLoggerAccess) testLogger;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create TestLogger", e);
        }
    }
}
