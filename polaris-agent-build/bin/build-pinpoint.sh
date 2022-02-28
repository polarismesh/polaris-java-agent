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

# init variables
folder_name=polaris-java-agent-"${version}"
package_name="${folder_name}".zip

# download pinpoint and unzip
echo "start to download pinpoint-release"
wget -O pinpoint-agent-2.3.3.tar.gz https://github.com/pinpoint-apm/pinpoint/releases/download/v2.3.3/pinpoint-agent-2.3.3.tar.gz
tar -zxvf pinpoint-agent-2.3.3.tar.gz

# copy polaris.config
echo "start to copy polaris.config"
cp ../config/polaris.config pinpoint-agent-2.3.3/

# build package
echo "start to build package"
cd ../..
mvn -B package --file pom.xml
cd target

#copy bootstrap
boot_jar_name=$(ls -1 | grep pinpoint-polaris-bootstrap | head -1)
cp "${boot_jar_name}" polaris-agent-build/bin/pinpoint-agent-2.3.3

#copy plugin
dubbox_jar_name=$(ls -1 | grep pinpoint-polaris-dubbox-plugin | head -1)
cp "${dubbox_jar_name}" polaris-agent-build/bin/pinpoint-agent-2.3.3/plugin

#copy polaris-all
pushd polaris-agent-core/common/common-library
mvn dependency:copy-dependencies
cd target/dependency
polaris_all_jar_name=$(ls -1 | grep polaris-all | head -1)
popd
cp "polaris-agent-core/common/common-library/target/dependency/${polaris_all_jar_name}" polaris-agent-build/bin/pinpoint-agent-2.3.3

# do package
cd polaris-agent-build/bin
mv pinpoint-agent-2.3.3 ${folder_name}
zip -r "${package_name}" "${folder_name}"
mv "${package_name}" ../../
