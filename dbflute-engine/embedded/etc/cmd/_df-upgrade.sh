#!/bin/bash

ANT_HOME=$DBFLUTE_HOME/ant
NATIVE_PROPERTIES_PATH=$1

sh $DBFLUTE_HOME/etc/cmd/_df-copy-properties.sh $NATIVE_PROPERTIES_PATH

sh $DBFLUTE_HOME/etc/cmd/_df-copy-extlib.sh

sh $DBFLUTE_HOME/ant/bin/ant -Ddfenv=$DBFLUTE_ENVIRONMENT_TYPE -Ddfver=$2 -f $DBFLUTE_HOME/build-torque.xml upgrade
antReturnCode=$?

sh $DBFLUTE_HOME/etc/cmd/_df-delete-extlib.sh

if [ $antReturnCode -ne 0 ];then
  exit $antReturnCode;
fi

MYDBFLUTE_DIR=$(dirname $DBFLUTE_HOME)
if [ -d $MYDBFLUTE_DIR/working_patched_dbflute ];then
  echo "...Switching current engine to patched engine of same version"
  mv -f $DBFLUTE_HOME $MYDBFLUTE_DIR/working_old_dbflute
  mv -f $MYDBFLUTE_DIR/working_patched_dbflute $DBFLUTE_HOME
  if [ -f $DBFLUTE_HOME/build-torque.xml ];then
    rm -Rf $MYDBFLUTE_DIR/working_old_dbflute
  fi
fi
