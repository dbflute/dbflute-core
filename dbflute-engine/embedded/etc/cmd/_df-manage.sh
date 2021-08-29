#!/bin/bash

NATIVE_PROPERTIES_PATH=$1

FIRST_ARG=$2
SECOND_ARG=$3
taskReturnCode=0

if [ "$FIRST_ARG" = "" ];then
  echo "     |\  |-\ |-- |      |      "
  echo "     | | |-\ |-  | | | -+- /_\ "
  echo "     |/  |-/ |   | |_|  |  \-  "
  echo ""
  echo " <<< DB Task >>>"
  echo "   0 : replace-schema  => drop tables and create schema"
  echo "   1 : renewal         => call 0->21->22->23->25->24"
  echo "   4 : load-data-reverse  5 : schema-sync-check"
  echo "   7 : save-previous      8 : alter-check"
  echo ""
  echo " <<< Generate Task >>>"
  echo "   2 : regenerate  => call 21->22->23->25->24"
  echo "  21 : jdbc        22 : doc  23 : generate"
  echo "  24 : sql2entity  25 : outside-sql-test"
  echo ""
  echo " <<< Utility Task >>>"
  echo "  12 : freegen  => generate anything you like"
  echo "  88 : intro    => boot DBFlute Intro"
  echo ""
  echo " <<< Environment Task >>>"
  echo "  31 : sai      => download sai libraries"
  echo "  94 : upgrade  => upgrade DBFlute Engine"
  echo "  97 : help     => show list of all tasks"
  echo ""
  echo \(input on your console\)
  echo What is your favorite task? \(number\):

  read FIRST_ARG
fi

# you can specify plural tasks by comma string
#  e.g. manage.sh 21,22
IFS=,
for element in $FIRST_ARG
do
  sh $DBFLUTE_HOME/etc/cmd/_df-managedo.sh $NATIVE_PROPERTIES_PATH $element $SECOND_ARG
  taskReturnCode=$?
  if [ $taskReturnCode -ne 0 ];then
    exit $taskReturnCode;
  fi
done
