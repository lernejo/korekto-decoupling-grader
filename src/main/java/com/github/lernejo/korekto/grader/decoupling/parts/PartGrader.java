package com.github.lernejo.korekto.grader.decoupling.parts;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SubjectForToolkitInclusion
public interface PartGrader {

    Logger LOGGER = LoggerFactory.getLogger(PartGrader.class);

    String name();

    default Double maxGrade() {
        return null;
    }

    default double minGrade() {
        return 0.0D;
    }

    GradePart grade(GradingConfiguration configuration, Exercise exercise, LaunchingContext context, GitContext gitContext);

    default GradePart result(List<String> explanations, double grade) {
        return new GradePart(name(), Math.min(Math.max(minGrade(), grade), maxGrade()), maxGrade(), explanations);
    }
}
