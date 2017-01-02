#!/bin/bash

if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

cd ..
ant -f build.xml reflect-to-test-dbms-sqlserver

cd ..
export answer=y

cd ../dbflute-test-dbms-sqlserver/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile
