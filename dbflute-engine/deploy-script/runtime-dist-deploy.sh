#!/bin/bash

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Runtime Deploy to Maven Central (and to DBFlute Engine)
#
# precondition:
# o engine's runtime version in build.xml is same as runtime's pom.xml 
# o gpg key is needed as first argument of this shell
# _/_/_/_/_/_/_/_/_/_/

# dbflute-runtime's java version is fixed
if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

cd ../../dbflute-runtime

# deploy process
mvn -e clean deploy -Dgpg.keyname=$1

cd ../dbflute-engine
ant -f build.xml runtime-dist