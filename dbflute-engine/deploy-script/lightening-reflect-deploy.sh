#!/bin/bash
cd `dirname $0`

# _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
# DBFlute Lightening Reflect(-only) Deploy
#
# for easy test while deplopment so simple process only
#
# precondition:
# o target projects are git-cloned at the next directory of dbflute-core 
# _/_/_/_/_/_/_/_/_/_/

# also to dbflute-engine directory
. _prepare-build.sh

# lightening dist and reflect
ant -f build.xml dist
ant -f build.xml reflect-to-test-active-dockside
ant -f build.xml reflect-to-test-active-hangar
