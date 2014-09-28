
setlocal
set ANT_HOME=%DBFLUTE_HOME%\ant
set NATIVE_PROPERTIES_PATH=%1
if "%DBFLUTE_ENVIRONMENT_TYPE%"=="" set DBFLUTE_ENVIRONMENT_TYPE=""
set VARYING_ARG=%2
if "%VARYING_ARG%"=="" set VARYING_ARG=""

call %DBFLUTE_HOME%\etc\cmd\_df-copy-properties.cmd %NATIVE_PROPERTIES_PATH%

call %DBFLUTE_HOME%\etc\cmd\_df-copy-extlib.cmd

call %DBFLUTE_HOME%\ant\bin\ant -Ddfenv=%DBFLUTE_ENVIRONMENT_TYPE% -Ddfvarg=%VARYING_ARG% -f %DBFLUTE_HOME%\build-torque.xml doc

call %DBFLUTE_HOME%\etc\cmd\_df-delete-extlib.cmd
