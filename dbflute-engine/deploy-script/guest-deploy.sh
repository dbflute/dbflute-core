cd ..
ant -f build.xml reflect-to-hibernate
ant -f build.xml reflect-to-s2jdbc
ant -f build.xml reflect-to-doma

cd ../../dbflute-example-friends-guest/dbflute-hibernate-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh

cd ../../dbflute-s2jdbc-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-doma-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. outside-sql-test.sh