#!/bin/bash

wd=`dirname $0`
java -cp $wd/bin:$wd/../AssessmentEngine/bin:$wd/../CrosslinkMining/bin wikipedia.GetTitle "$@" 