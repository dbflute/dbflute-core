
if exist .\extlib\*.jar (
  xcopy /c /e /i /y /z .\extlib %DBFLUTE_HOME%\lib\extlib
)