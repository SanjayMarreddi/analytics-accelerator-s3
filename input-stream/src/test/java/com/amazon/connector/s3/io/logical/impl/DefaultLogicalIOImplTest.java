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
package com.amazon.connector.s3.io.logical.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazon.connector.s3.io.physical.PhysicalIO;
import com.amazon.connector.s3.util.S3URI;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@SuppressFBWarnings(
    value = "NP_NONNULL_PARAM_VIOLATION",
    justification = "We mean to pass nulls to checks")
public class DefaultLogicalIOImplTest {
  private static final S3URI TEST_URI = S3URI.of("foo", "bar");

  @Test
  void testConstructor() {
    assertNotNull(new DefaultLogicalIOImpl(mock(PhysicalIO.class)));
  }

  @Test
  void testConstructorThrowsOnNullArgument() {
    assertThrows(NullPointerException.class, () -> new DefaultLogicalIOImpl(null));
  }

  @Test
  void testCloseDependencies() throws IOException {
    // Given
    PhysicalIO physicalIO = mock(PhysicalIO.class);
    DefaultLogicalIOImpl logicalIO = new DefaultLogicalIOImpl(physicalIO);

    // When: close called
    logicalIO.close();

    // Then: close will close dependencies
    verify(physicalIO, times(1)).close();
  }

  @Test
  void testRead() throws IOException {
    PhysicalIO physicalIO = mock(PhysicalIO.class);
    DefaultLogicalIOImpl logicalIO = new DefaultLogicalIOImpl(physicalIO);

    logicalIO.read(5);
    verify(physicalIO).read(5);
  }

  @Test
  void testReadWithBuffer() throws IOException {
    PhysicalIO physicalIO = mock(PhysicalIO.class);
    DefaultLogicalIOImpl logicalIO = new DefaultLogicalIOImpl(physicalIO);

    byte[] buffer = new byte[5];
    logicalIO.read(buffer, 0, 5, 5);
    verify(physicalIO).read(buffer, 0, 5, 5L);
  }

  @Test
  void testReadTail() throws IOException {
    PhysicalIO physicalIO = mock(PhysicalIO.class);
    DefaultLogicalIOImpl logicalIO = new DefaultLogicalIOImpl(physicalIO);

    byte[] buffer = new byte[5];
    logicalIO.readTail(buffer, 0, 5);
    verify(physicalIO).readTail(buffer, 0, 5);
  }
}
