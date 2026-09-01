#!/bin/sh
#
# Copyright (c) 2007-present Gradle Inc. — Apache 2.0
#
# Gradle start up script for UN*X environments
# (standard gradle-wrapper script)
# Self-extracting stub; downloads gradle-wrapper.jar on first run.

set -e

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support
cygwin=false
darwin=false
nonstop=false
case "$(uname)" in
    CYGWIN* ) cygwin=true ;;
    Darwin* ) darwin=true ;;
    NONSTOP* ) nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the java command to use
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1 ; then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    fi
fi

# For Cygwin or MSYS, convert paths
if [ "$cygwin" = "false" ] && [ "$darwin" = "false" ] ; then
    case "$(uname -s)" in
        *CYGWIN*|*MSYS*|MINGW*) ;;
    esac
fi

# Add default JVM options here
DEFAULT_JVM_OPTS=""

exec "$JAVACMD" \
    $DEFAULT_JVM_OPTS \
    $JAVA_OPTS \
    $GRADLE_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
