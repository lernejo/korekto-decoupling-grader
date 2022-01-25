package com.github.lernejo.korekto.grader.decoupling.parts;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Part6Grader implements PartGrader<LaunchingContext> {

    @NotNull
    @Override
    public String name() {
        return "Part 6 - Logger composition";
    }

    @NotNull
    @Override
    public Double maxGrade() {
        return 4.0D;
    }

    @NotNull
    @Override
    public GradePart grade(LaunchingContext context) {
        if (context.loggerClass == null) {
            return result(List.of("Not available when there is no logger class"), 0.0D);
        }

        List<String> missingClasses = new ArrayList<>();

        Class<?> compositeLoggerClass = loadClass(context.exerciseClassloader, "fr.lernejo.logger.CompositeLogger", missingClasses);
        Class<?> contextualLoggerClass = loadClass(context.exerciseClassloader, "fr.lernejo.logger.ContextualLogger", missingClasses);
        Class<?> filteredLoggerClass = loadClass(context.exerciseClassloader, "fr.lernejo.logger.FilteredLogger", missingClasses);

        if (!missingClasses.isEmpty()) {
            return result(List.of("Classes not found: " + missingClasses), 0.0D);
        }

        Class<?> testLoggerClass = new TestLoggerClassBuilder(context.loggerClass).buildTestLoggerClass();
        TestLoggerAccess testLoggerFiltered = TestLoggerClassBuilder.buildTestLogger(testLoggerClass);
        TestLoggerAccess testLoggerContextual = TestLoggerClassBuilder.buildTestLogger(testLoggerClass);

        Predicate<String> condition = m -> !m.contains("1234");
        final Object filteredLogger;
        try {
            // TODO here we assume the order of the params, we need to cover all cases (+ inversion and bad params)
            filteredLogger = filteredLoggerClass.getConstructor(context.loggerClass, Predicate.class).newInstance(testLoggerFiltered, condition);
        } catch (ReflectiveOperationException e) {
            return result(List.of("Unable to invoke FilteredLogger constructor: " + e.getMessage()), 0.0D);
        }

        String contextMarker = "--testContext--";
        final Object contextualLogger;
        try {
            // TODO here we assume the order of the params, we need to cover all cases (+ inversion and bad params)
            contextualLogger = contextualLoggerClass.getConstructor(String.class, context.loggerClass).newInstance(contextMarker, testLoggerContextual);
        } catch (ReflectiveOperationException e) {
            return result(List.of("Unable to invoke FilteredLogger constructor: " + e.getMessage()), 0.0D);
        }

        final Object compositeLogger;
        try {
            compositeLogger = compositeLoggerClass.getConstructor(context.loggerClass, context.loggerClass).newInstance(filteredLogger, contextualLogger);
        } catch (ReflectiveOperationException e) {
            return result(List.of("Unable to invoke FilteredLogger constructor: " + e.getMessage()), 0.0D);
        }

        final Method logMethod;
        try {
            logMethod = context.loggerClass.getMethod("log", String.class);
        } catch (NoSuchMethodException e) {
            return result(List.of("Logger does not have a `void log(String message)` method: " + e.getMessage()), 0.0D);
        }

        String initialMessage = "random test message";
        try {
            logMethod.invoke(compositeLogger, initialMessage);
        } catch (ReflectiveOperationException e) {
            return result(List.of("Unable to invoke log method on CompositeLogger: " + e.getMessage()), 0.0D);
        }

        String contextualLastMessage = testLoggerContextual.getLastMessage();
        if (contextualLastMessage == null || !contextualLastMessage.contains(contextMarker) || !contextualLastMessage.contains(initialMessage)) {
            return result(List.of("Down the logger chain, expecting the message to contain: `" + contextMarker + "` and `" + initialMessage + "`, but was : `" + contextualLastMessage + "`"), maxGrade() * 1 / 4);
        }

        String filteredLastMessage = testLoggerFiltered.getLastMessage();
        if (initialMessage == null || !initialMessage.equals(filteredLastMessage)) {
            return result(List.of("Down the logger chain, expecting the message `" + initialMessage + "`, but was : `" + filteredLastMessage + "`"), maxGrade() * 2 / 4);
        }

        testLoggerContextual.reset();
        testLoggerFiltered.reset();

        String secondMessage = "[1234] this message should be filtered";
        try {
            logMethod.invoke(compositeLogger, secondMessage);
        } catch (ReflectiveOperationException e) {
            return result(List.of("Unable to invoke log method on CompositeLogger: " + e.getMessage()), 0.0D);
        }

        contextualLastMessage = testLoggerContextual.getLastMessage();
        filteredLastMessage = testLoggerFiltered.getLastMessage();

        if (contextualLastMessage == null || !contextualLastMessage.contains(secondMessage)) {
            return result(List.of("Down the logger chain, expecting the contextual logger to receive nothing a message containing `" + secondMessage + "`, but was : `" + contextualLastMessage + "`"), maxGrade() * 2 / 4);
        }

        if (filteredLastMessage != null) {
            return result(List.of("Down the logger chain, expecting the filtered logger to receive nothing but it did: `" + filteredLastMessage + "`"), maxGrade() * 2 / 4);
        }

        return result(List.of(), maxGrade());
    }

    private Class<?> loadClass(URLClassLoader classloader, String className, List<String> missingClasses) {
        try {
            return classloader.loadClass(className);
        } catch (ClassNotFoundException e) {
            missingClasses.add(className);
            return null;
        }
    }
}
