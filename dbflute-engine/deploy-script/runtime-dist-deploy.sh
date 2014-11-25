cd ../../dbflute-runtime

# deploy process
mvn -e clean deploy

cd ../dbflute-engine
ant -f build.xml runtime-dist -Dgpg.keyname=$1