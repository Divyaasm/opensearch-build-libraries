/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package jenkins.tests

import org.junit.Test

class TestTestManifest extends BuildPipelineTest {
    @Test
    void testTestManifest() {
        helper.registerAllowedMethod("git", [Map])
        super.testPipeline("tests/jenkins/jobs/TestManifest_Jenkinsfile")
    }
}
