#!/bin/bash

ZH_TOPICS_PATH=~/experiments/ntcir-9-clld/topics/training/zh/
EN_TOPICS_PATH=~/experiments/ntcir-9-clld/topics/training/en/


./find_crosslingual_topics.sh $EN_TOPICS_PATH $ZH_TOPICS_PATH ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/en-zh/e2c_table.txt /data/corpus/wikipedia/CJK/xml-v2/

TMP_LINKS=/tmp/$0.tmp

get_en_topics_links() {
	for i in `ls $ZH_TOPICS_PATH`
	do
	TOPIC_ID=``
	
	~/workspace/ant/build/release/link_extract $ZH_TOPICS_PATH/$i > $TMP_LINKS
	
	cat $TMP_LINKS | while read line
	
	done

}

get_zh_topics_links() {
	for i in `ls $EN_TOPICS_PATH`
	do
	TOPIC_ID=``
	
	~/workspace/ant/build/release/link_extract $ZH_TOPICS_PATH/$i > $TMP_LINKS
	
	cat $TMP_LINKS | while read line
	
	
	done

}