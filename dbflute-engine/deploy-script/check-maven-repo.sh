
rm -R ~/.m2/repository/org/dbflute/dbflute-runtime/
rm -R ~/.m2/repository/org/dbflute/utflute/

ls
cd ../../
cd ../dbflute-test-active-dockside/
mvn -e compile
