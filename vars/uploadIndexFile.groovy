/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
void call(Map args = [:]) {
    def latestBuildData = ['latest': "${BUILD_NUMBER}"]
    writeJSON file: 'index.json', json: latestBuildData

    withCredentials([string(credentialsId: 'test-jenkins-aws-account-public', variable: 'ARTIFACT_BUCKET_NAME')]) {
        echo "Uploading index.json to s3://${ARTIFACT_BUCKET_NAME}/${args.indexFilePath}"

        uploadToS3(
            sourcePath: 'index.json',
            bucket: "${ARTIFACT_BUCKET_NAME}",
            path: "${args.indexFilePath}/index.json"
        )
    }
}
