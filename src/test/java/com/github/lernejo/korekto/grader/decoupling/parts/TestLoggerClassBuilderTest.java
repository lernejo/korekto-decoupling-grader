package com.github.lernejo.korekto.grader.decoupling.parts;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TestLoggerClassBuilderTest {

    @Test
    void testLogger_is_interoperable() throws ReflectiveOperationException {
        Class<?> testLoggerClass = new TestLoggerClassBuilder(Logger.class).buildTestLoggerClass();

        TestLoggerAccess testLogger = TestLoggerClassBuilder.buildTestLogger(testLoggerClass);
        assertThat(testLogger).isNotNull();

        testLogger.reset();
        assertThat(testLogger.getLastMessage()).isNull();

        Method logMethod = testLoggerClass.getMethod("log", String.class);

        logMethod.invoke(testLogger, "a test message");

        assertThat(testLogger.getLastMessage()).isEqualTo("a test message");
        testLogger.reset();
        assertThat(testLogger.getLastMessage()).isNull();
    }

    public interface Logger {
        void log(String message);
    }
}
