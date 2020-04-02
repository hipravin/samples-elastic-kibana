package hipravin.samples.elastic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileWriteReadRandomTask {

    private final Path tempFile;
    private final long lineCount;

    public FileWriteReadRandomTask(Path tempFile, long lineCount) {
        this.tempFile = tempFile;
        this.lineCount = lineCount;
    }

    public WriteReadBenchmarkResult writeAndRead() {
        WriteReadBenchmarkResult result = new WriteReadBenchmarkResult();

        try {
            if (Files.exists(tempFile)) {
                throw new IOException("File " + tempFile + " already exists");
            }
            String randomLine = UUID.randomUUID().toString();

            ioWithBenchmark(() -> {
                Stream<String> lines = Stream.generate(() -> randomLine).limit(lineCount);
                Files.write(tempFile, (Iterable<String>) lines::iterator, StandardCharsets.UTF_8);
            }, result::setWriteMillis);

            ioWithBenchmark(() -> {
                try (Stream<String> fileLines = Files.lines(tempFile, StandardCharsets.UTF_8)) {
                    long fileLineCount = fileLines
                            .filter(randomLine::equals)
                            .count();

                    if (lineCount != fileLineCount) {
                        throw new IOException("File is broken: " + tempFile);
                    }
                }
            }, result::setReadMillis);

            result.setFileSizeBytes(Files.size(tempFile));
            String currentThreadName = Thread.currentThread().getName();
            result.setId(currentThreadName + "-" + tempFile.getFileName() + "-" + result.getTimestamp().toInstant().toEpochMilli());
            result.setThreadName(currentThreadName);
            Files.delete(tempFile);
        } catch (IOException e) {
            result.setErrorMessage(e.getMessage());
            result.setFailed(true);
        }

        return result;
    }

    public static void ioWithBenchmark(ThrowingOperation<IOException> operation,
                                       Consumer<Long> updateDurationMillis) throws IOException {
        Temporal start = Instant.now();
        try {
            operation.perform();
        } finally {
            updateDurationMillis.accept(Duration.between(start, Instant.now()).toMillis());
        }
    }
}
