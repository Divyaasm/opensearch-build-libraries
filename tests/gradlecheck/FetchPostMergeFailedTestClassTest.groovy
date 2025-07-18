/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package gradlecheck.tests

import gradlecheck.FetchPostMergeFailedTestClass
import org.junit.Before
import org.junit.Test
import groovy.json.JsonOutput

class FetchPostMergeFailedTestClassTest {

    private FetchPostMergeFailedTestClass fetchPostMergeFailedTestClass
    private final String metricsUrl = "http://example.com"
    private final String awsAccessKey = "testAccessKey"
    private final String awsSecretKey = "testSecretKey"
    private final String awsSessionToken = "testSessionToken"
    private final String indexName = "gradle-check-*"
    private def script

    @Before
    void setUp() {
        script = new Expando()
        script.sh = { Map args ->
            if (args.containsKey("script")) {
                return """
                {
                    "aggregations": {
                        "test_class_keyword_agg": {
                            "buckets": [
                                {"key": "testClass1"},
                                {"key": "testClass2"}
                            ]
                        }
                    }
                }
                """
            }
            return ""
        }
        fetchPostMergeFailedTestClass = new FetchPostMergeFailedTestClass(metricsUrl, awsAccessKey, awsSecretKey, awsSessionToken, indexName, script)
    }

    @Test
    void testGetQueryReturnsExpectedQuery() {
        def expectedOutput = JsonOutput.toJson([
                size: 200,
                query: [
                        bool: [
                                must: [
                                        [
                                                terms: [
                                                        "invoke_type.keyword": [
                                                                "Post Merge Action",
                                                                "Timer"
                                                        ]
                                                ]
                                        ],
                                        [
                                                match: [
                                                        test_status: [
                                                                query: "FAILED",
                                                                operator: "OR",
                                                                prefix_length: 0,
                                                                max_expansions: 50,
                                                                fuzzy_transpositions: true,
                                                                lenient: false,
                                                                zero_terms_query: "NONE",
                                                                auto_generate_synonyms_phrase_query: true,
                                                                boost: 1
                                                        ]
                                                ]
                                        ]
                                ],
                                filter: [
                                        [
                                                range: [
                                                        build_start_time: [
                                                                from: "now-15d",
                                                                to: "now",
                                                                include_lower: true,
                                                                include_upper: true,
                                                                boost: 1
                                                        ]
                                                ]
                                        ]
                                ],
                                adjust_pure_negative: true,
                                boost: 1
                        ]
                ],
                aggregations: [
                        test_class_keyword_agg: [
                                terms: [
                                        field: "test_class",
                                        size: 500
                                ]
                        ]
                ]
        ]).replace('"', '\\"')

        def result = fetchPostMergeFailedTestClass.getQuery("15d")

        assert result == expectedOutput
    }

    @Test
    void testGetPostMergeFailedTestClassReturnsKeys() {
        def expectedOutput = ["testClass1", "testClass2"]
        def timeFrame = "15d"
        def result = fetchPostMergeFailedTestClass.getPostMergeFailedTestClass(timeFrame)

        assert result == expectedOutput
    }
}
