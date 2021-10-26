package com.github.lernejo.korekto.grader.decoupling.parts;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.misc.ClassLoaders;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Part3Grader implements PartGrader {

    @Override
    public String name() {
        return "Part 3 - Console Logger";
    }

    @Override
    public Double maxGrade() {
        return 2.0D;
    }

    @Override
    public GradePart grade(GradingConfiguration configuration, Exercise exercise, LaunchingContext context, GitContext gitContext) {
        if (context.compilationFailed) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        context.exerciseClassloader = ClassLoaders.newIsolatedClassLoader(exercise.getRoot().resolve("target").resolve("classes"));

        Class<?> loggerClass;
        String loggerClassName = "fr.lernejo.logger.Logger";
        String loggerFactoryClassName = "fr.lernejo.logger.LoggerFactory";
        String getLoggerMethodName = loggerFactoryClassName + "#getLogger(String)";
        try {
            loggerClass = context.exerciseClassloader.loadClass(loggerClassName);
        } catch (ClassNotFoundException e) {
            return result(List.of("Class not found: " + loggerClassName), 0.0D);
        }

        Class<?> loggerFactoryClass;
        try {
            loggerFactoryClass = context.exerciseClassloader.loadClass(loggerFactoryClassName);
        } catch (ClassNotFoundException e) {
            return result(List.of("Class not found: " + loggerFactoryClassName), 0.0D);
        }

        Method getLoggerMethod;
        try {
            getLoggerMethod = loggerFactoryClass.getMethod("getLogger", String.class);
        } catch (NoSuchMethodException e) {
            return result(List.of("Method not found: " + getLoggerMethodName), 0.0D);
        }

        String getLoggerMethodReturnType = getLoggerMethod.getReturnType().getName();
        if (!loggerClassName.equals(getLoggerMethodReturnType)) {
            return result(List.of("Method " + getLoggerMethod + " as a bad return type, expecting [" + loggerClassName + "] but was [" + getLoggerMethodReturnType + "]"), 0.0D);
        }

        Object logger;
        try {
            logger = getLoggerMethod.invoke(null, "toto");
        } catch (IllegalAccessException e) {
            return result(List.of("Cannot invoke " + getLoggerMethodName + ": " + e.getMessage()), 0.0D);
        } catch (InvocationTargetException e) {
            return result(List.of("Error during " + getLoggerMethodName + " invocation: " + e.getMessage()), 0.0D);
        }

        if (logger == null) {
            return result(List.of("Method " + getLoggerMethodName + " returns null"), 0.0D);
        } else if (!loggerClass.isAssignableFrom(logger.getClass())) {
            return result(List.of("Method " + getLoggerMethodName + " returns an object that is not implementing " + loggerClassName), 0.0D);
        }
        return result(List.of(), maxGrade());
    }
}
