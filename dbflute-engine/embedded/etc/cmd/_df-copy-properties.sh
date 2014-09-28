#!/bin/bash

NATIVE_PROPERTIES_PATH=$1
if ! [ -e build.properties ]; then
  mv $NATIVE_PROPERTIES_PATH build.properties
fi
