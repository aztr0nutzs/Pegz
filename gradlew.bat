\
@echo off
set DIR=%~dp0
set CLASSPATH=%DIR%\gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
  echo ERROR: Missing gradle\wrapper\gradle-wrapper.jar
  echo Open in Android Studio to regenerate the wrapper, or run: gradle wrapper
  exit /b 1
)
"%JAVA_HOME%\bin\java.exe" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
