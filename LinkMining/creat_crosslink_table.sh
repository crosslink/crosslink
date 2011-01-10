#!/bin/bash

# the link table created from link_extract software is in this format
# <source_document>:<target_document>:<source_document_title>|<target_document_title>
# with this table we can create two crosslink tables for both languages and for easy processing

cat $1 | while read line
do
	source=`echo $line -f 1 -d :
	dest=`echo $line -f 2 -d :
	titles=`echo $line -f 3 -d :
	
	source_title=`echo $titles -f 1 -d |`
	dest_title=`echo $titles -f 2 -d |`
	
	if [ "${source}" -gt "0" ]; then
		echo "${source}:${dest}:${source_title}"
	fi
		
	if [ "${source}" -gt "0" ]; then
		echo "${dest}:${source}:${dest_title}" 1>&2
	fi
	
done