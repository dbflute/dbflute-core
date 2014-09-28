#!/bin/bash

if [ -e ./extlib ]; then
  if [ "$(ls ./extlib | grep '.jar$')" != '' ]; then
    cp -Rf ./extlib $DBFLUTE_HOME/lib/extlib
  fi
fi
