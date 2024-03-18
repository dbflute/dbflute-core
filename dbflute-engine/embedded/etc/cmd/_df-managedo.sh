#!/bin/bash

NATIVE_PROPERTIES_PATH=$1

FIRST_ARG=$2
SECOND_ARG=$3
taskReturnCode=0

if [ "$FIRST_ARG" = "0" ];then
  FIRST_ARG=replace-schema
elif [ "$FIRST_ARG" = "1" ];then
  FIRST_ARG=renewal
elif [ "$FIRST_ARG" = "2" ];then
  FIRST_ARG=regenerate
elif [ "$FIRST_ARG" = "4" ];then
  FIRST_ARG=load-data-reverse
elif [ "$FIRST_ARG" = "5" ];then
  FIRST_ARG=schema-sync-check
elif [ "$FIRST_ARG" = "7" ];then
  FIRST_ARG=save-previous
elif [ "$FIRST_ARG" = "8" ];then
  FIRST_ARG=alter-check
elif [ "$FIRST_ARG" = "11" ];then
  FIRST_ARG=refresh
elif [ "$FIRST_ARG" = "12" ];then
  FIRST_ARG=freegen
elif [ "$FIRST_ARG" = "13" ];then
  FIRST_ARG=take-assert
elif [ "$FIRST_ARG" = "21" ];then
  FIRST_ARG=jdbc
elif [ "$FIRST_ARG" = "22" ];then
  FIRST_ARG=doc
elif [ "$FIRST_ARG" = "23" ];then
  FIRST_ARG=generate
elif [ "$FIRST_ARG" = "24" ];then
  FIRST_ARG=sql2entity
elif [ "$FIRST_ARG" = "25" ];then
  FIRST_ARG=outside-sql-test
elif [ "$FIRST_ARG" = "31" ];then
  FIRST_ARG=sai
elif [ "$FIRST_ARG" = "88" ];then
  FIRST_ARG=intro
elif [ "$FIRST_ARG" = "94" ];then
  FIRST_ARG=upgrade
elif [ "$FIRST_ARG" = "97" ];then
  FIRST_ARG=help
fi

if [ "$FIRST_ARG" = "replace-schema" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the ReplaceSchema task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-replace-schema.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "renewal" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the Renewal task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-renewal.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "regenerate" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the Regenerate task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-regenerate.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "load-data-reverse" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the LoadDataReverse task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-doc.sh $NATIVE_PROPERTIES_PATH load-data-reverse $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "schema-sync-check" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the SchemaSyncCheck task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-doc.sh $NATIVE_PROPERTIES_PATH schema-sync-check $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "alter-check" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the AlterCheck task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-replace-schema.sh $NATIVE_PROPERTIES_PATH alter-check $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "save-previous" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the SavePrevious task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-replace-schema.sh $NATIVE_PROPERTIES_PATH save-previous $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "refresh" ];then

  if [ "$2" = "" ];then
    echo \(input on your console\)
    echo What is refresh project? \(name\):
    read SECOND_ARG
  fi

  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the Refresh task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-refresh.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "freegen" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the FreeGen task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-freegen.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "take-assert" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the TakeAssert task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-take-assert.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "jdbc" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the JDBC task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-jdbc.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "doc" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the Doc task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-doc.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "generate" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the Generate task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-generate.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "sql2entity" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the Sql2Entity task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-sql2entity.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "outside-sql-test" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the OutsideSqlTest task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-outside-sql-test.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "sai" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the sai Download task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-sai-download.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "intro" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the DBFluteIntro task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-intro.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "upgrade" ];then
  echo "/nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"
  echo "...Calling the DBFlute Upgrade task"
  echo "nnnnnnnnnn/"
  sh $DBFLUTE_HOME/etc/cmd/_df-upgrade.sh $NATIVE_PROPERTIES_PATH $SECOND_ARG
  taskReturnCode=$?

elif [ "$FIRST_ARG" = "help" ];then
  echo " [DB Task] => after changing database, with replacing your database"
  echo "   0 : replace-schema    => drop tables and re-create schema (needs settings)"
  echo "   1 : renewal           => replace-schema and generate all (call 0->21->22->23->25->24)"
  echo "   4 : load-data-reverse => reverse data to excel for e.g. ReplaceSchema"
  echo "   5 : schema-sync-check => check difference between two schemas"
  echo "   7 : save-previous     => save previous DDLs for AlterCheck"
  echo "   8 : alter-check       => check alter DDLs with previous and next DDLs"
  echo "  13 : take-assert       => execute assertion SQL of TakeFinally"
  echo ""
  echo " [Generate Task] => generate class files and documents by schema meta data"
  echo "   2 : regenerate       => generate all (call 21->22->23->25->24)"
  echo "  21 : jdbc             => get meta data from schema (before doc and generate)"
  echo "  22 : doc              => generate documents e.g. SchemaHTML, HistoryHTML"
  echo "  23 : generate         => generate class files for tables"
  echo "  24 : sql2entity       => generate class files for OutsideSql"
  echo "  25 : outside-sql-test => check OutsideSql (execute SQLs, expect no error)"
  echo ""
  echo " [Utility Task]"
  echo "  12 : freegen => generate something by free template"
  echo "  88 : intro   => boot DBFluteIntro that provides GUI control"
  echo ""
  echo " [Environment Task]"
  echo "  11 : refresh => request refresh (F5) to IDE e.g. Eclipse"
  echo "  31 : sai     => download sai libraries to extlib for JavaScript"
  echo "  94 : upgrade => upgrade DBFlute module to new version (except runtime)"
  echo "  97 : help    => show description of tasks"
  taskReturnCode=0

fi

if [ $taskReturnCode -ne 0 ];then
  exit $taskReturnCode;
fi
