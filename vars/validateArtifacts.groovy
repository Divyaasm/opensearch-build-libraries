/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/**
Wrapper that runs validation.sh script with provided args.
@param Map[<any>] <Required> - Any arguments that you want to be passed to validation.sh script. eg: version: 1.0.0 will be passed as --version 1.0.0
*/
void call(Map args = [:]) {
    if (!fileExists("$WORKSPACE/validation.sh")) {
        println("Validation.sh script not found in the workspace: ${WORKSPACE}, exit 1")
        System.exit(1)
    }

    sh([
        './validation.sh',
        args.version ? "--version ${args.version}" : null,
        args.file_path ? "--file-path ${args.file_path}" : null,
        args.distribution ? "--distribution ${args.distribution}" : null,
        args.platform ? "--platform ${args.platform}" : null,
        args.arch ? "--arch ${args.arch}" : null,
        args.projects ? "--projects ${args.projects}" : null,
        args.docker_source ? "--docker-source ${args.docker_source}" : null,
        args.os_build_number ? "--os-build-number ${args.os_build_number}" : null,
        args.osd_build_number ? "--osd-build-number ${args.osd_build_number}" : null,
        args.artifact_type ? "--artifact-type ${args.artifact_type}" : null,
        args.allow_http ? '--allow_http' : null,
        args.docker_args ? "${args.artifact_type}" : null,
    ].join(' '))
}
