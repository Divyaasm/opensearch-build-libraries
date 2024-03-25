/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import jenkins.tests.BuildPipelineTest
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.hasItem
import static org.hamcrest.MatcherAssert.assertThat

class TestRunBenchmarkTestScriptWithEndpoint extends BuildPipelineTest {

    @Before
    void setUp() {
        this.registerLibTester(new RunBenchmarkTestScriptLibTester(
                '',
                '',
                '',
                'opensearch-ABCxdfdfhyfk.com',
                'true',
                'nyc_taxis',
                'true',
                'false',
                'true',
                'false',
                '',
                '',
                'r5.8xlarge',
                '',
                '',
                'custom-test-procedure',
                '',
                '',
                'cluster.indices.replication.strategy:SEGMENT',
                'false',
                'true',
                ''
        ))
        super.setUp()
    }

    @Test
    public void testRunBenchmarkTestScript_PipelineSingleNode() {
        super.testPipeline("tests/jenkins/jobs/BenchmarkTestWithEndpoint_Jenkinsfile")
    }

    @Test
    void testRunBenchmarkTestScript_verifyScriptExecutionsNoManifest() {
        runScript("tests/jenkins/jobs/BenchmarkTestWithEndpoint_Jenkinsfile")

        def testScriptCommands = getCommandExecutions('sh', './test.sh').findAll {
            shCommand -> shCommand.contains('./test.sh')
        }

        assertThat(testScriptCommands.size(), equalTo(1))
        assertThat(testScriptCommands, hasItem(
                "./test.sh benchmark-test    --cluster-endpoint opensearch-ABCxdfdfhyfk.com  --workload nyc_taxis --benchmark-config /tmp/workspace/benchmark.ini --user-tag security-enabled:true  --single-node  --use-50-percent-heap   --capture-segment-replication-stat --suffix 307-secure      --data-instance-type r5.8xlarge  --test-procedure custom-test-procedure   --additional-config cluster.indices.replication.strategy:SEGMENT --data-node-storage 200 --ml-node-storage 200  "
        ))
    }

    def getCommandExecutions(methodName, command) {
        def shCommands = helper.callStack.findAll {
            call ->
                call.methodName == methodName
        }.
                collect {
                    call ->
                        callArgsToString(call)
                }.findAll {
            shCommand ->
                shCommand.contains(command)
        }

        return shCommands
    }
}
