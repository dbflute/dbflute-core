cd ..
ant -f build.xml dist
ant -f build.xml reflect-to-test-active-dockside

cd ..
export answer=y

cd ../dbflute-test-active-dockside/dbflute_maihamadb
rm ./log/*.log
. manage.sh regenerate
cd ..
