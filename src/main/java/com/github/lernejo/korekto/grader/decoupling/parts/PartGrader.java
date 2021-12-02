package com.github.lernejo.korekto.grader.decoupling.parts;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.lernejo.korekto.grader.decoupling.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SubjectForToolkitInclusion
public interface PartGrader {

    long processReadTimeout = Long.parseLong(System.getProperty("PROCESS_READ_TIMEOUT", "400"));
    long processReadRetryDelay = Long.parseLong(System.getProperty("PROCESS_READ_RETRY_DELAY", "50"));

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

    @SubjectForToolkitInclusion
    default String readOutput(Process process) {
        return readStream(process.getInputStream());
    }

    default String readStream(InputStream inputStream) {
        long start = System.currentTimeMillis();
        do {
            try {
                TimeUnit.MILLISECONDS.sleep(processReadRetryDelay);
                StringBuilder sb = new StringBuilder();
                while (inputStream.available() > 0) {
                    byte[] bytes = inputStream.readNBytes(inputStream.available());
                    UniversalDetector detector = new UniversalDetector();
                    detector.handleData(bytes);
                    detector.dataEnd();
                    String detectedCharset = detector.getDetectedCharset();
                    sb.append(new String(bytes, detectedCharset != null ? Charset.forName(detectedCharset) : StandardCharsets.UTF_8));
                }
                String lineOutput = sb.toString().trim();
                if (lineOutput.length() == 0) {
                    continue;
                }
                return lineOutput;
            } catch (IOException | InterruptedException e) {
                LOGGER.warn("Unable to read process output: " + e.getMessage());
                return null;
            }
        } while (System.currentTimeMillis() - start < processReadTimeout);
        LOGGER.warn("No process output to read in " + processReadTimeout + " ms");
        return null;
    }

    @SubjectForToolkitInclusion
    default void writeInput(Process process, String s) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(s);
            writer.flush();
            TimeUnit.MILLISECONDS.sleep(100L);
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Unable to write to process input: " + e.getMessage());
        }
    }
}
