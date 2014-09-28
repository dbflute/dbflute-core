#!/bin/bash

ANT_HOME=$DBFLUTE_HOME/ant
NATIVE_PROPERTIES_PATH=$1

sh $DBFLUTE_HOME/etc/cmd/_df-copy-properties.sh $NATIVE_PROPERTIES_PATH

sh $DBFLUTE_HOME/etc/cmd/_df-copy-extlib.sh

sh $DBFLUTE_HOME/ant/bin/ant -Ddfenv=$DBFLUTE_ENVIRONMENT_TYPE -Ddfdir=$2 -f $DBFLUTE_HOME/build-torque.xml take-assert
antReturnCode=$?

sh $DBFLUTE_HOME/etc/cmd/_df-delete-extlib.sh

if [ $antReturnCode -ne 0 ];then
  exit $antReturnCode;
fi
