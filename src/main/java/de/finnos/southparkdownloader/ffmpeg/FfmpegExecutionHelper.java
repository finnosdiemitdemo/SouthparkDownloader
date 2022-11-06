package de.finnos.southparkdownloader.ffmpeg;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.processes.FfmpegProcess;
import de.finnos.southparkdownloader.processes.ThreadProcess;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;

public class FfmpegExecutionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(FfmpegExecutionHelper.class);

    public static Optional<Process> executeCommand(final String executionFilePath, final String command) {
        final File ffmpegFile = new File(executionFilePath);
        if (!ffmpegFile.isFile()) {
            Helper.showErrorMessage(null, "Der angegbene FFmpeg-Pfad ist nicht korrekt: " + executionFilePath);
        } else {
            try {
                final ProcessBuilder builder = new ProcessBuilder((ffmpegFile.getAbsolutePath() + " " + command).split(" "));
                builder.redirectErrorStream(true);
                final var process = builder.start();
                return Optional.of(process);
            } catch (IOException e) {
                Helper.showErrorMessage(null, e.getMessage());
            }
        }

        return Optional.empty();
    }

    public static FfmpegProcess executeCommandAsync(final String executionFilePath, final String command) {
        final var optProcess = executeCommand(executionFilePath, command);
        if (optProcess.isPresent()) {
            final var process = optProcess.get();
            final ThreadProcess progressThread = new ThreadProcess() {
                @Override
                public void execute() {
                    final var inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    final var errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    final BiConsumer<BufferedReader, String> handleStream = (bufferedReader, logPrefix) -> {
                        try {
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                final String finalLine = logPrefix + line;

                                getProgressEvent().updateProgress(finalLine);
                                LOG.trace(finalLine);
                            }
                        } catch (IOException e) {
                            Helper.showErrorMessage(null, e.getMessage());
                        }
                    };

                    handleStream.accept(inputStream, "[OUTPUT] ");
                    handleStream.accept(errorStream, "[ERROR] ");
                }
            };

            return new FfmpegProcess(process, progressThread);
        }

        return null;
    }

    public static String executeCommandSync (final String executionFilePath, final String command) {
        final var optProcess = executeCommand(executionFilePath, command);
        if (optProcess.isPresent()) {
            final var process = optProcess.get();
            StringBuilder result = new StringBuilder();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (IOException e) {
                Helper.showErrorMessage(null, e.getMessage());
            }

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                LOG.error("ffmpeg process execution interrupted");
                return "";
            }

            return result.toString();
        }
        return "";
    }

    public static String executeSyncFfmpegCommand(final String command) {
        return executeCommandSync(getAndValidateFfmpegExecutable(), command);
    }

    public static FfmpegProcess executeAsyncFfmpegCommand(final String command) {
        return executeCommandAsync(getAndValidateFfmpegExecutable(), command);
    }

    public static String executeSyncFfprobeCommand(final String command) {
        return executeCommandSync(getAndValidateFfprobeExecutable(), command);
    }

    public static FfmpegProcess executeAsyncFfprobeCommand(final String command) {
        return executeCommandAsync(getAndValidateFfprobeExecutable(), command);
    }

    public static String getAndValidateFfmpegExecutable () {
        final var ffmpegFilePath = Config.get().getFfmpegFilePath();
        validateExecutablePath(ffmpegFilePath, "FFmpeg");
        return ffmpegFilePath;
    }

    public static String getAndValidateFfprobeExecutable () {
        final var ffmpegFilePath = Config.get().getFfprobeFilePath();
        validateExecutablePath(ffmpegFilePath, "FFprobe");
        return ffmpegFilePath;
    }

    public static void validateExecutablePath(final String pathString, final String name) {
        final var path = Path.of(pathString);
        if (!isFFmpegFileExecutable(pathString)) {
            Helper.showErrorMessage(null, I18N.i18n("invalid_ffmpeg_executable").formatted(name, path));
            throw new FFmpegExecutablesException("Unable to execute " + pathString);
        }
    }

    public static boolean isFFmpegFileExecutable (final String pathString) {
        final var path = Path.of(pathString);
        final var extension = FilenameUtils.getExtension(pathString);
        return Files.exists(path) && !Files.isDirectory(path) && Files.isExecutable(path) && (extension.isBlank() || extension.equals("exe"));
    }

    public static class FFmpegExecutablesException extends RuntimeException {
        public FFmpegExecutablesException(final String message) {
            super(message);
        }
    }
}
