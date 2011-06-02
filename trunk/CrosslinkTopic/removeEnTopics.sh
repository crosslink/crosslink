#!/bin/bash
#
# version: 1.0
#

HOME_PATH=`dirname $0`

langs="en"
CJK_CORPORA_PATH=$1

if ! [ -n "$CJK_CORPORA_PATH" ]; then
	echo "Usage: $0 [CJK document collections path]"
	exit -1
fi

if ! [ -e "$CJK_CORPORA_PATH" ]; then
	echo "Error: $CJK_CORPORA_PATH does not exist"
	exit -2
fi

for i in $langs
do
	if [ -e "$CJK_CORPORA_PATH/$i" ]; then
		echo "removing $i topics from corpus..."
		
		REMOVED_TOPICS_PATH=$CJK_CORPORA_PATH/topics/${i}
		mkdir -v -p $REMOVED_TOPICS_PATH
	
		TOPICS=$HOME_PATH/${i}_topics_list.txt
		
		if [ -e "$TOPICS" ]; then
			for topic in `cat $TOPICS`
			do
				topic_file=${CJK_CORPORA_PATH}/${i}/${topic}
				echo $topic
				target_file=${REMOVED_TOPICS_PATH}/${topic}
				target_path=`dirname $target_file`
				mkdir -p $target_path
				mv -v $topic_file $target_path
			done
		else
			echo "Error: no $TOPICS found"
		fi
	fi
done
