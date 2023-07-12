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
    String staging_image = "opensearchstaging/${args.product}:${args.version}."
    String prod_digest = ""
    String staging_digest = ""
    String result = ""

    sh"""
    #!/bin/bash
    set -e
    set +x

    docker pull ${docker_image}
    """

    sh """docker inspect --format '{{ index .Config.Labels "org.label-schema.build-date"}}' ${docker_image} > time"""

    build_time = readFile('time').trim()

    sh """docker inspect --format '{{ index .Config.Labels "org.label-schema.description"}}' ${docker_image} > number"""

    build_number = readFile('number').trim()

    println("${build_number}, ${build_time}")

    staging_image = "${staging_image}" + "${build_number}"

    println("staging_image: ${staging_image}")

    sh """docker pull ${staging_image}"""

    /*Validate Digests*/

    sh"""docker inspect ${docker_image} | jq -r '.[0].RepoDigests[0]' | cut -d'@' -f2 > prod"""

    sh"""docker inspect ${staging_image} | jq -r '.[0].RepoDigests[0]' | cut -d'@' -f2 > stage"""

    prod_digest = readFile('prod').trim()
    staging_digest = readFile('stage').trim()

    println("${staging_digest}")


}
