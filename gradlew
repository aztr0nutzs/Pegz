#!/bin/sh
# Minimal Gradle wrapper script placeholder.
# If gradle-wrapper.jar is missing, regenerate wrapper from Android Studio:
#   File -> New -> New Project from Existing Sources (or "Add Gradle Wrapper")
# Or from a machine with Gradle installed:
#   gradle wrapper --gradle-version 8.7

DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_CMD=${JAVA_HOME:+$JAVA_HOME/bin/}java

CLASSPATH="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: Missing gradle/wrapper/gradle-wrapper.jar"
  echo "Open in Android Studio to regenerate the wrapper, or run: gradle wrapper"
  exit 1
fi

exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
