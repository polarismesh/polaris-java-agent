#!/bin/bash

version=$(cat /app/version.txt)
polaris_agent_dir_name="polaris-java-agent-${version}"
echo "polaris agent dir name ${polaris_agent_dir_name}"

java_agent_dir=${JAVA_AGENT_DIR}
echo "begin exec mkdir -p ${java_agent_dir}"

# 这里先把 java-agent 存放的目录先构造出来
mkdir -p ${java_agent_dir}

# 将 /app/lib 下的文件 copy 一份出去

cp -f /app/${polaris_agent_dir_name}.zip ${java_agent_dir}/
cp -f /app/version.txt ${java_agent_dir}/


# 这里需要将 agent 起到需要的相关信息全部注入到对应 plugin 中
cd ${java_agent_dir}
unzip ${polaris_agent_dir_name}.zip

check_string_not_empty() {
  local string_to_check="$1"
  local trimmed

  # 删除所有空白字符后检查字符串是否为空
  trimmed=$(echo "$string_to_check" | tr -d '[:space:]')

  if test -n "$trimmed"; then
    return 0  # 变量非空且不全是空格，返回0（成功）
  else
    return 1  # 变量为空或全是空格，返回1（失败）
  fi
}

# 第一步，需要确定 agent-plugin 启用哪个
custom_plugin_id=""
if check_string_not_empty "${JAVA_AGENT_FRAMEWORK_NAME}"  && check_string_not_empty "${JAVA_AGENT_FRAMEWORK_VERSION}"; then
  custom_plugin_id="${JAVA_AGENT_FRAMEWORK_NAME}-${JAVA_AGENT_FRAMEWORK_VERSION}-plugin"
  echo "inject with framework ${JAVA_AGENT_FRAMEWORK_NAME} and version ${JAVA_AGENT_FRAMEWORK_VERSION}"
else
  echo "JAVA_AGENT_FRAMEWORK_NAME [${JAVA_AGENT_FRAMEWORK_NAME}] or JAVA_AGENT_FRAMEWORK_VERSION [${JAVA_AGENT_FRAMEWORK_VERSION}] is empty"
fi
echo "plugins.enable=${custom_plugin_id}" > ${polaris_agent_dir_name}/conf/polaris-agent.config

# 第二步，将 plugin 所需要的配置注入到 plugin 对应的目录中去
echo "inject with default config ${JAVA_AGENT_PLUGIN_CONF}"
custom_plugin_properties=${JAVA_AGENT_PLUGIN_CONF}
target_config_file=${polaris_agent_dir_name}/conf/plugin/spring-cloud/application.properties
echo "${custom_plugin_properties}" > "${target_config_file}"

# 第三步，将地域信息拉取并设置进配置文件
# 腾讯云不能拿到大区，因此腾讯云上的region对应的是北极星的zone，zone对应北极星的campus
echo "start to fetch region, target config file ${target_config_file}"
region="$(curl -s --connect-timeout 10 -m 10 http://metadata.tencentyun.com/latest/meta-data/placement/region)"
region_code=$?
echo "region is ${region}, return code is ${region_code}"
if [ ${region_code} -eq 0 ] && [ -n ${region} ]; then
  sed -i "s/spring.cloud.tencent.metadata.content.zone=\"\"/spring.cloud.tencent.metadata.content.zone=${region}/g" ${target_config_file}
fi

echo "start to fetch zone"
zone="$(curl -s --connect-timeout 10 -m 10 http://metadata.tencentyun.com/latest/meta-data/placement/zone)"
zone_code=$?
echo "zone is ${zone}, return code is ${zone_code}"
if [ ${zone_code} -eq 0 ] && [ -n ${zone} ]; then
  sed -i "s/spring.cloud.tencent.metadata.content.campus=\"\"/spring.cloud.tencent.metadata.content.campus=${zone}/g" ${target_config_file}
fi