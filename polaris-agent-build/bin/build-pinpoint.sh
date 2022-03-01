#!/bin/bash

set -e

# generate version
if [ $# -gt 0 ]; then
  version="$1"
else
  current=$(date "+%Y-%m-%d %H:%M:%S")
  timeStamp=$(date -d "$current" +%s)
  currentTimeStamp=$(((timeStamp * 1000 + 10#$(date "+%N") / 1000000) / 1000))
  version="$currentTimeStamp"
fi

# workdir root
cd ../..
workdir=$(pwd)
echo "workdir is ${workdir}"

# init variables
folder_name=polaris-pinpoint-agent-"${version}"
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
mvn -B package --file pom.xml

#copy bootstrap
pushd target
echo "start to copy bootstrap"
boot_jar_name=$(ls -1 | grep pinpoint-polaris-bootstrap | head -1)
cp "${boot_jar_name}" "${workdir}"/pinpoint-agent-2.3.3/

#copy plugin
echo "start to copy plugin"
dubbox_jar_name=$(ls -1 | grep pinpoint-polaris-dubbox-plugin | head -1)
cp "${dubbox_jar_name}" "${workdir}"/pinpoint-agent-2.3.3/plugin
popd

#copy polaris dependencies
echo "start to copy polaris dependencies"
mkdir -P "${workdir}"/pinpoint-agent-2.3.3/polaris/lib
mkdir -P "${workdir}"/pinpoint-agent-2.3.3/polaris/log
pushd polaris-agent-build
mvn dependency:copy-dependencies
pushd target/dependency
cp ./*.jar "${workdir}"/pinpoint-agent-2.3.3/polaris/lib/
popd
cp config/logback.xml "${workdir}"/pinpoint-agent-2.3.3/polaris/lib/
popd

# do package
echo "start to zip package"
mv pinpoint-agent-2.3.3 ${folder_name}
zip -r "${package_name}" "${folder_name}"
