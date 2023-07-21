/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

void call(Map args = [:]) {
    String docker_image = "opensearchproject/${args.product}:${args.version}"
    String manifest = ""
    String build_date = ""

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

    /*slice the time to get date value*/
    build_date = build_time[0..3] + build_time[5..6] + build_time[8..9]
    echo "${build_date}"






}
