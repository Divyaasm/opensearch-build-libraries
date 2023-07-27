/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

void call(Map args = [:]) {
    def lib = library(identifier: 'jenkins@dockerpackerlib', retriever: legacySCM(scm))
    String docker_image = "opensearchproject/${args.project}:${args.version}"

    sh"""
    #!/bin/bash
    set -e
    set +x

    docker pull ${docker_image}
    """
    sh """docker inspect --format '{{ index .Config.Labels "org.label-schema.version"}}' ${docker_image} > versionnumber"""

    sh """docker inspect --format '{{ index .Config.Labels "org.label-schema.build-date"}}' ${docker_image} > time"""

    sh """docker inspect --format '{{ index .Config.Labels "org.label-schema.description"}}' ${docker_image} > number"""

    version = readFile('versionnumber').trim()
    build_time = readFile('time').trim()
    build_number = readFile('number').trim()

    echo "${version} ${build_time} ${build_number}"

    def inputManifest = lib.jenkins.InputManifest.new(readYaml(file: "manifests/${version}/${args.project}-${version}.yml"))

    artifactUrlX64 = "https://ci.opensearch.org/ci/dbc/distribution-build-${args.project}/${version}/${build_number}/linux/x64/tar/dist/${args.project}/${args.project}-${version}-linux-x64.tar.gz"

    artifactUrlARM64 = "https://ci.opensearch.org/ci/dbc/distribution-build-${args.project}/${version}/${build_number}/linux/x64/tar/dist/${args.project}/${args.project}-${version}-linux-x64.tar.gz"


    /*slice the time to get date value*/
    build_date = build_time[0..3] + build_time[5..6] + build_time[8..9]
    echo "${build_date}"
    echo "${artifactUrlX64}"
    echo "${artifactUrlARM64}"

    String args.project = inputManifest.build.getFilename()

    def build_qualifier = inputManifest.build.qualifier

    if (build_qualifier != null && build_qualifier != 'null') {
        build_qualifier = "-" + build_qualifier
    }
    else {
        build_qualifier = ''
    }

    echo "${build_qualifier} ${args.project}"


}