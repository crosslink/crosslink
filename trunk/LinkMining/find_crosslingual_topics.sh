#!/bin/bash

# find the cross-lingual counterparts for the given topics 

topic_path=$1

# counterparts path
cp_topic_path=$2

# the cross-lingual link table
# format: 
# <source_document>:<target_document>:<source_document_title>|<target_document_title>
# 
cl_table_path=$3

if [ -e $cl_table_path ]
then
	echo "the cross-lingual link table: \"$cl_table_path\" does't not exist"
	exit
fi

for file in `ls $topic_path`
do

	input=echo $file
	
	len=${#input}
	
	#echo $len
	
	
	subdir=
	
	if [ "$len" -gt "3" ]
	then
	subdir=${input: -3}
	else
	subdir=`printf "%03d" $input`
	fi
	
	#echo $subdir
	
	filename=$subdir/${input}.xml
	
	filename="pages/$filename"
	#; cat $filename | head -n 50
	echo $filename

done