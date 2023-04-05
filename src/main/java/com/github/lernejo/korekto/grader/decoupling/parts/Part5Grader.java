package com.github.lernejo.korekto.grader.decoupling.parts;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.InteractiveProcess;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Part5Grader implements PartGrader<LaunchingContext> {

    private final Logger logger = LoggerFactory.getLogger(Part5Grader.class);

    @NotNull
    @Override
    public String name() {
        return "Part 5 - Computer Player";
    }

    @NotNull
    @Override
    public Double maxGrade() {
        return 4.0D;
    }

    private boolean isBlank(String s) {
        return  s == null || s.isBlank();
    }

    @NotNull
    @Override
    public GradePart grade(LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        int numberToGuess = LaunchingContext.getRandomSource().nextInt(57) + 57;

        String mainClass = "fr.lernejo.guessgame.Launcher";
        context.processBuilder.command(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString(),
            "-cp", context.getExercise().getRoot().resolve("target").resolve("classes").toString(), mainClass, "-auto", String.valueOf(numberToGuess));

        int attempt = 0;
        String storedResult = null;
        do {
            attempt++;
            try (InteractiveProcess process = new InteractiveProcess(context.processBuilder.start())) {
                String welcome = process.read();// optional welcome message

                String result = process.read();
                String error = process.readErr();
                if (error != null) {
                    if (isBlank(result) && isBlank(welcome)) {
                        return result(List.of("An error occurred: " + error), 0);
                    } else {
                        logger.warn("Java program wrote to stderr: " + error);
                    }
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
