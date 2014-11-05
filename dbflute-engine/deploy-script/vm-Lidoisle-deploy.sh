cd ..
ant -f build.xml reflect-to-test-dbms-oracle

cd ../dbflute-test-dbms-oracle/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
. diffworld-test.sh
