package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;
import com.github.lernejo.korekto.toolkit.partgrader.MavenContext;
import org.jetbrains.annotations.NotNull;

import java.net.URLClassLoader;

public class LaunchingContext extends GradingContext implements MavenContext {
    public final ProcessBuilder processBuilder = new ProcessBuilder();
    private boolean compilationFailed;
    private boolean testFailed;
    public URLClassLoader exerciseClassloader;
    public Class<?> loggerClass;

    LaunchingContext(@NotNull GradingConfiguration configuration) {
        super(configuration);
    }

    @Override
    public boolean hasCompilationFailed() {
        return compilationFailed;
    }

    @Override
    public boolean hasTestFailed() {
        return testFailed;
    }

    @Override
    public void markAsCompilationFailed() {
        this.compilationFailed = true;
    }

    @Override
    public void markAsTestFailed() {
        this.testFailed = true;
    }
}
