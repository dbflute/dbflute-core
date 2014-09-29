cd ../../dbflute-runtime

# deploy process
mvn -e clean deploy

# to avoid snapshot-unresolved problem
mvn -e install

cd ../dbflute-engine
ant -f build.xml runtime-dist