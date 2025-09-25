#!/bin/bash
cd `dirname $0`

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Runtime Deploy to local environment (Not to Maven Central)
#
# precondition:
# o engine's runtime version in build.xml is same as runtime's pom.xml 
# _/_/_/_/_/_/_/_/_/_/

# also to dbflute-engine directory
. _prepare-build.sh

cd ../dbflute-runtime

# deploy process
mvn -e clean package

cd ../dbflute-engine
ant -f build.xml runtime-dist