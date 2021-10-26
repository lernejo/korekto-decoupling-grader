package com.github.lernejo.korekto.grader.decoupling;

import com.github.lernejo.korekto.toolkit.*;
import com.github.lernejo.korekto.toolkit.misc.OS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DecouplingGraderTest {

    private final Path workspace = Paths.get("target/repositories");

    @BeforeEach
    void setUp() {
        String maven_home = System.getenv("MAVEN_HOME");
        if (maven_home != null && System.getProperty("maven.home") == null) {
            System.out.println("Setting ${maven.home}");
            System.setProperty("maven.home", maven_home);
        }
        OS.Companion.getCURRENT_OS().deleteDirectoryCommand(workspace);
    }

    @Test
    void nominal_project() {
        Grader grader = Grader.Companion.load();
        String repoUrl = grader.slugToRepoUrl("lernejo");
        GradingConfiguration configuration = new GradingConfiguration(repoUrl, "", "", workspace);

        AtomicReference<GradingContext> contextHolder = new AtomicReference<>();
        new GradingJob()
            .addCloneStep()
            .addStep("grading", grader)
            .addStep("report", (conf, context) -> contextHolder.set(context))
            .run(configuration);

        assertThat(contextHolder)
            .as("Grading context")
            .hasValueMatching(Objects::nonNull, "is present");

        assertThat(contextHolder.get().getGradeDetails().getParts())
            .containsExactly(
                new GradePart("Part 1 - Compilation & Tests", 4, 4.0D, List.of()),
                new GradePart("Part 2 - CI", 1, 1.0D, List.of()),
                new GradePart("Part 3 - Console Logger", 2, 2.0D, List.of()),
                new GradePart("Part 4 - Human Player", 4, 4.0D, List.of()),
                new GradePart("Part 5 - Computer Player", 4, 4.0D, List.of())
            );
    }
}
