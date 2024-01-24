#!/bin/bash

version=$(cat /app/version.txt)
polaris_agent_dir_name="polaris-java-agent-${version}"
echo "polaris agent dir name ${polaris_agent_dir_name}"

java_agent_dir=${JAVA_AGENT_DIR}
echo "begin exec mkdir -p ${java_agent_dir}"

# 这里先把 java-agent 存放的目录先构造出来
mkdir -p ${java_agent_dir}

# 将 /app/lib 下的文件 copy 一份出去

mv -f /app/${polaris_agent_dir_name} ${java_agent_dir}
mv -f /app/version.txt ${java_agent_dir}

# 这里需要将 agent 起到需要的相关信息全部注入到对应 plugin 中
cd ${java_agent_dir}

# 第一步，需要确定 agent-plugin 启用哪个
custom_plugin_type=${JAVA_AGENT_PLUGIN_TYPE}
echo "${custom_plugin_type}" > ${polaris_agent_dir_name}/conf/polaris-agent.config

# 第二步，将 plugin 所需要的配置注入到 plugin 对应的目录中去
custom_plugin_properties=${JAVA_AGENT_PLUGIN_CONF}
echo "${custom_plugin_properties}" > ${polaris_agent_dir_name}/plugin/${custom_plugin_type}/application.properties