#!/bin/bash

if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

cd ../../dbflute-runtime

# deploy process
mvn -e clean deploy -Dgpg.keyname=$1

cd ../dbflute-engine
ant -f build.xml runtime-dist