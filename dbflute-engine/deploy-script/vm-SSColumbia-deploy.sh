cd ..
ant -f build.xml reflect-to-test-dbms-sqlserver

cd ../dbflute-test-dbms-sqlserver/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile
