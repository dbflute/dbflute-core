@echo off

set ANT_OPTS=-Xmx512m

set DBFLUTE_HOME=..\mydbflute\dbflute-@dbflute.version@

set MY_PROJECT_NAME=dfclient

set MY_PROPERTIES_PATH=build.properties

if "%pause_at_end%"=="" set pause_at_end=y
