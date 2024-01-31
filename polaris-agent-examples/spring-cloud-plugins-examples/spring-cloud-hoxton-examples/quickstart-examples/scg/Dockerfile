FROM alpine:3.13.6

ARG version
ARG java_version

COPY target/polaris-javaagent-demo-sc-quickstart-hoxton-scg${version}.jar /app/hoxton-scg.jar

WORKDIR /app

RUN sed -i 's!http://dl-cdn.alpinelinux.org/!https://mirrors.tencent.com/!g' /etc/apk/repositories

RUN set -eux && \
    apk add openjdk${java_version} && \
    apk add bind-tools && \
    apk add busybox-extras && \
    apk add findutils && \
    apk add tcpdump && \
    apk add tzdata && \
    apk add curl && \
    apk add bash && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    date

RUN chmod 777 /app/

RUN ls -la /app/

ENTRYPOINT ["java", "-jar", "/app/hoxton-scg.jar"]