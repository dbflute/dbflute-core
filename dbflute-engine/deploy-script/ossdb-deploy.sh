cd ..
ant -f build.xml reflect-to-mysql
ant -f build.xml reflect-to-postgresql
ant -f build.xml reflect-to-sqlite

cd ../../dbflute-example-database/dbflute-mysql-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh

cd ../../dbflute-postgresql-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh

cd ../../dbflute-sqlite-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
# unsupported at SQLite
#. outside-sql-test.sh
