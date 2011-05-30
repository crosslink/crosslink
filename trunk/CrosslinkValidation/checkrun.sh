#!/bin/bash

CLASSPATH=lib/assessment-engine.jar:lib/xml2txt.jar

if [ -e bin ]; then
	CLASSPATH=$CLASSPATH:bin
else
	if [ -e dist/CrosslinkValidation.jar ]; then
		CLASSPATH=$CLASSHPATH:dist/CrosslinkValidation.jar
	else
		CLASSPATH=$CLASSHPATH:CrosslinkValidation.jar
	fi
fi

java -cp $CLASSPATH ltwassessmenttool.submission.RunChecker "$@"
