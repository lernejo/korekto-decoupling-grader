package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.grader.decoupling.parts.*;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.Grader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.HumanReadableDuration;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DecouplingGrader implements Grader<LaunchingContext> {

    private final Logger logger = LoggerFactory.getLogger(DecouplingGrader.class);

    @NotNull
    @Override
    public String slugToRepoUrl(@NotNull String slug) {
        return "https://github.com/" + slug + "/decoupling_java_training";
    }

    @Override
    public boolean needsWorkspaceReset() {
        return true;
    }

    @NotNull
    @Override
    public LaunchingContext gradingContext(@NotNull GradingConfiguration configuration) {
        return new LaunchingContext(configuration);
    }

    @Override
    public void run(LaunchingContext context) {
        context.getGradeDetails().getParts().addAll(grade(context));
    }

    @SubjectForToolkitInclusion
    private Collection<? extends GradePart> grade(LaunchingContext context) {
        return graders().stream()
            .map(g -> applyPartGrader(context, g))
            .collect(Collectors.toList());
    }

    private GradePart applyPartGrader(LaunchingContext context, PartGrader<LaunchingContext> g) {
        long startTime = System.currentTimeMillis();
        try {
            return g.grade(context);
        } finally {
            logger.debug(g.name() + " in " + HumanReadableDuration.toString(System.currentTimeMillis() - startTime));
        }
    }

    private Collection<? extends PartGrader<LaunchingContext>> graders() {
        return List.of(
            new Part1Grader(),
            new Part2Grader(),
            new Part3Grader(),
            new Part4Grader(),
            new Part5Grader(),
            new Part6Grader()
        );
    }
}
