#!/bin/bash

for i in zh ja ko
do
   echo $i
   cat /home/monfee/experiments/ntcir-9-clld/topics/test/topic-ids.txt | while read line
   do
   file=resources/Pool/${i}/wikipedia_pool_${line}.xml
   num=`grep "<tobep" $file | wc -l`
   echo $line,$num
   done

   echo
done
