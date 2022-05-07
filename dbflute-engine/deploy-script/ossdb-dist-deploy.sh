#!/bin/bash

if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

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

cd ..
export answer=y

cd ../dbflute-test-active-dockside/dbflute_maihamadb
rm ./log/*.log
. manage.sh replace-schema
. manage.sh jdbc,doc
. manage.sh generate,sql2entity,outside-sql-test
cd ..
mvn -e compile

cd ../dbflute-test-active-hangar/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
. manage.sh freegen
. diffworld-test.sh
. lrevworld-test.sh
. syncworld-test.sh
cd ../dbflute_resortlinedb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-mysql/dbflute_maihamadb
rm ./log/*.log
. nextdb-renewal.sh
. slave-replace-schema.sh
. manage.sh renewal
. manage.sh load-data-reverse
cd ../dbflute_resortlinedb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-postgresql/dbflute_maihamadb
rm ./log/*.log
. nextschema-renewal.sh
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-derby/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-sqlite/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ../dbflute_readonlydb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile
cd ../dbflute-test-dbms-derby/dbflute_maihamadb
. manage.sh refresh
cd ..

cd ../dbflute-test-option-compatible10x/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-howto/dbflute_maihamadb
rm ./log/*.log
. manage.sh replace-schema
. manage.sh jdbc,doc
. manage.sh generate,sql2entity,outside-sql-test
cd ..
mvn -e compile
