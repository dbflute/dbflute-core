cd ..
ant -f build.xml dist
ant -f build.xml reflect-to-test-option-compatible10x
ant -f build.xml reflect-to-test-active-dockside
ant -f build.xml reflect-to-test-active-hanger

cd ..
export answer=y

cd ../dbflute-test-option-compatible10x/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-active-dockside/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile

cd ../dbflute-test-active-hanger/dbflute_maihamadb
rm ./log/*.log
. manage.sh renewal
cd ..
mvn -e compile
