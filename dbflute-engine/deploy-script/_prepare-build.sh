#!/bin/bash

# should be java8 when deploy script
# other version branches are manually merged after that
if [ `uname` = "Darwin" ]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

# to dbflute-engine directory
cd ..

# use internal ant for build located in engine repository
export ANT_HOME=./etc/ant
export PATH=$PATH:$ANT_HOME/bin
chmod -R 755 $ANT_HOME/bin/*
