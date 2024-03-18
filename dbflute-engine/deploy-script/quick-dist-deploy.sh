#!/bin/bash

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Distribution and Quick Deploy
#
# for easy test while deplopment so simple process only
#
# precondition:
# o target projects are git-cloned at the next directory of dbflute-core 
# o the first ReplaceSchema after clone is already executed
# _/_/_/_/_/_/_/_/_/_/

# should be java8 when deploy script
# other version branches are manually merged after that
if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

# to dbflute-engine directory
cd ..

ant -f build.xml dist
ant -f build.xml reflect-to-test-active-dockside

# to dbflute-core directory
cd ..

# to execute ReplaceSchema without human confirmation
export answer=y

cd ../dbflute-test-active-dockside/dbflute_maihamadb
rm ./log/*.log
# no ReplaceSchema for quick dpeloy
. manage.sh regenerate
cd ..
