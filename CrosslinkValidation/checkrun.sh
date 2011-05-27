#!/bin/bash

CLASSPATH=lib/assessment-engine.jar:lib/xml2txt.jar:lib/xercesImpl.jar:lib/jdom.jar

if [ -e bin ]; then
	CLASSPATH=$CLASSHPATH:bin
else
	if [ -e dist/CrosslinkValidation.jar ]; then
		CLASSPATH=$CLASSHPATH:dist/CrosslinkValidation.jar
	else
		CLASSPATH=$CLASSHPATH:CrosslinkValidation.jar
	fi
fi

java -cp $CLASSPATH ltwassessmenttool.submission.RunChecker "$@"