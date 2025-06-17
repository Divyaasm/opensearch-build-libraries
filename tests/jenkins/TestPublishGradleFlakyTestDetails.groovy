/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins.tests

import org.junit.Before
import groovy.json.JsonSlurper
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date

class TestPublishGradleFlakyTestDetails extends BuildPipelineTest {

    @Overrride
    @Before
    void setUP() {
        super.setUp()
        binding.setVariable('currentBuild', [
            number: 450,
            startTimeInMillis: System.currentTimeMillis(),
            duration: ,
            result: FAILURE
        ])
        binding.setVariable('sh', { cmd -> println cmd })
        binding.setVariable('writeFile', { params -> println params.text })
        binding.setVariable('withCredentials', { creds, closure -> closure() })
        binding.setVariable('currentDate', new Date())
        binding.setVariable('formattedDate', new SimpleDateFormat("MM-yyyy").format(currentDate))
        helper.registerAllowedMethod("withAWS", [Map, Closure], { args, closure ->
            closure.delegate = delegate
            return helper.callClosure(closure)
        })
        binding.setVariable('curl', { params -> println params })
    }

    @Test
    void testIndexFailedTestData() {

        def indexName = 'gradle-test-test'
        def testRecordsFile = 'failed-test-records.ndjson'

        def script = loadScript('vars/publishGradleFlakyTestDetails')

        def calledCommands = new arrayList()
        script.metaClass.sh = { String command ->
            calledCommands << command
            if (command.contains("curl -I")) {
                return "HTTP/1.1 200 OK"
            } else if (command.contains("curl -s -XPUT") && command.contains("test-index")) {
                return '{"acknowledged":true}'
            } else if (command.contains("curl -XPOST") && command.contains("test-index")) {
                return '{"took":10, "errors":false}'
            } else {
                throw new IllegalArgumentException("Unexpected command: $command")
            }
        }

        script.indexFailedTestData(indexName, testRecordsFile)
        def expectedCommandBlock = sh '''
                set -e
                set -x

                MONTH_YEAR=\$(date +"%m-%Y")
                INDEX_NAME="gradle-test-flaky-\$MONTH_YEAR"
                INDEX_MAPPING='{
                    "mappings": {
                        "properties": {
                            "build_duration": {
                                "type": "float"
                            },
                            "build_number": {
                                "type": "integer"
                            },
                            "build_result": {
                                "type": "keyword"
                            },
                            "build_start_time": {
                                "type": "date"
                            },
                            "test_class": {
                                "type": "keyword"
                            },
                            "test_fail_count": {
                                "type": "integer"
                            },
                            "test_name": {
                                "type": "keyword"
                            },
                            "test_passed_count": {
                                "type": "integer"
                            },
                            "test_skipped_count": {
                                "type": "integer"
                            },
                            "test_status": {
                                "type": "keyword"
                            }
                        }
                    }
                }'
                echo "INDEX NAME IS \$INDEX_NAME"
                curl -I "${METRICS_HOST_URL}/\$INDEX_NAME" --aws-sigv4 \"aws:amz:us-east-1:es\" --user \"${awsAccessKey}:${awsSecretKey}\" -H \"x-amz-security-token:${awsSessionToken}\" | grep -E "HTTP\\/[0-9]+(\\.[0-9]+)? 200"
                if [ \$? -eq 0 ]; then
                    echo "Index already exists. Indexing Results"
                else
                    echo "Index does not exist. Creating..."
                    create_index_response=\$(curl -s -XPUT "${METRICS_HOST_URL}/\${INDEX_NAME}" --aws-sigv4 \"aws:amz:us-east-1:es\" --user \"${awsAccessKey}:${awsSecretKey}\" -H \"x-amz-security-token:${awsSessionToken}\" -H 'Content-Type: application/json' -d "\${INDEX_MAPPING}")
                    if echo "\$create_index_response" | grep -q '"acknowledged":true'; then
                        echo "Index created successfully."
                        echo "Updating alias..."
                        update_alias_response=\$(curl -s -XPOST "${METRICS_HOST_URL}/_aliases" --aws-sigv4 \"aws:amz:us-east-1:es\" --user \"${awsAccessKey}:${awsSecretKey}\" -H \"x-amz-security-token:${awsSessionToken}\" -H "Content-Type: application/json" -d '{
                            "actions": [
                                {
                                    "add": {
                                    "index": "\${INDEX_NAME}",
                                    "alias": "gradle-test-flaky"
                                    }
                                }
                            ]
                        }')
                        if echo "\$update_alias_response" | grep -q '"acknowledged":true'; then
                            echo "Alias updated successfully."
                        else
                            echo "Failed to update alias. Error message: \$update_alias_response"
                        fi
                    else
                        echo "Failed to create index. Error message: \$create_index_response"
                        exit 1
                    fi
                fi
                if [ -s failed-test-records.json ]; then
                    echo "File Exists, indexing results."
                    curl -XPOST "${METRICS_HOST_URL}/\$INDEX_NAME/_bulk" --aws-sigv4 \"aws:amz:us-east-1:es\" --user \"${awsAccessKey}:${awsSecretKey}\" -H \"x-amz-security-token:${awsSessionToken}\" -H "Content-Type: application/x-ndjson" --data-binary "@failed-test-records.json"
                else
                    echo "File Does not exist. No failing test records to process."
                fi
        '''
        assert calledCommands.size() == 1
        assert normalizeString(calledCommands[0]) == normalizeString(expectedCommandBlock)
    }

     @Test
    void testGenerateJson() {
        def script = loadScript('vars/publishGradleFlakyTestDetails')
        def result = script.generateJson(
            '450', 'main', 'PemTrustConfigTests', 'org.opensearch.common.ssl.PemTrustConfigTests.testTrustConfigReloadsFileContents',
             'FAILED', 'FAILED', '1', '0', '0', '224724', System.currentTimeMillis()
        )

        def parsedResult = new JsonSlurper().parseText(result)
        def expectedJson = [
            build_number: '450',
            git_reference:'main',
            test_class:'PemTrustConfigTests',
            test_name:'org.opensearch.common.ssl.PemTrustConfigTests.testTrustConfigReloadsFileContents',
            test_status:'FAILED',
            build_result:'FAILURE',
            test_fail_count: '1',
            test_skipped_count: '0',
            test_passed_count: '0',
            build_duration: '224724',
        ]
        // Remove the dynamic field for comparison
        parsedResult.remove('build_start_time')
        assert parsedResult == expectedJson

    }





    }

}