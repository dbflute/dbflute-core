#!/bin/bash

export ANT_OPTS=-Xmx512m

export DBFLUTE_HOME=../mydbflute/dbflute-@dbflute.version@

export MY_PROPERTIES_PATH=build.properties

JAVA_VERSION=1.8+

# auto-setting for JAVA_HOME (keeping exiting JAVA_HOME)
if [[ -z "${JAVA_HOME}" ]]; then
  if [[ `uname` = "Darwin" ]]; then
    export JAVA_HOME=$(/usr/libexec/java_home -v ${JAVA_VERSION})
  fi
fi
