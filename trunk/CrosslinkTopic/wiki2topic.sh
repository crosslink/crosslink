#!/bin/bash

path=`dirname $0`

java -cp $path/bin crosslink.CrosslinkTopic "$@" 