/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazon.connector.s3.io.logical.parquet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.amazon.connector.s3.io.physical.plan.IOPlan;
import com.amazon.connector.s3.request.Range;
import java.util.List;
import org.mockito.ArgumentMatcher;

class IOPlanMatcher implements ArgumentMatcher<IOPlan> {
  private final List<Range> expectedRanges;

  public IOPlanMatcher(List<Range> expectedRanges) {
    this.expectedRanges = expectedRanges;
  }

  @Override
  public boolean matches(IOPlan argument) {
    assertArrayEquals(argument.getPrefetchRanges().toArray(), expectedRanges.toArray());
    return true;
  }
}
