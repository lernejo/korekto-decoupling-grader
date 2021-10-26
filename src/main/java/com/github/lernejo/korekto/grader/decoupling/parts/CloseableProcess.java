package com.github.lernejo.korekto.grader.decoupling.parts;

import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;

@SubjectForToolkitInclusion
public record CloseableProcess(Process process) implements AutoCloseable {

    @Override
    public void close() {
        process.destroyForcibly();
    }
}
