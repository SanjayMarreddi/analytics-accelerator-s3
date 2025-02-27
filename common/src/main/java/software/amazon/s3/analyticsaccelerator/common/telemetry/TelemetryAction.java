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
package software.amazon.s3.analyticsaccelerator.common.telemetry;

/** A functional interface that mimics {@link Runnable}, but allows exceptions to be thrown. */
@FunctionalInterface
public interface TelemetryAction {
  /**
   * Functional representation of the code that takes no parameters and returns no value. The code
   * is allowed to throw any exception.
   *
   * @throws Exception on error condition.
   */
  void apply() throws Throwable;
}
