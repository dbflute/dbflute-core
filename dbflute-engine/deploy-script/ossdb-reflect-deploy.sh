#!/bin/bash

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Distribution and OpenSource DB Deploy
#
# only test projects for OSS DBs are deployed, e.g. mysql, postgresql
# full tasks are execute in the target projects
#
# precondition:
# o target projects are git-cloned at the next directory of dbflute-core 
# _/_/_/_/_/_/_/_/_/_/

# should be java8 when deploy script
# other version branches are manually merged after that
if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

# to dbflute-engine directory
cd ..

ant -f build.xml dist
ant -f build.xml reflect-to-test-active-dockside
ant -f build.xml reflect-to-test-active-hangar
ant -f build.xml reflect-to-test-dbms-mysql
ant -f build.xml reflect-to-test-dbms-postgresql
ant -f build.xml reflect-to-test-dbms-derby
ant -f build.xml reflect-to-test-dbms-sqlite
ant -f build.xml reflect-to-test-option-compatible10x
# to manage indivisually
#ant -f build.xml reflect-to-example-on-play2java
#ant -f build.xml reflect-to-example-on-springboot
#ant -f build.xml reflect-to-example-with-non-rdb
#ant -f build.xml reflect-to-example-with-remoteapi-gen
ant -f build.xml reflect-to-howto

# to dbflute-core directory
cd ..
