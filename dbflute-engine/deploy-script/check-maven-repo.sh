
# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# Check of Maven Central Repository for DBFlute Runtime
#
# precondition:
# o dbflute-runtime that the dockside project uses is already deployed 
# o the dockside project are git-cloned at the next directory of dbflute-core
# _/_/_/_/_/_/_/_/_/_/

rm -R ~/.m2/repository/org/dbflute/dbflute-runtime/
rm -R ~/.m2/repository/org/dbflute/utflute/

ls
cd ../../
cd ../dbflute-test-active-dockside/

# expect correct download
mvn -e compile
