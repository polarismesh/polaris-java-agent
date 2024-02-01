#!/bin/bash

version=$(cat /app/version.txt)
polaris_agent_dir_name="polaris-java-agent-${version}"
echo "polaris agent dir name ${polaris_agent_dir_name}"

java_agent_dir=${JAVA_AGENT_DIR}
echo "begin exec mkdir -p ${java_agent_dir}"

# 这里先把 java-agent 存放的目录先构造出来
mkdir -p ${java_agent_dir}

# 将 /app/lib 下的文件 copy 一份出去

#mv -f /app/${polaris_agent_dir_name} ${java_agent_dir}
#mv -f /app/version.txt ${java_agent_dir}
ln -s /app/${polaris_agent_dir_name} ${java_agent_dir}/${polaris_agent_dir_name}
ln -s /app/version.txt ${java_agent_dir}/version.txt


# 这里需要将 agent 起到需要的相关信息全部注入到对应 plugin 中
cd ${java_agent_dir}

echo "inject with framework ${JAVA_AGENT_FRAMEWORK_NAME} and version ${JAVA_AGENT_FRAMEWORK_VERSION}"

# 第一步，需要确定 agent-plugin 启用哪个
custom_plugin_id="${JAVA_AGENT_FRAMEWORK_NAME}-${JAVA_AGENT_FRAMEWORK_VERSION}-plugin"
echo "${custom_plugin_id}" > ${polaris_agent_dir_name}/conf/polaris-agent.config

# 第二步，将 plugin 所需要的配置注入到 plugin 对应的目录中去
java_agent_config_dir="${JAVA_AGENT_FRAMEWORK_NAME}-${JAVA_AGENT_FRAMEWORK_VERSION}"
echo "inject with config dir ${java_agent_config_dir}"
echo "inject with default config ${JAVA_AGENT_PLUGIN_CONF}"

custom_plugin_properties=${JAVA_AGENT_PLUGIN_CONF}
echo "${custom_plugin_properties}" > ${polaris_agent_dir_name}/conf/plugin/${java_agent_config_dir}/application.properties