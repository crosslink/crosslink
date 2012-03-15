#!/bin/bash

program=`basename $0`
echo "running $program"
file_name=`echo $program | cut -f 1 -d .`
log_file=${file_name}.log
task=`echo $file_name | cut -f 2 -d "-"`
out_file=GroundTruth${task}.xml

java -cp bin:lib/assessment-engine.jar crosslink.CrosslinkMining en:ko \
 ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all \
  ~/experiments/ntcir-9-clld/topics/test/en \
~/experiments/ntcir-9-clld/topics/test/ko  /data/corpus/wikipedia/all/ run  1>$out_file 
