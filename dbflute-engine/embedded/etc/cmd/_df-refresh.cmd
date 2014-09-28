
setlocal
set ANT_HOME=%DBFLUTE_HOME%\ant
set NATIVE_PROPERTIES_PATH=%1
if "%DBFLUTE_ENVIRONMENT_TYPE%"=="" set DBFLUTE_ENVIRONMENT_TYPE=""
set DBFLUTE_SPECIFIED_PROJECT=%2
if "%DBFLUTE_SPECIFIED_PROJECT%"=="" set DBFLUTE_SPECIFIED_PROJECT=""

call %DBFLUTE_HOME%\etc\cmd\_df-copy-properties.cmd %NATIVE_PROPERTIES_PATH%

call %DBFLUTE_HOME%\etc\cmd\_df-copy-extlib.cmd

call %DBFLUTE_HOME%\ant\bin\ant -Ddfenv=%DBFLUTE_ENVIRONMENT_TYPE% -Ddfprj=%DBFLUTE_SPECIFIED_PROJECT% -f %DBFLUTE_HOME%\build-torque.xml refresh

call %DBFLUTE_HOME%\etc\cmd\_df-delete-extlib.cmd
