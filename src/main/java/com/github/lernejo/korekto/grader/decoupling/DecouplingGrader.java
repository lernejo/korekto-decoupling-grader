package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.grader.decoupling.parts.Part1Grader;
import com.github.lernejo.korekto.grader.decoupling.parts.Part2Grader;
import com.github.lernejo.korekto.grader.decoupling.parts.PartGrader;
import com.github.lernejo.korekto.toolkit.*;
import com.github.lernejo.korekto.toolkit.misc.HumanReadableDuration;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitNature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SubjectForToolkitInclusion
public class DecouplingGrader implements Grader {

    private final Logger logger = LoggerFactory.getLogger(DecouplingGrader.class);

    public String slugToRepoUrl(String slug) {
        return "https://github.com/" + slug + "/decoupling_java_training";
    }

    public void run(GradingConfiguration gradingConfiguration, GradingContext context) {
        Optional<GitNature> optionalGitNature = context.getExercise().lookupNature(GitNature.class);
        if (optionalGitNature.isEmpty()) {
            context.getGradeDetails().getParts().add(new GradePart("exercise", 0D, 12D, List.of("Not a Git project")));
        } else {
            GitNature gitNature = optionalGitNature.get();
            context.getGradeDetails().getParts().addAll(gitNature.withContext(c -> grade(gradingConfiguration, context.getExercise(), c)));
        }
    }

    private Collection<? extends GradePart> grade(GradingConfiguration configuration, Exercise exercise, GitContext gitContext) {
        LaunchingContext context = new LaunchingContext();
        return graders().stream()
            .map(g -> applyPartGrader(configuration, exercise, gitContext, context, g))
            .collect(Collectors.toList());
    }

    private GradePart applyPartGrader(GradingConfiguration configuration, Exercise exercise, GitContext gitContext, LaunchingContext context, PartGrader g) {
        long startTime = System.currentTimeMillis();
        try {
            return g.grade(configuration, exercise, context, gitContext);
        } finally {
            logger.debug(g.name() + " in " + HumanReadableDuration.toString(System.currentTimeMillis() - startTime));
        }
    }

    private Collection<? extends PartGrader> graders() {
        return List.of(
            new Part1Grader(),
            new Part2Grader()
        );
    }
}
