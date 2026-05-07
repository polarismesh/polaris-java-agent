#!/bin/bash

version=$(cat /app/version.txt)
polaris_agent_zip_name="polaris-java-agent-${version}"
polaris_agent_dir_name="polaris-java-agent"
echo "polaris agent dir name ${polaris_agent_dir_name}"

java_agent_dir=${JAVA_AGENT_DIR}
echo "begin exec mkdir -p ${java_agent_dir}"

# 这里先把 java-agent 存放的目录先构造出来
mkdir -p ${java_agent_dir}

# 将 /app/lib 下的文件 copy 一份出去

cp -f /app/${polaris_agent_zip_name}.zip ${java_agent_dir}/
cp -f /app/version.txt ${java_agent_dir}/


# 这里需要将 agent 起到需要的相关信息全部注入到对应 plugin 中
cd ${java_agent_dir}
unzip ${polaris_agent_zip_name}.zip
# 创建 polaris-java-agent 目录
mkdir -p ${polaris_agent_dir_name}
cp -rf ${polaris_agent_zip_name}/* ${polaris_agent_dir_name}/

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

# 第一步，需要确定 agent-plugin 启用哪个（支持逗号分隔的多框架）
custom_plugin_id=""
if check_string_not_empty "${JAVA_AGENT_FRAMEWORK_NAME}" && check_string_not_empty "${JAVA_AGENT_FRAMEWORK_VERSION}"; then
  IFS=',' read -ra framework_names <<< "${JAVA_AGENT_FRAMEWORK_NAME}"
  IFS=',' read -ra framework_versions <<< "${JAVA_AGENT_FRAMEWORK_VERSION}"

  if [ ${#framework_names[@]} -ne ${#framework_versions[@]} ]; then
    echo "ERROR: JAVA_AGENT_FRAMEWORK_NAME has ${#framework_names[@]} items but JAVA_AGENT_FRAMEWORK_VERSION has ${#framework_versions[@]} items, they must match"
    exit 1
  fi

  for i in "${!framework_names[@]}"; do
    name="${framework_names[$i]}"
    version="${framework_versions[$i]}"
    plugin_id="${name}-${version}-plugin"
    echo "inject with framework ${name} and version ${version}, plugin_id=${plugin_id}"
    if [ -z "${custom_plugin_id}" ]; then
      custom_plugin_id="${plugin_id}"
    else
      custom_plugin_id="${custom_plugin_id},${plugin_id}"
    fi
  done
else
  echo "JAVA_AGENT_FRAMEWORK_NAME [${JAVA_AGENT_FRAMEWORK_NAME}] or JAVA_AGENT_FRAMEWORK_VERSION [${JAVA_AGENT_FRAMEWORK_VERSION}] is empty"
  framework_names=()
  framework_versions=()
fi
echo "plugins.enable=${custom_plugin_id}" > ${polaris_agent_dir_name}/conf/polaris-agent.config

# 第二步，将 plugin 所需要的配置注入到 plugin 对应的目录中去（按框架分流）
has_spring_cloud=false
for i in "${!framework_names[@]}"; do
  name="${framework_names[$i]}"
  case "${name}" in
    spring-cloud)
      has_spring_cloud=true
      echo "inject config for spring-cloud plugin"
      target_config_file=${polaris_agent_dir_name}/conf/plugin/spring-cloud/application.properties
      echo "inject with default config ${JAVA_AGENT_PLUGIN_CONF}"
      if check_string_not_empty "${JAVA_AGENT_PLUGIN_CONF}"; then
        echo "${JAVA_AGENT_PLUGIN_CONF}" > "${target_config_file}"
      else
        echo "JAVA_AGENT_PLUGIN_CONF is empty"
        echo "read polaris server ip: ${POLARIS_SERVER_IP}"
        echo "read polaris discovery port: ${POLARIS_DISCOVER_PORT}"
        echo "read polaris config ip: ${POLARIS_CONFIG_IP}"
        echo "read polaris config port: ${POLARIS_CONFIG_PORT}"
        polaris_address="grpc\\\:\/\/${POLARIS_SERVER_IP}\\\:${POLARIS_DISCOVER_PORT}"
        polaris_config_address="grpc\\\:\/\/${POLARIS_CONFIG_IP}\\\:${POLARIS_CONFIG_PORT}"
        echo "read polaris address: ${polaris_address}"
        echo "read polaris config address: ${polaris_config_address}"
        sed -i "s/spring.cloud.polaris.address=grpc\\\:\/\/127.0.0.1\\\:8091/spring.cloud.polaris.address=${polaris_address}/g" ${target_config_file}
        sed -i "s/spring.cloud.polaris.config.address=grpc\\\:\/\/127.0.0.1\\\:8093/spring.cloud.polaris.config.address=${polaris_config_address}/g" ${target_config_file}
      fi
      cat ${target_config_file}
      ;;
    dubbo)
      echo "inject config for dubbo plugin"
      if ! check_string_not_empty "${POLARIS_SERVER_IP}" || ! check_string_not_empty "${POLARIS_DISCOVER_PORT}"; then
        echo "WARNING: POLARIS_SERVER_IP or POLARIS_DISCOVER_PORT is empty, skip dubbo config injection"
      else
        dubbo_config_file=${polaris_agent_dir_name}/conf/plugin/dubbo/dubbo-polaris.properties
        echo "read polaris server ip: ${POLARIS_SERVER_IP}"
        echo "read polaris discovery port: ${POLARIS_DISCOVER_PORT}"
        polaris_dubbo_address="polaris://${POLARIS_SERVER_IP}:${POLARIS_DISCOVER_PORT}"
        echo "read polaris dubbo address: ${polaris_dubbo_address}"
        sed -i "s|dubbo.registry.address=polaris://127.0.0.1:8091|dubbo.registry.address=${polaris_dubbo_address}|g" ${dubbo_config_file}
        cat ${dubbo_config_file}
      fi
      ;;
    *)
      echo "unknown framework: ${name}, skip config injection"
      ;;
  esac
done

# 第三步，将地域信息拉取并设置进配置文件（仅 spring-cloud 需要）
if [ "${has_spring_cloud}" = true ]; then
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

  cat ${target_config_file}
fi
