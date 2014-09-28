
setlocal
set ANT_HOME=%DBFLUTE_HOME%\ant
set NATIVE_PROPERTIES_PATH=%1
if "%DBFLUTE_ENVIRONMENT_TYPE%"=="" set DBFLUTE_ENVIRONMENT_TYPE=""
set DBFLUTE_UPGRADE_VERSION=%2
if "%DBFLUTE_UPGRADE_VERSION%"=="" set DBFLUTE_UPGRADE_VERSION=""

call %DBFLUTE_HOME%\etc\cmd\_df-copy-properties.cmd %NATIVE_PROPERTIES_PATH%

call %DBFLUTE_HOME%\etc\cmd\_df-copy-extlib.cmd

call %DBFLUTE_HOME%\ant\bin\ant -Ddfenv=%DBFLUTE_ENVIRONMENT_TYPE% -Ddfver=%DBFLUTE_UPGRADE_VERSION% -f %DBFLUTE_HOME%\build-torque.xml upgrade

call %DBFLUTE_HOME%\etc\cmd\_df-delete-extlib.cmd
