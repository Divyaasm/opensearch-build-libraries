/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/** Library to create/edit/skip Flaky Test Report GitHub issue for OpenSearch repo.
 @param Map args = [:] args A map of the following parameters
 @param args.repoUrl <required> - GitHub repository URL to create issue
 @param args.issueTitle <required> - GitHub issue title
 @param args.issueBody <required> - GitHub issue body
 @param args.label <optional> - GitHub issue label to be attached along with 'untriaged'. Defaults to autocut.
 @param args.issueEdit <optional> - Updates the body of the issue, the default if not passed is to add a comment.
 @param args.issueBodyFile <optional> - GitHub issue body from an `.md` file
 */

import gradlecheck.ParseMarkDownTable
import gradlecheck.MarkdownComparator

void call(Map args = [:]) {
    label = args.label ?: 'autocut,>test-failure,flaky-test'
    try {
//
//        def gh_token = "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"
//
//        sh """
//              echo ${gh_token} | gh auth login --with-token
//        """

        def existingIssueBody = sh(
                script: "gh issue list --repo https://github.com/Divyaasm/opensearch-build -S \"[AUTOCUT] Gradle Check Flaky Test Report for AutoForceMergeManagerTests in:title is:closed\"  --json body --jq '.[0].body'",
                returnStdout: true
        ).trim()

//        println "${existingIssueBody}"

        def existingTable = new ParseMarkDownTable(existingIssueBody).parseMarkdownTableRows()
        println "1"
                def markdownTable = new ParseMarkDownTable(readFile(args.issueBodyFile)).parseMarkdownTableRows()
        println "2"
                def differences = new MarkdownComparator(markdownTable, existingTable).markdownComparison()
        println "3"
                if (!differences) {
                    println("Not Re-opening the issue as the no change in the Flaky report after the issue is closed")
                } else {
                    println "Differences found:"
                    differences.each { diffRow ->
                        println "Git Reference: ${diffRow['Git Reference']}, " +
                                "Merged Pull Request: ${diffRow['Merged Pull Request']}, " +
                                "Build Details: ${diffRow['Build Details']}, " +
                                "Test Name: ${diffRow['Test Name']}"
                    }
                }


    } catch (Exception ex) {
        error("Unable to create GitHub issue for ${args.repoUrl}", ex.getMessage())
    }
}




