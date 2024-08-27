package com.amazon.connector.s3.io.physical.data;

import static com.amazon.connector.s3.util.Constants.ONE_MB;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.connector.s3.io.physical.PhysicalIOConfiguration;
import com.amazon.connector.s3.request.Range;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RangeOptimiserTest {

  @Test
  public void test__splitRanges__smallRangesCauseNoSplit() {
    // Given: small ranges
    RangeOptimiser rangeOptimiser = new RangeOptimiser(PhysicalIOConfiguration.DEFAULT);
    List<Range> ranges = new LinkedList<>();
    ranges.add(new Range(0, 100));
    ranges.add(new Range(200, 300));
    ranges.add(new Range(300, 400));
    ranges.add(new Range(400, 500));

    // When: splitRanges is called
    List<Range> splitRanges = rangeOptimiser.splitRanges(ranges);

    // Then: nothing happens
    assertEquals(ranges, splitRanges);
  }

  @Test
  public void test__splitRanges__bigRangesResultInSplits() {
    // Given: a 16MB range
    RangeOptimiser rangeOptimiser = new RangeOptimiser(PhysicalIOConfiguration.DEFAULT);
    List<Range> ranges = new LinkedList<>();
    ranges.add(new Range(0, 16 * ONE_MB - 1));

    // When: splitRanges is called
    List<Range> splitRanges = rangeOptimiser.splitRanges(ranges);

    // Then: 16MB range is split into 4x4MB ranges
    List<Range> expected = new LinkedList<>();
    expected.add(new Range(0, 8 * ONE_MB - 1));
    expected.add(new Range(8 * ONE_MB, 16 * ONE_MB - 1));
    assertEquals(expected, splitRanges);
  }
}
