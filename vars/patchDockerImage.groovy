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
    String build_time = ""
    String build_number = ""
    String prod_digest = ""
    String staging_digest = ""

    sh"""
    #!/bin/bash

    docker pull ${docker_image}
    build_time=`docker inspect --format '{{ index .Config.Labels "org.label-schema.build-date"}}' ${docker_image}`
    build_number=`docker inspect --format '{{ index .Config.Labels "org.label-schema.description"}}' ${docker_image}`
    """
    println("docker image successfully pulled and inspected, exit 1 ${build_time} ${build_number}")

    staging_image += "${build_number}"

    println("staging_image ${staging_image}")

    /* Validate Digests */
    sh"""
    #!/bin/bash

    prod_digest=`docker inspect ${docker_image} | jq -r '.[0].RepoDigests[0]' | cut -d'@' -f2`
    staging_digest=`docker inspect ${staging_image} | jq -r '.[0].RepoDigests[0]' | cut -d'@' -f2`

    """
    println("prod_digest staging_digest")




}
