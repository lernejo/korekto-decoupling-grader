package com.github.lernejo.korekto.grader.decoupling.parts;

public interface TestLoggerAccess {

    String getLastMessage();

    void reset();
}
