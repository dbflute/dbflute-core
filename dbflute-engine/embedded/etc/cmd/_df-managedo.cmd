
setlocal
set NATIVE_PROPERTIES_PATH=%1
set FIRST_ARG=%2
set SECOND_ARG=%3
set PURE_FIRST_ARG=%2

:: for compatibility
if "%FIRST_ARG%"=="""" (
  set FIRST_ARG=
)
if "%SECOND_ARG%"=="""" (
  set SECOND_ARG=
)
if "%PURE_FIRST_ARG%"=="""" (
  set PURE_FIRST_ARG=
)

if "%FIRST_ARG%"=="0" (
  set FIRST_ARG=replace-schema
) else if "%FIRST_ARG%"=="1" (
  set FIRST_ARG=renewal
) else if "%FIRST_ARG%"=="2" (
  set FIRST_ARG=regenerate
) else if "%FIRST_ARG%"=="4" (
  set FIRST_ARG=load-data-reverse
) else if "%FIRST_ARG%"=="5" (
  set FIRST_ARG=schema-sync-check
) else if "%FIRST_ARG%"=="7" (
  set FIRST_ARG=save-previous
) else if "%FIRST_ARG%"=="8" (
  set FIRST_ARG=alter-check
) else if "%FIRST_ARG%"=="11" (
  set FIRST_ARG=refresh
) else if "%FIRST_ARG%"=="12" (
  set FIRST_ARG=freegen
) else if "%FIRST_ARG%"=="13" (
  set FIRST_ARG=take-assert
) else if "%FIRST_ARG%"=="21" (
  set FIRST_ARG=jdbc
) else if "%FIRST_ARG%"=="22" (
  set FIRST_ARG=doc
) else if "%FIRST_ARG%"=="23" (
  set FIRST_ARG=generate
) else if "%FIRST_ARG%"=="24" (
  set FIRST_ARG=sql2entity
) else if "%FIRST_ARG%"=="25" (
  set FIRST_ARG=outside-sql-test
) else if "%FIRST_ARG%"=="88" (
  set FIRST_ARG=intro
) else if "%FIRST_ARG%"=="94" (
  set FIRST_ARG=upgrade
) else if "%FIRST_ARG%"=="97" (
  set FIRST_ARG=help
)

if "%FIRST_ARG%"=="replace-schema" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the ReplaceSchema task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-replace-schema.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="renewal" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the Renewal task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-renewal.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="regenerate" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the Regenerate task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-regenerate.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="load-data-reverse" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the LoadDataReverse task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-doc.cmd %NATIVE_PROPERTIES_PATH% load-data-reverse %SECOND_ARG%

) else if "%FIRST_ARG%"=="schema-sync-check" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the SchemaSyncCheck task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-doc.cmd %NATIVE_PROPERTIES_PATH% schema-sync-check %SECOND_ARG%

) else if "%FIRST_ARG%"=="alter-check" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the AlterCheck task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-replace-schema.cmd %NATIVE_PROPERTIES_PATH% alter-check %SECOND_ARG%

) else if "%FIRST_ARG%"=="save-previous" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the SavePrevious task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-replace-schema.cmd %NATIVE_PROPERTIES_PATH% save-previous %SECOND_ARG%

) else if "%FIRST_ARG%"=="refresh" (

  if "%PURE_FIRST_ARG%"=="" echo (input on your console^)
  if "%PURE_FIRST_ARG%"=="" echo What is refresh project? (name^):
  if "%PURE_FIRST_ARG%"=="" set /p SECOND_ARG=

  echo /nnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the Refresh task
  echo nnnnnnnnnn/
  setlocal enabledelayedexpansion
  call %DBFLUTE_HOME%\etc\cmd\_df-refresh.cmd %NATIVE_PROPERTIES_PATH% !SECOND_ARG!
  endlocal
) else if "%FIRST_ARG%"=="freegen" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the FreeGen task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-freegen.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="take-assert" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the TakeAssert task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-take-assert.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="jdbc" (
  echo /nnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the JDBC task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-jdbc.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="doc" (
  echo /nnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the Doc task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-doc.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="generate" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the Generate task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-generate.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="sql2entity" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the Sql2Entity task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-sql2entity.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="outside-sql-test" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the OutsideSqlTest task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-outside-sql-test.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="intro" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the DBFluteIntro task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-intro.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="upgrade" (
  echo /nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
  echo ...Calling the DBFlute Upgrade task
  echo nnnnnnnnnn/
  call %DBFLUTE_HOME%\etc\cmd\_df-upgrade.cmd %NATIVE_PROPERTIES_PATH% %SECOND_ARG%

) else if "%FIRST_ARG%"=="help" (
  echo  [DB Change] =^> after changing database, with replacing your database
  echo    0 : replace-schema =^> drop tables and re-create schema (needs settings^)
  echo    1 : renewal        =^> replace-schema and generate all (call 0-^>21-^>22-^>23-^>25-^>24^)
  echo    7 : save-previous  =^> save previous DDLs for AlterCheck
  echo    8 : alter-check    =^> check alter DDLs with previous and next DDLs
  echo:
  echo  [Generate] =^> generate class files and documents by schema meta data
  echo    2 : regenerate       =^> generate all (call 21-^>22-^>23-^>24-^>25^)
  echo   21 : jdbc             =^> get meta data from schema (before doc and generate^)
  echo   22 : doc              =^> generate documents e.g. SchemaHTML, HistoryHTML
  echo   23 : generate         =^> generate class files for tables
  echo   24 : sql2entity       =^> generate class files for OutsideSql
  echo   25 : outside-sql-test =^> check OutsideSql (execute SQLs, expect no error^)
  echo:
  echo  [Utility] =^> various tasks
  echo    4 : load-data-reverse =^> reverse data to excel for e.g. ReplaceSchema
  echo    5 : schema-sync-check =^> check difference between two schemas
  echo   11 : refresh           =^> request refresh (F5^) to IDE e.g. Eclipse
  echo   12 : freegen           =^> generate something by free template
  echo   13 : take-assert       =^> execute assertion SQL of TakeFinally
  echo:
  echo   88 : intro   =^> boot DBFluteIntro that provides GUI control
  echo   94 : upgrade =^> upgrade DBFlute module to new version (except runtime^)
  echo   97 : help    =^> show description of tasks

)
