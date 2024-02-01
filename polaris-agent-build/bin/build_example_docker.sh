#!/bin/bash

set -e

# workdir root


version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='properties']/*[local-name()='revision']/text()" ../../pom.xml)
echo "${version}" > version.txt

cd ../..
workdir=$(pwd)
echo "workdir is ${workdir}"

docker_tag=$1

docker_repository="${DOCKER_REPOSITORY}"
if [[ "${docker_repository}" == "" ]]; then
    docker_repository="polarismesh"
fi

#arch_list=( "amd64" "arm64" )
arch_list=( "amd64" )
platforms=""

for arch in ${arch_list[@]}; do
    platforms+="linux/${arch},"
done

echo "${platforms}"
platforms=${platforms%?}

#this script must be run after build.sh
pushd "polaris-agent-examples"
docker_files=$(find ./ | grep -w "Dockerfile")
for docker_file in ${docker_files}; do
  folder_name=${docker_file%/*}
  pushd "${folder_name}/"
  repo_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='artifactId']/text()" pom.xml)

  echo "current is $(pwd)"

  dir_name=${folder_name##*/} 
  
  cp Dockerfile Dockerfile-${dir_name}
  docker_tag="${version}-java8"

  echo "docker repository java8: ${docker_repository}/${repo_name}, tag : ${docker_tag}"
  docker buildx build -f Dockerfile-${dir_name} --no-cache -t ${docker_repository}/${repo_name}:${docker_tag}  --build-arg version=${version} --build-arg java_version=8 --platform ${platforms} --push ./

  docker_tag="${version}-java11"
  echo "docker repository java11: ${docker_repository}/${repo_name}, tag : ${docker_tag}"
  docker buildx build -f Dockerfile-${dir_name} --no-cache -t ${docker_repository}/${repo_name}:${docker_tag}  --build-arg version=${version} --build-arg java_version=11 --platform ${platforms} --push ./

  rm Dockerfile-${dir_name}
  
#  docker_tag="${version}-java17"
#  echo "docker repository java17: ${docker_repository}/${repo_name}, tag : ${docker_tag}"
#  docker buildx build -f ./Dockerfile -t ${docker_repository}/${repo_name}:${docker_tag}  --build-arg version=${version} --build-arg java_version=17 --platform ${platforms} --push ./
  popd 
done
popd