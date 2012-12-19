#!/bin/bash

program=`basename $0`
echo "running $program"
file_name=`echo $program | cut -f 1 -d .`
log_file=${file_name}.log

S_CODE=
T_CODE=

for i in 1 2
do
if [ "$i" == "1" ]
then
SOURCE_LANG="en"
else
TARGET_LANG="en"
fi

for j in zh ja ko
do

if [ "$i" == "1" ]
then
TARGET_LANG=${j}
else
SOURCE_LANG=${j}
fi

if [ "$SOURCE_LANG" == "zh" ]
then
  S_CODE="Zh"
fi
if [ "$SOURCE_LANG" == "en" ]
then
  S_CODE="En"
fi
if [ "$SOURCE_LANG" == "ja" ]
then
  S_CODE="Ja"
fi
if [ "$SOURCE_LANG" == "ko" ]
then
  S_CODE="Ko"
fi

if [ "$TARGET_LANG" == "zh" ]
then
  T_CODE="Zh"
fi
if [ "$TARGET_LANG" == "en" ]
then
  T_CODE="En"
fi
if [ "$TARGET_LANG" == "ja" ]
then
  T_CODE="Ja"
fi
if [ "$TARGET_LANG" == "ko" ]
then
  T_CODE="Ko"
fi

task=${S_CODE}2${T_CODE}
out_file=GroundTruth${task}.xml

topic_path=`ls -d ~/experiments/ntcir-10-crosslink/topics/official/${SOURCE_LANG}*`
counter_path=`ls -d ~/experiments/ntcir-10-crosslink/topics/official/${TARGET_LANG}*`

java -cp bin:lib/assessment-engine.jar crosslink.CrosslinkMining ${SOURCE_LANG}:${TARGET_LANG} \
 ~/experiments/ntcir-10-crosslink/assessment/wikipedia_groundtruth/link-mining/all \
  $topic_path \
 $counter_path  /data/corpus/wikipedia/all/ run  1>$out_file

done
done
