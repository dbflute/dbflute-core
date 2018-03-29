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
ant -f build.xml reflect-to-test-dbms-oracle
ant -f build.xml reflect-to-test-dbms-db2
ant -f build.xml reflect-to-test-dbms-sqlserver
ant -f build.xml reflect-to-test-dbms-derby
ant -f build.xml reflect-to-test-dbms-sqlite
ant -f build.xml reflect-to-test-option-compatible10x
ant -f build.xml reflect-to-example-on-parade
ant -f build.xml reflect-to-example-on-springboot
ant -f build.xml reflect-to-example-on-play2java
ant -f build.xml reflect-to-example-with-non-rdb
ant -f build.xml reflect-to-example-with-doma
ant -f build.xml reflect-to-howto

cd ..
export answer=y

cd ../dbflute-test-active-dockside/dbflute_maihamadb
rm ./log/*.log
. manage.sh replace-schema
. manage.sh jdbc,doc
. manage.sh generate,outside-sql-test,sql2entity
cd ..
mvn -e compile

cd ../dbflute-test-active-hangar/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
. manage.sh load-data-reverse
. manage.sh freegen
. diffworld-test.sh
. syncworld-test.sh
cd ..
mvn -e compile

cd ../dbflute-test-dbms-mysql/dbflute_maihamadb
rm ./log/*.log
. nextdb-renewal.sh
. slave-replace-schema.sh
. manage.sh renewal
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

cd ../dbflute-test-dbms-oracle/dbflute_maihamadb
rm ./log/*.log
. nextschema-renewal.sh
. manage.sh renewal
. diffworld-test.sh
cd ../dbflute_resortlinedb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-db2/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-sqlserver/dbflute_maihamadb
rm ./log/*.log
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

cd ../dbflute-example-on-parade/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal,freegen
cd ..
mvn -e compile

cd ../dbflute-example-on-springboot/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-example-on-play2java/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-example-with-non-rdb/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal,freegen
cd ..
mvn -e compile

cd ../dbflute-example-with-doma/dbflute_domadb
rm ./log/*.log
. manage.sh replace-schema
. manage.sh jdbc
. manage.sh doc
cd ..
mvn -e compile

cd ../dbflute-howto/dbflute_maihamadb
rm ./log/*.log
. manage.sh replace-schema
. manage.sh jdbc,doc
. manage.sh generate,sql2entity,outside-sql-test
cd ..
mvn -e compile
