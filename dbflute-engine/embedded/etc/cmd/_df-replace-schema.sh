#!/bin/bash

ANT_HOME=$DBFLUTE_HOME/ant
NATIVE_PROPERTIES_PATH=$1
VARYING_ARG=$2

sh $DBFLUTE_HOME/etc/cmd/_df-copy-properties.sh $NATIVE_PROPERTIES_PATH

antReturnCode=0
sh $DBFLUTE_HOME/etc/cmd/_df-copy-extlib.sh

sh $DBFLUTE_HOME/ant/bin/ant -Ddfenv=$DBFLUTE_ENVIRONMENT_TYPE -Ddfans=$answer -Ddfvarg=$VARYING_ARG -f $DBFLUTE_HOME/build-torque.xml replace-schema
antReturnCode=$?

sh $DBFLUTE_HOME/etc/cmd/_df-delete-extlib.sh

if [ $antReturnCode -ne 0 ];then
  exit $antReturnCode;
fi
