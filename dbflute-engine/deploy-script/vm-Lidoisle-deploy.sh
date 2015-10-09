cd ..
ant -f build.xml reflect-to-test-dbms-oracle

cd ..
export answer=y

cd ../dbflute-test-dbms-oracle/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
. diffworld-test.sh
cd ../dbflute_resortlinedb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile
