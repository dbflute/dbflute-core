#!/bin/bash

if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

cd ..
ant -f build.xml stage
ant -f buildnet.xml stage