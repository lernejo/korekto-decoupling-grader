package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;

import java.net.URLClassLoader;

@SubjectForToolkitInclusion
public class LaunchingContext {
    public final ProcessBuilder processBuilder = new ProcessBuilder();
    public boolean compilationFailed;
    public boolean testFailed;
    public URLClassLoader exerciseClassloader;
}
