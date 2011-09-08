#!/bin/bash

java -cp bin:lib/assessment-engine.jar:lib/xml2txt.jar crosslink.tools.ResultSetAnalyser "$@"
