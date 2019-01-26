
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

for %%I IN ( %DBFLUTE_HOME% ) do set "MYDBFLUTE_DIR=%%~dpI"
if exist %MYDBFLUTE_DIR%working_patched_dbflute (
  echo ...Switching current engine to patched engine of same version
  move %DBFLUTE_HOME% %MYDBFLUTE_DIR%working_old_dbflute
  move %MYDBFLUTE_DIR%working_patched_dbflute %DBFLUTE_HOME%
  if exist %DBFLUTE_HOME%\build-torque.xml (
    rmdir /q /s %MYDBFLUTE_DIR%working_old_dbflute
  )
)
