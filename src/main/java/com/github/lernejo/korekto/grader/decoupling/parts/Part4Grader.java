package com.github.lernejo.korekto.grader.decoupling.parts;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.InteractiveProcess;
import org.jetbrains.annotations.NotNull;

public class Part4Grader implements PartGrader<LaunchingContext> {
    static final Set<String> endKeywords = Set.of("done", "end", "win", "won", "fin", "bravo", "gagné", "trouvé", "found");
    static final Set<String> lowerKeywords = Set.of("petit", "lower", "smaller");
    static final Set<String> greaterKeywords = Set.of("grand", "greater", "bigger");
    static final Set<String> decisionKeywords = Stream.concat(
        lowerKeywords.stream(),
        greaterKeywords.stream()
    ).collect(Collectors.toSet());

    static final String TOKENIZE_REGEX = "\\s|\\p{Punct}|\n";

    @NotNull
    @Override
    public String name() {
        return "Part 4 - Human Player";
    }

    @NotNull
    @Override
    public Double maxGrade() {
        return 4.0D;
    }

    @NotNull
    @Override
    public GradePart grade(LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        String mainClass = "fr.lernejo.guessgame.Launcher";
        context.processBuilder.command(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString(),
            "-cp", context.getExercise().getRoot().resolve("target").resolve("classes").toString(), mainClass, "-interactive");

        int attempt = 0;
        String storedResult = "<no output>";
        do {
            attempt++;
            try (InteractiveProcess process = new InteractiveProcess(context.processBuilder.start())) {
                String welcome = process.read();// optional welcome message
                int min = 0;
                int max = 100;
                int nextGuess = (max + min) / 2;
                process.write(nextGuess + "\n");

                String result = process.read();
                if (!process.getProcess().isAlive()) {
                    String error = process.readErr();
                    if (error == null) {
                        error = result;
                    }
                    return result(List.of("The program stopped before the game was finished: " + error), 0);
                }
                if (result == null) {
                    String error = process.readErr();
                    error = error != null ? "\n\t" + error : "";
                    return result(List.of("No information given to player after submitting a guess (waited for" + process.getProcessReadTimeout() + "ms)" + error), maxGrade() * 1.0 / 3);
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

                    List<String> scopedResult2 = List.of(result.toLowerCase().split(TOKENIZE_REGEX));
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
                    process.write(nextGuess + "\n");
                    result = process.read();
                    if (result == null) {
                        return result(List.of("Cannot get an output in " + process.getProcessReadTimeout() + " ms, last output was: `" + storedResult + "`"), maxGrade() * 1.0 / 3);
                    }
                    storedResult = result;
                } while (currentIteration < maxLoop);
                // Factorize that,the only difference is the way the numbers are going (according to the use of the passive form or not)
                currentIteration = 0;
                min = 0;
                max = 100;
                nextGuess = (max + min) / 2;
                do {
                    currentIteration++;

                    List<String> scopedResult2 = List.of(result.toLowerCase().split(TOKENIZE_REGEX));
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
                    process.write(nextGuess + "\n");
                    result = process.read();
                    if (result == null) {
                        return result(List.of("Cannot get an output in " + process.getProcessReadTimeout() + " ms, last output was: `" + storedResult + "`"), maxGrade() * 1.0 / 3);
                    }
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
