package software.amazon.s3.analyticsaccelerator.access;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.s3.analyticsaccelerator.S3SdkObjectClient;
import software.amazon.s3.analyticsaccelerator.S3SeekableInputStream;
import software.amazon.s3.analyticsaccelerator.S3SeekableInputStreamConfiguration;
import software.amazon.s3.analyticsaccelerator.S3SeekableInputStreamFactory;
import software.amazon.s3.analyticsaccelerator.util.InputPolicy;
import software.amazon.s3.analyticsaccelerator.util.OpenStreamInformation;
import software.amazon.s3.analyticsaccelerator.util.S3URI;

public class SingleObjectReadTest extends IntegrationTestBase {
    private static final String AVRO_FILE = "0005df6e-df31-4b23-adbf-12f17bcbd79a-m1682.avro";
    private static final int ITERATIONS = 10;
    private static final int TOTAL_TESTS = 2;
    private static final int TOTAL_SIZE = 8_388_608; // 8 MB

    private static final Map<String, List<Double>> results = new ConcurrentHashMap<>();
    private static final AtomicInteger completedTests = new AtomicInteger(0);

    private static class Config {
        final String name;
        final OpenStreamInformation streamInfo;

        Config(String name, OpenStreamInformation streamInfo) {
            this.name = name;
            this.streamInfo = streamInfo;
        }
    }

    private static final Config SEQUENTIAL = new Config("Sequential",
            OpenStreamInformation.builder().inputPolicy(InputPolicy.Sequential).build());
    private static final Config DEFAULT = new Config("Default", OpenStreamInformation.DEFAULT);

    @ParameterizedTest
    @MethodSource("provideTestParameters")
    void testAvroFileRead(Config config) throws IOException {
        System.out.printf("\nStarting test with: Stream=%s, Iterations=%d%n", config.name, ITERATIONS);

        S3AsyncClient s3AsyncClient = S3AsyncClient.crtBuilder().maxConcurrency(600).region(Region.US_EAST_2).build();
        S3URI avroURI = S3URI.of(getS3ExecutionContext().getConfiguration().getBucket(),
                getS3ExecutionContext().getConfiguration().getPrefix() + AVRO_FILE);

        // Actual test runs
        List<Double> times = results.computeIfAbsent(config.name, k -> new ArrayList<>());
        for (int i = 0; i < ITERATIONS; i++) {
            System.out.printf("Iteration %d/%d%n", i + 1, ITERATIONS);
            double time = executeReads(avroURI, s3AsyncClient, config);
            times.add(time);
        }

        if (completedTests.incrementAndGet() == TOTAL_TESTS) {
            printResults();
        }
    }

    private double executeReads(S3URI avroURI, S3AsyncClient s3AsyncClient, Config config) throws IOException {
        S3SeekableInputStreamFactory factory = new S3SeekableInputStreamFactory(
                new S3SdkObjectClient(s3AsyncClient), S3SeekableInputStreamConfiguration.DEFAULT);
        try (S3SeekableInputStream stream = factory.createStream(avroURI, config.streamInfo)) {
            long start = System.nanoTime();
            byte[] buffer = new byte[TOTAL_SIZE];
            stream.read(buffer);
            return (System.nanoTime() - start) / 1_000_000.0; // Convert to milliseconds
        }
    }

    private void printResults() {
        System.out.println("\n=== Performance Results (milliseconds) ===");
        System.out.printf("%-12s %-10s %-10s %-10s %-10s%n", "Stream", "Mean", "Median", "StdDev", "StdDev%");

        results.forEach((name, times) -> {
            DoubleSummaryStatistics stats = times.stream().mapToDouble(d -> d).summaryStatistics();
            double mean = stats.getAverage();
            double median = calculateMedian(times);
            double stdDev = calculateStdDev(times, mean);
            double stdDevPercent = (stdDev / mean) * 100;

            System.out.printf("%-12s %-10.2f %-10.2f %-10.2f %.2f%%%n",
                    name, mean, median, stdDev, stdDevPercent);
        });
    }

    private static double calculateMedian(List<Double> times) {
        Collections.sort(times);
        int middle = times.size() / 2;
        return times.size() % 2 == 0 ? (times.get(middle - 1) + times.get(middle)) / 2 : times.get(middle);
    }

    private static double calculateStdDev(List<Double> times, double mean) {
        return Math.sqrt(times.stream().mapToDouble(time -> Math.pow(time - mean, 2)).average().orElse(0));
    }

    private static Stream<Arguments> provideTestParameters() {
        return Stream.of(SEQUENTIAL, DEFAULT).map(Arguments::of);
    }
}
