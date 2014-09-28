cd ..
ant -f build.xml reflect-to-multipledb

cd ../../dbflute-example-multipledb/dbflute-multipledb-seasar-example/dbflute_librarydb
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
