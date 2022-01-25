package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;
import org.jetbrains.annotations.NotNull;

import java.net.URLClassLoader;

public class LaunchingContext extends GradingContext {
    public final ProcessBuilder processBuilder = new ProcessBuilder();
    public boolean compilationFailed;
    public boolean testFailed;
    public URLClassLoader exerciseClassloader;
    public Class<?> loggerClass;

    LaunchingContext(@NotNull GradingConfiguration configuration) {
        super(configuration);
    }
}
