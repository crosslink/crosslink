#!/bin/bash


SOURCE_LANG=zh
TARGET_LANG=en

OTHER_LANG=
OUTPUT=

if [ "$TARGET_LANG" -eq "en" ]
then
	OTHER_LANG=$SOURCE_LANG
else
	OTHER_LANG=$TARGET_LANG
fi

SOURCE_TOPICS_PATH=~/experiments/ntcir-9-clld/topics/training/${SOURCE_LANG}/
TARGET_TOPICS_PATH=~/experiments/ntcir-9-clld/topics/training/${TARGET_LANG}/


SOURCE_CROSSLINK_MERGED_TABLE=~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all/${SOURCE_LANG}2${TARGET_LANG}_merged.txt
TARGET_CROSSLINK_MERGED_TABLE=~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all/${TARGET_LANG}2${SOURCE_LANG}_merged.txt

./find_crosslingual_topics.sh $TARGET_TOPICS_PATH $SOURCE_TOPICS_PATH ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/${TARGET_LANG}-${SOURCE_LANG/${TARGET_LANG}2${SOURCE_LANG}_table.txt /data/corpus/wikipedia/all/

TMP_LINKS=/tmp/$0.tmp

get_output_id_from_en() {
	
}

get_output_id_from_other_lang() {
	
}

get_topic_links() {
	topic_path=$1
	crosslink_table=$2
	for i in `ls $topic_path`
	do
#		TOPIC_ID=``
		
		~/workspace/ant/build/release/link_extract $topic_path/$i > $TMP_LINKS
		
		cat $TMP_LINKS | while read line
		do
		 	target_id=`echo $line | cut -f 2 -d :`
		 	num=`grep "^$target_id:" $crosslink_table | wc -l`
		 	if [ "$num" -gt "0" ]
			then
				if [ "$num" -eq "1" ]
				then
					
				else
				fi
#				grep "^$target_id:" $crosslink_table | while read line
#				do					
#				done
				echo $line 
			fi
		done
	done

}

get_topic_link $TARGET_TOPICS_PATH $TARGET_CROSSLINK_MERGED_TABLE
get_topic_link $SOURCE_TOPICS_PATH $SOURCE_CROSSLINK_MERGED_TABLE

#get_zh_topics_links() {
#	for i in `ls $TARGET_TOPICS_PATH`
#	do
#	TOPIC_ID=``
#	
#	~/workspace/ant/build/release/link_extract $SOURCE_TOPICS_PATH/$i > $TMP_LINKS
#	
#	cat $TMP_LINKS | while read line
#	
#	
#	done
#
#}

\rm -f $TMP_LINKS
