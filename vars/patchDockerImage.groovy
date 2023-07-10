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
    String build_time = ""
    String build_number = ""
    sh"""
    docker pull ${docker_image}
    $build_time = docker inspect --format '{{ index .Config.Labels "org.label-schema.build-date"}}' ${docker_image}
    $build_number = docker inspect --format '{{ index .Config.Labels "org.label-schema.description"}}' ${docker_image}
    """
    println("docker image sucessfully pulled and inspected, exit 1 ${build_time} ${build_number}")
}
