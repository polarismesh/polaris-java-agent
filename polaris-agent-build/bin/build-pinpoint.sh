#!/bin/bash

set -e

# generate version
if [ $# != 1 ]; then
  echo -e "invalid args, eg.bash $0 version"
  exit 1
fi

version="$1"
plugin_list=$(cat "plugins")

# workdir root
cd ../..
workdir=$(pwd)
echo "workdir is ${workdir}"

# init variables
folder_name=polaris-java-agent-"${version}"
package_name="${folder_name}".zip

# download pinpoint
echo "start to download pinpoint-release"
wget -O pinpoint-agent-2.3.3.tar.gz https://github.com/pinpoint-apm/pinpoint/releases/download/v2.3.3/pinpoint-agent-2.3.3.tar.gz

# clear pinpoint bootstrap
tar -zxvf pinpoint-agent-2.3.3.tar.gz
pushd pinpoint-agent-2.3.3
find ./ -maxdepth 1 -name "pinpoint-bootstrap*.jar" | xargs rm -f
popd

# copy polaris.config
echo "start to copy polaris.config"
cp polaris-agent-build/config/polaris.config pinpoint-agent-2.3.3/

# build package
echo "start to build package"
mvn clean -B package --file pom.xml

#copy bootstrap
pushd target
echo "start to copy bootstrap"
ls -lstrh
boot_jar_name="polaris-java-agent-bootstrap.jar"
cp "${boot_jar_name}" "${workdir}"/pinpoint-agent-2.3.3/

#remove unnecessary plug-ins
rm -rf  "${workdir}"/pinpoint-agent-2.3.3/plugin/*
echo "remove unnecessary plug-ins success"

#modify log4j2.xml
if [ $(uname) == "Darwin" ]
then
	echo "darwin"
	sed -i "" "/<AppenderRef ref=\"console\"\/>/d"  "${workdir}"/pinpoint-agent-2.3.3/profiles/local/log4j2.xml
	sed -i "" "/<AppenderRef ref=\"console\"\/>/d"  "${workdir}"/pinpoint-agent-2.3.3/profiles/release/log4j2.xml
  echo "modify log4j2.xml success"
elif [ $(expr substr $(uname -s) 1 5)  == "Linux" ]
then
	echo "linux"
	sed -i  "/<AppenderRef ref=\"console\"\/>/d"  "${workdir}"/pinpoint-agent-2.3.3/profiles/local/log4j2.xml
	sed -i  "/<AppenderRef ref=\"console\"\/>/d"  "${workdir}"/pinpoint-agent-2.3.3/profiles/release/log4j2.xml
  echo "modify log4j2.xml success"
fi

#copy plugin
echo "start to copy plugin"
ls -lstrh

for line in ${plugin_list}
do
  plugin_name=${line}
  plugin_jar_name=$(ls -1 | grep pinpoint-polaris-${plugin_name} | head -1)
  zip -d ${plugin_jar_name} 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
  cp -rf "${plugin_jar_name}" "${workdir}"/pinpoint-agent-2.3.3/plugin
done
popd

#copy polaris dependencies
echo "start to copy polaris"
mkdir -p "${workdir}"/pinpoint-agent-2.3.3/polaris/logs
mkdir -p "${workdir}"/pinpoint-agent-2.3.3/polaris/conf
mkdir -p "${workdir}"/pinpoint-agent-2.3.3/polaris/lib
pushd polaris-agent-build
mvn dependency:copy-dependencies
pushd target/dependency
cp ./*.jar "${workdir}"/pinpoint-agent-2.3.3/polaris/lib/
popd
cp config/polaris.yml "${workdir}"/pinpoint-agent-2.3.3/polaris/conf/
popd

# do package
echo "start to zip package"
rm -rf ${folder_name}
rm -rf ${package_name}
mv pinpoint-agent-2.3.3 ${folder_name}
zip -r "${package_name}" "${folder_name}"
