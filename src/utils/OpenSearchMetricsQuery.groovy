/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package utils

import groovy.json.JsonSlurper

class OpenSearchMetricsQuery {
    String metricsUrl
    String awsAccessKey
    String awsSecretKey
    String awsSessionToken
    def script
    def sh

    OpenSearchMetricsQuery(String metricsUrl, String awsAccessKey, String awsSecretKey, String awsSessionToken, def script, def sh) {
        this.metricsUrl = metricsUrl
        this.awsAccessKey = awsAccessKey
        this.awsSecretKey = awsSecretKey
        this.awsSessionToken = awsSessionToken
        this.script = script
        this.sh = sh
    }

    // Ensure the alias `gradle-check` is created targeting all the gradle-check-* indices.
    def fetchMetrics(String query) {
        this.script.println('Running query: '+ query)
        this.script.println('Called again')
        def curlCommand = """
            set -e
            set +x
            MONTH_YEAR=\$(date +"%m-%Y")
            curl -s -XGET "${metricsUrl}/gradle-check/_search" --aws-sigv4 "aws:amz:us-east-1:es" --user "${awsAccessKey}:${awsSecretKey}" -H "x-amz-security-token:${awsSessionToken}" -H 'Content-Type: application/json' -d "${query}" | jq '.'
        """
        String output = sh(script: curlCommand, returnStdout: true).trim()
        this.script.println('Response '+ output)
        return new JsonSlurper().parseText(response)
    }
}
