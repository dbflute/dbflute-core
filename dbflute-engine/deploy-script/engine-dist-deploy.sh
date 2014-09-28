cd ..
ant -f build.xml dist
ant -f build.xml reflect-to-test-active-dockside
ant -f build.xml reflect-to-test-active-hanger

cd ../../dbflute-test-active-dockside/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
cd ..
mvn -e compile

cd ../dbflute-test-active-hanger/dbflute_exampledb
rm ./log/*.log
. manage.sh regenerate
cd ..
mvn -e compile
