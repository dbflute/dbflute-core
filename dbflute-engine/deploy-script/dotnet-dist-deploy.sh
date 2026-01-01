#!/bin/bash
cd `dirname $0`

# also to dbflute-engine directory
. _prepare-build.sh

ant -f buildnet.xml dist
