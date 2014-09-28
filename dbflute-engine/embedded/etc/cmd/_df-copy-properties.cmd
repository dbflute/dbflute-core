
set NATIVE_PROPERTIES_PATH=%1
if not exist build.properties (
  ren %NATIVE_PROPERTIES_PATH% build.properties
)
