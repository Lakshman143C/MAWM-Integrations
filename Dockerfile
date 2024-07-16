################
##STANDARD SPRING BOOT DOCKERFILE FOR CONTAINERIZED DEPLOYMENTS
##USES A LAYERED JAR APPROACH
################

FROM fdazregistry.azurecr.io/floordecor/java:17-contrast-alpine

ARG serviceName=inbound
ARG contractAppName="InBound"
ARG workPath=/opt/floordecor/$serviceName
ARG logPath=/var/log/floordecor/$serviceName
ENV SERVICE_NAME ${serviceName}
ENV VERSION ${version}
ENV WORKPATH ${workPath}
ENV LOG_PATH ${logPath}
ENV M2_HOME /root/.m2


ENV SPRING_CONFIG_LOCATION=/configurations/
ENV SPRING_PROFILES_ACTIVE=dev

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=85.0 -XX:+UseContainerSupport"

USER root

RUN mkdir -p $WORKPATH && mkdir -p $LOG_PATH
RUN chown -R floordecor /opt/floordecor/* && chown -R floordecor $LOG_PATH

WORKDIR $WORKPATH
VOLUME $WORKPATH

RUN apk add tzdata && \
    cp /usr/share/zoneinfo/US/Eastern /etc/localtime && \
    echo "US/Eastern" > /etc/timezone && \
    apk del tzdata

RUN chown floordecor:floordecor $WORKPATH
RUN cp /contrast/contrast.jar .
COPY log4j2.properties ./
COPY target/dependencies ./
COPY target/spring-boot-loader ./
COPY target/snapshot-dependencies ./
COPY target/application ./

COPY src/main/resources/application-dev.yml ${CONFIG_LOCATION}application-dev.yml
ENV HOME $WORKPATH

USER floordecor

WORKDIR $WORKPATH

ENV AGENT_STRING  ""
CMD ["sh", "-c", "java $AGENT_STRING org.springframework.boot.loader.JarLauncher"]