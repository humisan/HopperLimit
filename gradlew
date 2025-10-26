#!/bin/bash
# Gradle wrapper script for Linux/macOS

# Use local gradle if available
if [ -f /tmp/gradle_dl/gradle-8.10/bin/gradle ]; then
    /tmp/gradle_dl/gradle-8.10/bin/gradle "$@"
else
    # Fall back to system gradle
    gradle "$@"
fi
