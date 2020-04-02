package hipravin.samples.elastic;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public class WriteReadBenchmarkResult {
    private String id;
    private String threadName;

    private int nThreads;
    private long writeMillis;
    private long readMillis;
    private long fileSizeBytes;

    private boolean failed = false;
    private String errorMessage = "";

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public WriteReadBenchmarkResult withNThreads(int nThreads) {
        this.nThreads = nThreads;
        return this;
    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public long getWriteMillis() {
        return writeMillis;
    }

    public void setWriteMillis(long writeMillis) {
        this.writeMillis = writeMillis;
    }

    public long getReadMillis() {
        return readMillis;
    }

    public void setReadMillis(long readMillis) {
        this.readMillis = readMillis;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WriteReadBenchmarkResult{" +
                "id='" + id + '\'' +
                ", threadName='" + threadName + '\'' +
                ", writeMillis=" + writeMillis +
                ", readMillis=" + readMillis +
                ", fileSizeBytes=" + fileSizeBytes +
                ", failed=" + failed +
                ", errorMessage='" + errorMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
