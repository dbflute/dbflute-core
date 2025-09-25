#!/bin/bash
cd `dirname $0`

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Distribution and Quick Deploy
#
# for easy test while deplopment so simple process only
#
# precondition:
# o target projects are git-cloned at the next directory of dbflute-core 
# o the first ReplaceSchema after clone is already executed
# _/_/_/_/_/_/_/_/_/_/

# also to dbflute-engine directory
. _prepare-build.sh

# quick dist and reflect
ant -f build.xml dist
ant -f build.xml reflect-to-test-active-dockside
ant -f build.xml reflect-to-test-active-hangar

# to dbflute-core directory
cd ..

# to execute ReplaceSchema without human confirmation
export answer=y

cd ../dbflute-test-active-dockside/dbflute_maihamadb
rm ./log/*.log
# no ReplaceSchema for quick dpeloy
. manage.sh regenerate
cd ..

cd ../dbflute-test-active-hangar/dbflute_maihamadb
rm ./log/*.log
# no ReplaceSchema for quick dpeloy
. manage.sh regenerate
cd ..
