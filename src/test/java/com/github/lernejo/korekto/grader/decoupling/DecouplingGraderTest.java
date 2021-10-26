package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.toolkit.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DecouplingGraderTest {

    @BeforeEach
    void setUp() {
        String maven_home = System.getenv("MAVEN_HOME");
        if (maven_home != null && System.getProperty("maven.home") == null) {
            System.out.println("Setting ${maven.home}");
            System.setProperty("maven.home", maven_home);
        }
    }

    @Test
    void nominal_project() {
        Grader grader = Grader.Companion.load();
        String repoUrl = grader.slugToRepoUrl("lernejo");
        GradingConfiguration configuration = new GradingConfiguration(repoUrl, "", "", Paths.get("target/repositories"));

        AtomicReference<GradingContext> contextHolder = new AtomicReference<>();
        new GradingJob()
            .addCloneStep()
            .addStep("grading", grader)
            .addStep("report", (conf, context) -> contextHolder.set(context))
            .run(configuration);

        assertThat(contextHolder)
            .as("Grading context")
            .hasValueMatching(c -> c != null, "is present");

        assertThat(contextHolder.get().getGradeDetails().getParts())
            .containsExactly(
                new GradePart("Part 1 - Compilation & Tests", 4, 4.0D, List.of()),
                new GradePart("Part 2 - CI", 1, 1.0D, List.of())
            );
    }
}
