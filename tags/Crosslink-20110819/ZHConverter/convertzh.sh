#!/bin/bash

home=`dirname $0`
jarfile=${home}/dist/ZHConverter.jar

if ! [ -e  $jarfile ]
then
	ant dist
fi

java -jar $jarfile "$@"