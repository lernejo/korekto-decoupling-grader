package com.github.lernejo.korekto.grader.decoupling.parts;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;

public class Part4Grader implements PartGrader {
    static final Set<String> endKeywords = Set.of("done", "end", "win", "won", "fin", "bravo", "gagné", "trouvé");
    static final Set<String> lowerKeywords = Set.of("petit", "lower");
    static final Set<String> greaterKeywords = Set.of("grand", "greater");
    static final Set<String> decisionKeywords = Stream.concat(
        lowerKeywords.stream(),
        greaterKeywords.stream()
    ).collect(Collectors.toSet());

    @Override
    public String name() {
        return "Part 4 - Human Player";
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
        context.processBuilder.command(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString(), "-cp", exercise.getRoot().resolve("target").resolve("classes").toString(), mainClass, "-interactive");

        int attempt = 0;
        String storedResult;
        do {
            attempt++;
            try (CloseableProcess process = new CloseableProcess(context.processBuilder.start())) {
                String welcome = readOutput(process.process());// optional welcome message
                int min = 0;
                int max = 100;
                int nextGuess = (max + min) / 2;
                writeInput(process.process(), nextGuess + "\n");

                String result = readOutput(process.process());
                if (result == null) {
                    return result(List.of("No information given to player after submitting a guess"), maxGrade() * 1.0 / 3);
                }

                List<String> scopedResult = List.of(result.toLowerCase().split("\\s|\\."));
                if (endKeywords.stream().anyMatch(scopedResult::contains)) {
                    storedResult = result;
                    // Right guess at the first response, or return true...
                    // Retrying to have a nice tennis ball behavior
                    continue;
                }

                boolean keywordsPresent = decisionKeywords.stream().anyMatch(scopedResult::contains);
                if (!keywordsPresent) {
                    return result(List.of("No meaningful keywords in response (expecting to contain one of " + decisionKeywords + ", but was: " + result), maxGrade() * 1.0 / 3);
                }

                final int maxLoop = 100;
                int currentIteration = 0;
                do {
                    currentIteration++;

                    List<String> scopedResult2 = List.of(result.toLowerCase().split("\\s|\\."));
                    if (endKeywords.stream().anyMatch(scopedResult2::contains)) {
                        return result(List.of(), maxGrade());
                    }

                    boolean lower = lowerKeywords.stream().anyMatch(scopedResult2::contains);
                    if (lower) {
                        min = nextGuess;
                    } else {
                        max = nextGuess;
                    }
                    nextGuess = (max + min) / 2;
                    writeInput(process.process(), nextGuess + "\n");
                    result = readOutput(process.process());
                    storedResult = result;
                } while (currentIteration < maxLoop);
                // Factorize that,the only difference is the way the numbers are going (according to the use of the passive form or not)
                currentIteration = 0;
                min = 0;
                max = 100;
                nextGuess = (max + min) / 2;
                do {
                    currentIteration++;

                    List<String> scopedResult2 = List.of(result.toLowerCase().split("\\s|\\."));
                    if (endKeywords.stream().anyMatch(scopedResult2::contains)) {
                        return result(List.of(), maxGrade());
                    }

                    boolean lower = lowerKeywords.stream().anyMatch(scopedResult2::contains);
                    if (!lower) {
                        min = nextGuess;
                    } else {
                        max = nextGuess;
                    }
                    nextGuess = (max + min) / 2;
                    writeInput(process.process(), nextGuess + "\n");
                    result = readOutput(process.process());
                    storedResult = result;
                } while (currentIteration < maxLoop);
                return result(List.of("Cannot get the game to converge in " + maxLoop + " iterations (last message was: `" + storedResult + "`"), maxGrade() * 2.0 / 3);
            } catch (IOException | RuntimeException e) {
                return result(List.of("Cannot start " + mainClass + ": " + e.getMessage()), 0.0D);
            }
        } while (attempt < 3);
        return result(List.of("Cannot get the game to respond anything other than an end message: " + storedResult), maxGrade() * 1.0 / 3);
    }
}
