package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.toolkit.Grader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;

public class DecouplingGrader implements Grader {

    public String slugToRepoUrl(String slug) {
        return "https://github.com/" + slug + "/decouplig_java_training";
    }

    public void run(GradingConfiguration gradingConfiguration, GradingContext gradingContext) {

    }
}
