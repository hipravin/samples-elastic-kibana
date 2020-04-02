package hipravin.samples.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ElasticIndexService {
    private Logger log = LoggerFactory.getLogger(ElasticIndexService.class);

    @Value("${elasticsearch.bulk:1000}")
    private long bulkIndexBatchSize;

    @Autowired
    RestHighLevelClient client;

    public void indexAll(Stream<WriteReadBenchmarkResult> benchmarkResults) {
        final List<WriteReadBenchmarkResult> indexBatch = new ArrayList<>();

        benchmarkResults.forEach(t -> {
            indexBatch.add(t);
            if (indexBatch.size() == bulkIndexBatchSize) {
                indexBatch(indexBatch);
                indexBatch.clear();
            }
        });
        indexBatch(indexBatch);
    }

    private void indexBatch(List<WriteReadBenchmarkResult> batch) {
        if (batch.isEmpty()) {
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            for (WriteReadBenchmarkResult writeReadBenchmarkResult : batch) {
                IndexRequest indexSingleRequest = new IndexRequest("samples-writrateread");
                indexSingleRequest.id(writeReadBenchmarkResult.getId());
                indexSingleRequest.source(mapper.writeValueAsString(writeReadBenchmarkResult), XContentType.JSON);

                bulkRequest.add(indexSingleRequest);
            }

            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            logResponse(bulkResponse);

        } catch (ActionRequestValidationException | IOException e) {
            throw new IllegalStateException("Failed to index " + e.getMessage(), e);
        }
    }

    private void logResponse(BulkResponse bulkResponse) {
        log.info("Elastic bulk response status: {}", bulkResponse.status());

        if (bulkResponse.hasFailures()) {
            log.error("Failures in bulk ");
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                log.error(bulkItemResponse.getFailureMessage());
            }
        }
    }
}