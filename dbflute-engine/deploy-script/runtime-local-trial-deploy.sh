#!/bin/bash

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Runtime Deploy to local environment (Not to Maven Central)
#
# precondition:
# o engine's runtime version in build.xml is same as runtime's pom.xml 
# _/_/_/_/_/_/_/_/_/_/

# dbflute-runtime's java version is fixed
if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

cd ../../dbflute-runtime

# deploy process
mvn -e clean package

cd ../dbflute-engine
ant -f build.xml runtime-dist