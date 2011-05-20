#!/bin/bash
#
# version: 1.0
#

HOME_PATH=`dirname $0`

langs="zh"
CJK_CORPORA_PATH=$1

if ! [ -n "$CJK_CORPORA_PATH" ]; then
	echo "Usage: $0 [CJK document collections path]"
	exit -1
fi

if ! [ -e "$CJK_CORPORA_PATH" ]; then
	echo "Error: $CJK_CORPORA_PATH does not exist"
	exit -2
fi

REMOVED_TOPICS_PATH=$CJK_CORPORA_PATH/topics

mkdir 

for i in $langs
do
	echo "removing $i topics from corpus..."
	TOPICS=$HOME_PATH/${i}_topics_list.txt
	
	if [ -e "$TOPICS" ]; then
		for topic in `cat $TOPICS`
		do
			echo $topic
			
		done
	else
		echo "Error: no $TOPICS found"
	fi
done
