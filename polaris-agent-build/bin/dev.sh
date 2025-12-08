#!/bin/bash

set -e

# workdir root
cd ../..
version=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='properties']/*[local-name()='revision']/text()" pom.xml)
workdir=$(pwd)
echo "workdir is ${workdir}"

# init variables
folder_name=polaris-java-agent-"${version}"
package_name="${folder_name}".zip

echo "start to create build dir ${folder_name}"

# 备份已存在的 conf 目录
if [ -d "${folder_name}/conf" ]; then
  echo "backing up existing conf directory"
  temp_conf_backup="/tmp/polaris-agent-conf-backup-$$"
  mkdir -p "${temp_conf_backup}"
  cp -r "${folder_name}/conf/." "${temp_conf_backup}/"
fi

rm -rf "${folder_name}"
mkdir -p "${folder_name}"
mkdir -p "${folder_name}/conf"
mkdir -p "${folder_name}/plugins"
mkdir -p "${folder_name}/lib"
mkdir -p "${folder_name}/lib/java9"
mkdir -p "${folder_name}/boot"

# 恢复备份的 conf 目录
if [ -d "${temp_conf_backup}" ]; then
  echo "restoring backed up conf directory"
  cp -r "${temp_conf_backup}/." "${folder_name}/conf/"
  rm -rf "${temp_conf_backup}"
fi

echo "start to build package with offline mode"

if [[ "${use_docker_env}" == "true" ]]; then
  docker run --rm -u root -v "$(pwd)":/home/maven/project -w /home/maven/project maven:3.8.6-openjdk-8 mvn clean -B package --file pom.xml
else
  mvn clean -B package --file pom.xml
fi

cp "polaris-agent-core/polaris-agent-core-bootstrap/target/polaris-agent-core-bootstrap.jar" "${folder_name}/"
cp "polaris-agent-core/polaris-agent-core-bootstrap-common/target/polaris-agent-core-bootstrap-common-${version}.jar" "${folder_name}/lib/"
cp "polaris-agent-core/polaris-agent-core-asm-java9/target/polaris-agent-core-asm-java9-${version}.jar" "${folder_name}/lib/java9/"
cp "polaris-agent-core/polaris-agent-core-optional-java9/target/polaris-agent-core-optional-java9-${version}.jar" "${folder_name}/lib/java9/"
cp "polaris-agent-core/polaris-agent-core-optional-java17/target/polaris-agent-core-optional-java17-${version}.jar" "${folder_name}/lib/java9/"
cp "polaris-agent-core/polaris-agent-core-extension/target/polaris-agent-core-extension-${version}.jar" "${folder_name}/boot/"

pushd "polaris-agent-plugins"
plugin_folders=$(find . -maxdepth 2 | grep -E ".+-plugin$")
for plugin_folder in ${plugin_folders}; do
  file_name=${plugin_folder##*/}
  cp "${plugin_folder}/target/${file_name}-${version}.jar" "../${folder_name}/plugins/"
  if [ -d "${plugin_folder}/src/main/conf" ]; then
    cp -r "${plugin_folder}/src/main/conf/." "../${folder_name}/conf/"
  fi
done
plugin_config_folders=$(find . -maxdepth 2 | grep -E ".+-plugin-common$")
for plugin_config_folder in ${plugin_config_folders}; do
  file_name=${plugin_config_folder##*/}
  if [ -d "${plugin_config_folder}/src/main/conf" ]; then
    cp -r "${plugin_config_folder}/src/main/conf/." "../${folder_name}/conf/"
  fi
done
popd

# do package
echo "start to zip package"
zip -r "${package_name}" "${folder_name}"
