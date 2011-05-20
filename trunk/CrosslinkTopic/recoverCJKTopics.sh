#!/bin/bash
#
# version: 1.0
#

HOME_PATH=`dirname $0`

langs="zh ja ko"

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
	TOPICS_PATH=${CJK_CORPORA_PATH}/topics/${i}
	if [ -e "$TOPICS_PATH" ]; then
		cd $TOPICS_PATH
		for file in `find . -name "*.xml"`
		do
			topic_file=${TOPICS_PATH}/$file
			target_path=${CJK_CORPORA_PATH}/${i}/`dirname $file`
			mv -v $topic_file $target_path
		done
	else
		echo "Can't found $TOPICS_PATH"
	fi
done