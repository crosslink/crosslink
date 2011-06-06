#!/bin/bash

base=`dirname $0`

CLASSPATH=$base/lib/assessment-engine.jar:$base/lib/xml2txt.jar

if [ -e $base/bin ]; then
	CLASSPATH=$CLASSPATH:$base/bin
else
	if [ -e $base/dist/CrosslinkValidation.jar ]; then
		CLASSPATH=$CLASSHPATH:$base/dist/CrosslinkValidation.jar
	else
		CLASSPATH=$CLASSHPATH:$base/CrosslinkValidation.jar
	fi
fi

java -cp $CLASSPATH ltwassessmenttool.submission.RunChecker "$@"
