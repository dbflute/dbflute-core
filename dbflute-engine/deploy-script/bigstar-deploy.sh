cd ..
ant -f build.xml reflect-to-seasar
ant -f build.xml reflect-to-spring
ant -f build.xml reflect-to-guice
ant -f build.xml reflect-to-mysql
ant -f build.xml reflect-to-postgresql
export answer=y

cd ../../dbflute-example-container/dbflute-seasar-example/dbflute_exampledb
rm ./log/*.log
. manage.sh 0
. manage.sh 21
. manage.sh 22
. manage.sh 23
. manage.sh 24
. manage.sh 25

cd ../../dbflute-spring-example/dbflute_exampledb
rm ./log/*.log
. manage.sh replace-schema
. manage.sh jdbc
. manage.sh doc
. manage.sh generate
. manage.sh sql2entity
. manage.sh outside-sql-test

cd ../../dbflute-guice-example/dbflute_exampledb
rm ./log/*.log
. manage.sh renewal
. manage.sh load-data-reverse
. manage.sh schema-sync-check
. manage.sh freegen
. diffworld-test.sh

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
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh
. manage.sh load-data-reverse
