#!/bin/bash

set -e

# generate version
if [ $# != 1 ]; then
  echo -e "invalid args, eg.bash $0 version"
  exit 1
fi

version="$1"

# workdir root
cd ../..
workdir=$(pwd)
echo "workdir is ${workdir}"

# init variables
folder_name=polaris-java-agent-"${version}"
package_name="${folder_name}".zip

echo "start to create build dir ${folder_name}"
rm -rf "${folder_name}"
mkdir -p "${folder_name}"
mkdir -p "${folder_name}/conf"
mkdir -p "${folder_name}/plugins"

cp "polaris-agent-build/conf/polaris-agent.config" "${folder_name}/conf"

echo "start to build package"

if [[ "${use_docker_env}" == "true" ]]; then
  docker run --rm -u root -v "$(pwd)":/home/maven/project -w /home/maven/project maven:3.8.6-openjdk-8 mvn clean -B package --file pom.xml
else
  mvn clean -B package --file pom.xml
fi

cp "polaris-agent-core/polaris-agent-core-bootstrap/target/polaris-agent-core-bootstrap.jar" "${folder_name}/"

pushd "polaris-agent-plugins"
plugin_folders=$(find . -maxdepth 2 | grep -E ".+-plugin$")
for plugin_folder in ${plugin_folders}; do
  file_name=${plugin_folder##*/}
  cp "${plugin_folder}/target/${file_name}-${version}.jar" "../${folder_name}/plugins/"
  if [ -d "${plugin_folder}/src/main/conf" ]; then
    cp -r "${plugin_folder}/src/main/conf/." "../${folder_name}/conf/"
  fi
done
popd

# do package
echo "start to zip package"
zip -r "${package_name}" "${folder_name}"
