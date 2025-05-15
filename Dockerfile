#########################################
##### freddy-ai-platform build image starts here #####
#########################################
FROM maven:3.9-amazoncorretto-21 as builder
ARG NEXUS_USERNAME
ARG NEXUS_PASSWORD
ARG PRIVATE_REPO_NEXUS_USERNAME
ARG PRIVATE_REPO_NEXUS_PASSWORD
ENV BUILD_ROOT /build
# mvnsettings.xml from freshworks-boot:/mvnsettings.xml will be used
COPY ./mvnsettings.xml /root/.m2/settings.xml
WORKDIR $BUILD_ROOT

# For caching intermediate builds. Will improve image creation performance in dev laptops.
COPY ./pom.xml $BUILD_ROOT/
RUN mvn -B clean install -DskipTests -Dcheckstyle.skip -Dasciidoctor.skip -Dmaven.gitcommitid.skip -Dspring-boot.repackage.skip -Dmaven.exec.skip=true -Dmaven.install.skip -Dmaven.resources.skip  -Dcodegen.skip --fail-never
# End caching intermediate builds

COPY . $BUILD_ROOT
RUN sed -i 's,http://nexus.runway.ci:32000,https://nexuscentral.runwayci.com,g' ./pom.xml
RUN mvn install

#########################################
##### freddy-ai-platform app image starts here #####
#########################################
FROM amazoncorretto:21
ARG APP_BUILD_VERSION
ENV APP_ROOT /app
ENV BUILD_ROOT /build

ENV JAR_FILE freddy-ai-platform-*.jar

WORKDIR $APP_ROOT
COPY . $APP_ROOT
COPY --from=builder $BUILD_ROOT/target/$JAR_FILE $APP_ROOT/

#install for ps command
RUN  yum update -y && yum install -y procps

ENV JAVA_OPTS=""
ENV APP_VERSION=${APP_BUILD_VERSION}
CMD source $APP_ROOT/set_sherlock_env.sh; java $JAVA_OPTS -javaagent:$APP_ROOT/build_libs/${OPENTELEMETRY_HAYSTACK_JARFILE}.jar -jar $APP_ROOT/$JAR_FILE

EXPOSE 6060
