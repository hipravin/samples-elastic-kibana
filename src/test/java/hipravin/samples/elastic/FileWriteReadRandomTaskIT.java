package hipravin.samples.elastic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@SpringBootTest
class FileWriteReadRandomTaskIT {

    String tempDir = ".";

    @Autowired
    ElasticIndexService elasticIndexService;

    AtomicInteger tasksSubmitted = new AtomicInteger(0);
    AtomicInteger tasksDone = new AtomicInteger(0);

    LinkedBlockingQueue<WriteReadBenchmarkResult> resultQueue = new LinkedBlockingQueue<>();

    @Test
    void testWriteReadIterative() {
        ForkJoinPool.commonPool().submit(benchmarkConsumer());

        for (int nthreads = 1; nthreads < 30; nthreads++) {
            testFixedNumberNThreads(nthreads, 300);
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Runnable benchmarkConsumer() {
        return  () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WriteReadBenchmarkResult b = resultQueue.take();
                    List<WriteReadBenchmarkResult> batch = new ArrayList<>();
                    resultQueue.drainTo(batch);
                    batch.add(b);
                    elasticIndexService.indexAll(batch.stream());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    void testFixedNumberNThreads(int nthreads, int wrCount) {

        AtomicInteger threadCounter = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(nthreads,
                r -> new Thread(r, "wrthread-" + threadCounter.getAndIncrement()));

        for (int i = 0; i < wrCount; i++) {
            final Path tempFile = Paths.get(tempDir, i + ".txt");
            CompletableFuture.supplyAsync(() -> {
                tasksSubmitted.incrementAndGet();
                FileWriteReadRandomTask task = new FileWriteReadRandomTask(
                        tempFile, 1_000_000);
                return task.writeAndRead().withNThreads(nthreads);
            }, executorService).thenAccept(r -> {
                resultQueue.add(r);
                tasksDone.incrementAndGet();
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(600, TimeUnit.SECONDS);
            List<Runnable> shutNow = executorService.shutdownNow();
            System.out.println("Still running:" + shutNow.size());
            executorService.awaitTermination(15, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSimpleReadWrite() throws IOException {

        FileWriteReadRandomTask task = new FileWriteReadRandomTask(
                Paths.get(".", "1.txt"), 3_000_000);
        WriteReadBenchmarkResult b = task.writeAndRead();

        elasticIndexService.indexAll(Stream.of(b));
        System.out.println(b);
    }
}