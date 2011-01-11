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

corpus_path=$4

if [ -e $cl_table_path ]
then
	echo "the cross-lingual link table: \"$cl_table_path\" does't not exist"
	exit
fi

for file in `ls $topic_path`
do
	source_id=`echo $file | cut -f 1 -d .`
	line=`grep ^$source_id: $cl_table_path`
	
	if [ "$?" -eq "0" ]
	then
		input=`echo $line | cut -f 2 -d :`
		
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
		echo "cp $corpus_path/$filename $cp_topic_path"
	fi
done