/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/** Library to fetch failing tests at the end of gradle-check run and index the results in an OpenSearch cluster.
 *
 * @param Map args = [:] args A map of the following parameters
 * @param args.prNumber <required> - The pull_request number that triggered the gradle-check run. If Null then use post_merge_action string.
 * @param args.prDescription <required> - The subject of the pull_request. If prNumber is null then it signifies push action on branch.
 */

import hudson.tasks.test.AbstractTestResultAction
import groovy.json.JsonOutput
import java.text.SimpleDateFormat

void call(Map args = [:]) {
    def lib = library(identifier: 'jenkins@groovytest', retriever: legacySCM(scm))
    println("Print something")
    def finalJsonDoc = ""
    def buildNumber = currentBuild.number
    def buildDuration = currentBuild.duration
    def buildResult = currentBuild.result
    def buildStartTime = currentBuild.startTimeInMillis
    def gitReference = args.gitReference.toString()
    def formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date())

    def indexName = "gradle-test-flaky-${formattedDate}"
    def test_docs = getFailedTestRecords(buildNumber, gitReference, buildResult, buildDuration, buildStartTime, formattedDate)
    println("${indexName}")
    if (test_docs) {
        println("${indexName}")
        for (doc in test_docs) {
            def jsonDoc = JsonOutput.toJson(doc)
            finalJsonDoc += "{\"index\": {\"_index\": \"${indexName}\"}}\n" + "${jsonDoc}\n"
        }
        writeFile file: "failed-test-records.json", text: finalJsonDoc

        def fileContents = readFile(file: "failed-test-records.json").trim()
        println("File Content is:\n${fileContents}")
        indexFailedTestData()
    }
}

List<Map<String, String>> getFailedTestRecords(buildNumber, gitReference, buildResult, buildDuration, buildStartTime, formattedDate) {
    def testResults = []
    AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    if (testResultAction != null) {
        def testsTotal = testResultAction.totalCount
        def testsFailed = testResultAction.failCount
        def testsSkipped = testResultAction.skipCount
        def testsPassed = testsTotal - testsFailed - testsSkipped
        def failedTests = testResultAction.getFailedTests()

        if (failedTests){
            for (test in failedTests) {
                def failDocument = ['build_number': buildNumber, 'git_reference': gitReference, 'test_class': test.getParent().getName(), 'test_name': test.fullName, 'test_status': 'FAILED', 'build_result': buildResult, 'test_fail_count': testsFailed, 'test_skipped_count': testsSkipped, 'test_passed_count': testsPassed, 'build_duration': buildDuration, 'build_start_time': buildStartTime, 'build_date': formattedDate]
                testResults.add(failDocument)
            }
        } else {
            println("No test failed.")
        }
    }
    println("${testResults}")
    return testResults
}

void indexFailedTestData() {


    sh """
                #!/bin/bash
                
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
                            "git_reference": {
                                "type": "text",
                                "fields": {
                                    "keyword": {
                                        "type": "keyword",
                                        "ignore_above": 256
                                    }
                                }
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
                            },
                            "test_status": {
                                "type": "date"
                            }
                        }
                    }
                }'
                echo "INDEX NAME IS \$INDEX_NAME"

                echo "Index does not exist. Creating..."
                    
                if [ -s failed-test-records.json ]; then
                    echo "File Exists, indexing results."
                fi
        """
}
