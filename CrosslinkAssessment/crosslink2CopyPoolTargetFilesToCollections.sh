#!/bin/bash

HOME_PATH=`dirname $0`

#langs="ja"
lang=$1
POOL_PATH=resources/Pool/crosslink2


if ! [ -n "$POOL_PATH" ]; then
	echo "Usage: $0 [lang] [pool path]"
	exit -1
fi

if ! [ -e "$POOL_PATH" ]; then
	echo "Error: $POOL_PATH does not exist"
	exit -2
fi

COLLECTION_PATH=resources/Collections/$lang
ls $POOL_PATH/*${lang} >/dev/null

if [ "$?" -eq "0" ]; then
	echo "copy target files from corpus to collections..."
	
	mkdir -v -p $COLLECTION_PATH

	cat $POOL_PATH/*${lang}/*.xml | grep "<tobep" | cut -f 2 -d ">" | cut -f 1 -d "<" | while read line
		#for topic in `cat $TOPICS`
	do
		extname=`/data/corpus/wikipedia/all/id2path.sh ${line}`
		filename=/data/corpus/wikipedia/all/${lang}/${extname}
		#echo $filename
		target_file=${COLLECTION_PATH}/${extname}
		target_path=`dirname $target_file`
		mkdir -p $target_path
		#mv -v $filename $target_path
		cp -v $filename ${target_path}
	done
fi

