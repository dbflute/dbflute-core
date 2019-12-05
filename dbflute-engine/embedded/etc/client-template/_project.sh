#!/bin/bash

export ANT_OPTS=-Xmx512m

export DBFLUTE_HOME=../mydbflute/dbflute-@dbflute.version@

export MY_PROPERTIES_PATH=build.properties

JAVA_VERSION=1.8+

# Check JAVA_HOME exists
if [[ -z "${JAVA_HOME}" ]]; then
  if [[ `uname` = "Darwin" ]]; then
    export JAVA_HOME=$(/usr/libexec/java_home -v ${JAVA_VERSION})
  else
    echo "JAVA_HOME is not specified." >&2
    exit 1
  fi
fi

# Check executable
if [[ ! -x ${JAVA_HOME}/bin/java ]]; then
  echo "Could not find Java executable in ${JAVA_HOME}" >&2
  exit 1
fi

