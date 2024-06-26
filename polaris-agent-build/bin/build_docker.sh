#!/bin/bash

set -e

# workdir root


version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='properties']/*[local-name()='revision']/text()" ../../pom.xml)
echo "${version}" > version.txt

cd ../..
workdir=$(pwd)
echo "workdir is ${workdir}"

cd polaris-agent-build/bin
bash ./build.sh

cd ${workdir}

docker_tag=$1

docker_repository="${DOCKER_REPOSITORY}"
if [[ "${docker_repository}" == "" ]]; then
    docker_repository="polarismesh"
fi

echo "docker repository : ${docker_repository}/polaris-javaagent-init, tag : ${docker_tag}"

arch_list=( "amd64" "arm64" )
# arch_list=( "amd64" )
platforms=""

for arch in ${arch_list[@]}; do
    platforms+="linux/${arch},"
done

echo "${platforms}"
platforms=${platforms%?}
extra_tags=""

pre_release=`echo ${docker_tag}|egrep "(alpha|beta|rc|[T|t]est)"|wc -l`
if [ ${pre_release} == 0 ]; then
  extra_tags="-t ${docker_repository}/polaris-javaagent-init:latest"
fi

docker buildx build -f ./Dockerfile -t ${docker_repository}/polaris-javaagent-init:${docker_tag}  --build-arg version=${version} ${extra_tags} --platform ${platforms} --push ./