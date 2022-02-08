#!/bin/bash

# download pinpoint and unzip
wget -O polaris-java-agent.tar.gz https://github.com/pinpoint-apm/pinpoint/releases/download/v2.3.3/pinpoint-agent-2.3.3.tar.gz
tar -zxvf polaris-java-agent.tar.gz

# modify content
mv pinpoint-agent-2.3.3 polaris-java-agent
rm -rf polaris-java-agent/plugin/*

# build with maven
mvn -B package --file pom.xml

# add plugin
mv target/* polaris-java-agent/plugin/

# zip
zip -r polaris-java-agent.zip polaris-java-agent



