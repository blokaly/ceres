#@IgnoreInspection BashAddShebang

docker pull blokaly/java8
docker run -i -t blokaly/java8 /bin/bash

apt-get update
apt-get install curl
apt-get install gnupg

# install tini
TINI_VERSION="v0.17.0"
TINI_REAL_VERSION="0.17.0"
TINI_BUILD="/tmp/tini"
TINI_DEPS="build-essential cmake git rpm curl libcap-dev python-dev hardening-includes"

echo "Installing Tini build dependencies"
apt-get install --yes ${TINI_DEPS}
echo "Building Tini"
git clone https://github.com/krallin/tini.git "${TINI_BUILD}"
cd "${TINI_BUILD}"
curl -O https://pypi.python.org/packages/source/v/virtualenv/virtualenv-13.1.2.tar.gz 
tar -xf virtualenv-13.1.2.tar.gz 
mv virtualenv-13.1.2/virtualenv.py virtualenv-13.1.2/virtualenv 
export PATH="${TINI_BUILD}/virtualenv-13.1.2:${PATH}" 
HARDENING_CHECK_PLACEHOLDER="${TINI_BUILD}/hardening-check/hardening-check" 
HARDENING_CHECK_PLACEHOLDER_DIR="$(dirname "${HARDENING_CHECK_PLACEHOLDER}")" 
mkdir "${HARDENING_CHECK_PLACEHOLDER_DIR}" 
echo  "#/bin/sh" > "${HARDENING_CHECK_PLACEHOLDER}" 
chmod +x "${HARDENING_CHECK_PLACEHOLDER}" 
export PATH="${PATH}:${HARDENING_CHECK_PLACEHOLDER_DIR}" 
git checkout "${TINI_VERSION}" 
export SOURCE_DIR="${TINI_BUILD}" 
export BUILD_DIR="${TINI_BUILD}" 
export ARCH_NATIVE=1 
"${TINI_BUILD}/ci/run_build.sh" 
echo "Installing Tini"
dpkg -i "${TINI_BUILD}/tini_${TINI_REAL_VERSION}.deb"
echo "Symlinkng to /usr/local/bin"
ln -s /usr/bin/tini        /usr/local/bin/tini
ln -s /usr/bin/tini-static /usr/local/bin/tini-static
echo "Running Smoke Test"
/usr/bin/tini -- ls
/usr/bin/tini-static -- ls
/usr/local/bin/tini -- ls
/usr/local/bin/tini-static -- ls
echo "Done Tini build"

# install gosu
GOSU_VERSION="1.7"
curl -L -o /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-amd64"
curl -L -o /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-amd64.asc"
export GNUPGHOME="$(mktemp -d)"
gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4
gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu
rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc
chmod +x /usr/local/bin/gosu
gosu nobody true

# clean up
echo "Cleaning up"
cd /
rm -rf "${TINI_BUILD}"
apt-get purge --yes ${TINI_DEPS}
apt-get autoremove --yes
rm -rf /var/lib/apt/lists/*

# Install jmxtrans
JMXTRANS_HOME="/usr/share/jmxtrans"
JMXTRANS_LOG="/var/log/jmxtrans"
JMXTRANS_JSON="/var/lib/jmxtrans"
mkdir -p ${JMXTRANS_HOME}/bin
mkdir -p ${JMXTRANS_HOME}/conf
mkdir -p ${JMXTRANS_HOME}/lib
mkdir -p ${JMXTRANS_LOG}
mkdir -p ${JMXTRANS_JSON}
JMXTRANS_VERSION=`curl http://central.maven.org/maven2/org/jmxtrans/jmxtrans/maven-metadata.xml | sed -n 's:.*<release>\(.*\)</release>.*:\1:p'`
wget -q http://central.maven.org/maven2/org/jmxtrans/jmxtrans/${JMXTRANS_VERSION}/jmxtrans-${JMXTRANS_VERSION}-all.jar
mv jmxtrans-${JMXTRANS_VERSION}-all.jar ${JMXTRANS_HOME}/lib/jmxtrans-all.jar
cat <<EOF > ${JMXTRANS_HOME}/conf/logback.xml
<configuration debug="false">

    <appender name="File" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/jmxtrans/\${log.name}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>\${log.name}/\${log.name}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>500MB</maxFileSize>
            <maxHistory>14</maxHistory>
        </rollingPolicy>
        <encoder>
            <charset>UTF-8</charset>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ}[%level][%.10thread]  %logger{32} - %msg%n</pattern>
        </encoder>
    </appender>


    <appender name="AsyncFile" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>500</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="File" />
    </appender>

    <root level="INFO">
        <appender-ref ref="AsyncFile" />
    </root>

</configuration>
EOF

# setup jmxtrans
addgroup jmxtrans
adduser --system --home ${JMXTRANS_HOME} --shell /bin/bash --ingroup jmxtrans jmxtrans
chown -R jmxtrans:jmxtrans ${JMXTRANS_HOME}
chown -R jmxtrans:jmxtrans ${JMXTRANS_LOG}
chown -R jmxtrans:jmxtrans ${JMXTRANS_JSON}