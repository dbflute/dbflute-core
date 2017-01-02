#!/bin/bash

if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

cd ..
ant -f buildnet.xml dist
