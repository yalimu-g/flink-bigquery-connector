/*
 * Copyright (C) 2024 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.flink.bigquery.sink;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.connector.base.DeliveryGuarantee;

import com.google.cloud.flink.bigquery.common.config.BigQueryConnectOptions;
import com.google.cloud.flink.bigquery.sink.serializer.FakeBigQuerySerializer;
import com.google.cloud.flink.bigquery.sink.serializer.TestBigQuerySchemas;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Tests for {@link BigQueryIndirectSink}. */
public class BigQueryIndirectSinkTest {

    @Rule public final TemporaryFolder tmp = new TemporaryFolder();

    private static BigQueryConnectOptions connectOptions() {
        return BigQueryConnectOptions.builder()
                .setProjectId("test-project")
                .setDataset("test-dataset")
                .setTable("test-table")
                .build();
    }

    private static BigQuerySinkConfig<Object> createConfig(
            String temporaryGcsBucket, DeliveryGuarantee guarantee) {
        return BigQuerySinkConfig.newBuilder()
                .connectOptions(connectOptions())
                .schemaProvider(TestBigQuerySchemas.getSimpleRecordSchema())
                .serializer(new FakeBigQuerySerializer())
                .temporaryGcsBucket(temporaryGcsBucket)
                .deliveryGuarantee(guarantee)
                .build();
    }

    @Test
    public void testConstructorRequiresTemporaryGcsBucket() {
        BigQuerySinkConfig<Object> configNullBucket =
                createConfig(null, DeliveryGuarantee.AT_LEAST_ONCE);
        assertThatThrownBy(() -> new BigQueryIndirectSink<>(configNullBucket))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "temporaryGcsBucket option must be specified for indirect write mode");

        BigQuerySinkConfig<Object> configEmptyBucket =
                createConfig("", DeliveryGuarantee.AT_LEAST_ONCE);
        assertThatThrownBy(() -> new BigQueryIndirectSink<>(configEmptyBucket))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "temporaryGcsBucket option must be specified for indirect write mode");
    }

    @Test
    public void testCreateIndirectSink() {
        String bucketUri = tmp.getRoot().toURI().toString();
        BigQuerySinkConfig<Object> config =
                createConfig(bucketUri, DeliveryGuarantee.AT_LEAST_ONCE);
        BigQueryIndirectSink<Object> sink = new BigQueryIndirectSink<>(config);

        assertNotNull(sink.getCommittableSerializer());
    }

    @Test
    public void testBigQuerySinkGetSelectsIndirectSink() {
        String bucketUri = tmp.getRoot().toURI().toString();
        BigQuerySinkConfig<Object> indirectConfig =
                createConfig(bucketUri, DeliveryGuarantee.AT_LEAST_ONCE);
        Sink<Object> indirectSink = BigQuerySink.get(indirectConfig);
        assertTrue(indirectSink instanceof BigQueryIndirectSink);
    }

    @Test
    public void testBigQuerySinkGetSelectsDefaultSinkWhenBucketNotSpecified() {
        BigQuerySinkConfig<Object> directAtLeastOnceConfig =
                createConfig(null, DeliveryGuarantee.AT_LEAST_ONCE);
        Sink<Object> directSink = BigQuerySink.get(directAtLeastOnceConfig);
        assertTrue(directSink instanceof BigQueryDefaultSink);
    }
}
