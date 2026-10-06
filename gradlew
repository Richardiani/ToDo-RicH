#!/usr/bin/env bash
APP_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
APP_HOME=${APP_HOME%/}
DIR="${APP_HOME}/.gradle"
if [ ! -d "$DIR" ]; then
    mkdir -p "$DIR"
fi

exec "$APP_HOME/gradlew" "$@"
