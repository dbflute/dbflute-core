cd ..
ant -f build.xml reflect-to-all-regulars
export answer=y

cd ../../dbflute-example-container/dbflute-seasar-example/dbflute_exampledb
rm ./log/*.log
. replace-schema.sh
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh

cd ../../dbflute-spring-example/dbflute_exampledb
rm ./log/*.log
. replace-schema.sh
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh

cd ../../dbflute-guice-example/dbflute_exampledb
rm ./log/*.log
. manage.sh renewal
. manage.sh load-data-reverse
. manage.sh schema-sync-check
. manage.sh freegen
. diffworld-test.sh
. sqlap-manage.sh regenerate

cd ../../dbflute-cdi-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../../dbflute-example-database/dbflute-mysql-example/dbflute_exampledb
rm ./log/*.log
. replace-schema.sh
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh
. manage.sh load-data-reverse
. manage.sh freegen

cd ../../dbflute-postgresql-example/dbflute_exampledb
rm ./log/*.log
. replace-schema.sh
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh
. manage.sh load-data-reverse

# needs environment set up so kick by yourself (refresh only here)
#cd ../../dbflute-oracle-example/dbflute_exampledb
#rm ./log/*.log
# needs environment set up so kick by yourself (refresh only here)
#. jdbc.sh
#. doc.sh
#. generate.sh
#. sql2entity.sh
#. outside-sql-test.sh
#. manage.sh refresh

cd ../../dbflute-db2-example/dbflute_exampledb
rm ./log/*.log
. manage.sh renewal

# needs environment set up so kick by yourself (refresh only here)
#cd ../../dbflute-sqlserver-example/dbflute_exampledb
#rm ./log/*.log
#. jdbc.sh
#. doc.sh
#. generate.sh
#. sql2entity.sh
#. outside-sql-test.sh
#. manage.sh refresh

# deploy only (cannot do tasks on Mac)
cd ../../dbflute-msaccess-example/dbflute_exampledb
rm ./log/*.log
. manage.sh refresh

cd ../../dbflute-sqlite-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh

cd ../../../dbflute-example-multipledb/dbflute-multipledb-seasar-example/dbflute_librarydb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
cd ../dbflute_memberdb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-multipledb-spring-example/dbflute_librarydb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
cd ../dbflute_memberdb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../../dbflute-example-friends-frank/dbflute-flexserver-example
. sync-lib.sh
cd dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-ymir-example
cd dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-sastruts-example
cd dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../../dbflute-example-database/dbflute-mysql-example/dbflute_exampledb
. bhvap-doc.sh
. bhvap-generate.sh
. bhvap-sql2entity.sh
. bhvap-outside-sql-test.sh

cd ../../dbflute-postgresql-example/dbflute_exampledb
. bhvap-doc.sh
. bhvap-generate.sh
. bhvap-sql2entity.sh
. bhvap-outside-sql-test.sh

cd ../../dbflute-sqlite-example/dbflute_exampledb
. bhvap-doc.sh
. bhvap-generate.sh
. bhvap-sql2entity.sh
. bhvap-outside-sql-test.sh

cd ../../../dbflute-example-container/dbflute-seasar-example/
mvn -e compile

cd ../../dbflute-spring-example/
mvn -e compile
