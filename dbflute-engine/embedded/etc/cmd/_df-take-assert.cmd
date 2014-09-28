
setlocal
set ANT_HOME=%DBFLUTE_HOME%\ant
set NATIVE_PROPERTIES_PATH=%1
if "%DBFLUTE_ENVIRONMENT_TYPE%"=="" set DBFLUTE_ENVIRONMENT_TYPE=""
set DBFLUTE_SQL_ROOT_DIR=%2
if "%DBFLUTE_SQL_ROOT_DIR%"=="" set DBFLUTE_SQL_ROOT_DIR=""

call %DBFLUTE_HOME%\etc\cmd\_df-copy-properties.cmd %NATIVE_PROPERTIES_PATH%

call %DBFLUTE_HOME%\etc\cmd\_df-copy-extlib.cmd

call %DBFLUTE_HOME%\ant\bin\ant -Ddfenv=%DBFLUTE_ENVIRONMENT_TYPE% -Ddfdir=%DBFLUTE_SQL_ROOT_DIR% -f %DBFLUTE_HOME%\build-torque.xml take-assert

call %DBFLUTE_HOME%\etc\cmd\_df-delete-extlib.cmd
