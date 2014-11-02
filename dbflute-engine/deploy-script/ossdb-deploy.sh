cd ..
ant -f build.xml dist
ant -f build.xml reflect-to-test-dbms-mysql
ant -f build.xml reflect-to-test-dbms-postgresql

cd ..
export answer=y

cd ../dbflute-test-dbms-mysql/dbflute_maihamadb
rm ./log/*.log
. nextdb-renewal.sh
. slave-replace-schema.sh
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-dbms-postgresql/dbflute_maihamadb
rm ./log/*.log
# not replace-schema because of big data
. manage.sh regenerate
cd ..
mvn -e compile
