#!/bin/bash

lang_pair=$1
run_path=$2

java -cp bin:../AssessmentEngine/dist/assessment-engine.jar crosslink.RunDescription2Table -l $lang_pair $run_path
