cd ../../dbflute-runtime

# deploy process
mvn -e clean deploy -Dgpg.keyname=$1

cd ../dbflute-engine
ant -f build.xml runtime-dist