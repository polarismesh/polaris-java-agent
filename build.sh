#!/bin/bash

set -e

# generate version
if [ $# -gt 0 ]; then
  version="$1"
else
  current=`date "+%Y-%m-%d %H:%M:%S"`
  timeStamp=`date -d "$current" +%s`
  currentTimeStamp=$(((timeStamp*1000+10#`date "+%N"`/1000000)/1000))
  version="$currentTimeStamp"
fi

# init variables
folder_name=polaris-java-agent-"${version}"
package_name="${folder_name}".zip

# download pinpoint and unzip
wget -O "${folder_name}".tar.gz https://github.com/pinpoint-apm/pinpoint/releases/download/v2.3.3/pinpoint-agent-2.3.3.tar.gz
tar -zxvf "${folder_name}".tar.gz

# modify content
mv pinpoint-agent-2.3.3 "${folder_name}"
rm -rf "${folder_name}"/plugin/*

# build with maven
mvn -B package --file pom.xml

# add plugin
mv target/* "${folder_name}"/plugin/

# zip
zip -r "${package_name}" "${folder_name}"



