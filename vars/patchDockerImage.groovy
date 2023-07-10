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

    sh"""
    #!/bin/bash
    set -e
    set +x

    docker pull ${docker_image}
    """
    sh"""
    docker inspect --format '{{ index .Config.Labels "org.label-schema.build-date"}}' ${docker_image} > build_time
    docker inspect --format '{{ index .Config.Labels "org.label-schema.description"}}' ${docker_image}` > build_number
    """
    sh """docker inspect --format '{{ index .Config.Labels "org.label-schema.description"}}' ${docker_image} > commandResult"""
    result = readFile('commandResult').trim()
    build_time = readFile('build_time').trim()
    build_numbers = readFile('build_number').trim()
    println("${result} ${build_time} ${build_numbers}")

/*
    staging_image = ${staging_image} + "${build_number}"

    println("staging_image: ${staging_image}")

    Validate Digests
    sh"""
    #!/bin/bash

    prod_digest=`docker inspect --format='{{.RepoDigests}}' opensearchproject/${docker_image}`
    staging_digest=`docker inspect --format='{{.RepoDigests}}' opensearchproject/${docker_image}`

    """
    println("${prod_digest[0]} ${staging_digest[0]}")
*/
}
