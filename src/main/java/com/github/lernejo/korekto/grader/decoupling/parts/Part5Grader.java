package com.github.lernejo.korekto.grader.decoupling.parts;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import org.jetbrains.annotations.NotNull;

public class Part5Grader implements PartGrader {

    @Override
    public String name() {
        return "Part 5 - Computer Player";
    }

    @Override
    public Double maxGrade() {
        return 4.0D;
    }

    @Override
    public GradePart grade(GradingConfiguration configuration, Exercise exercise, LaunchingContext context, GitContext gitContext) {
        if (context.compilationFailed) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        String mainClass = "fr.lernejo.guessgame.Launcher";
        context.processBuilder.command(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString(), "-cp", exercise.getRoot().resolve("target").resolve("classes").toString(), mainClass, "-auto", "56");

        int attempt = 0;
        String storedResult = null;
        do {
            attempt++;
            try (CloseableProcess process = new CloseableProcess(context.processBuilder.start())) {
                String welcome = readOutput(process.process());// optional welcome message

                String result = readOutput(process.process());
                String error = readStream(process.process().getErrorStream());
                if (error != null) {
                    return result(List.of("An error occurred: " + error), 0);
                }
                if (welcome == null && result == null) {
                    continue;
                } else {
                    storedResult = welcome + "\n" + result;
                }

                List<String> scopedResult = tokenize(storedResult);
                if (welcome != null) {
                    scopedResult.addAll(tokenize(welcome));
                }
                if (Part4Grader.endKeywords.stream().anyMatch(scopedResult::contains)) {
                    return result(List.of(), maxGrade());
                }
            } catch (IOException | RuntimeException e) {
                return result(List.of("Cannot start " + mainClass + ": " + e.getMessage()), 0.0D);
            }
        } while (attempt < 3);
        if (storedResult == null) {
            return result(List.of("No information output after launching the game in auto mode"), 0.0D);
        } else {
            return result(List.of("No meaningful keywords in output (expecting to contain one of " + Part4Grader.endKeywords + ", but was: " + storedResult), maxGrade() / 4);
        }
    }

    @NotNull
    private List<String> tokenize(String result) {
        return new ArrayList<>(List.of(result.toLowerCase().split(Part4Grader.TOKENIZE_REGEX)));
    }
}
