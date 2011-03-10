#!/bin/bash

# the link table created from link_extract software is in this format
# <source_document>:<target_document>:<source_document_title>|<target_document_title>
# with this table we can create two crosslink tables for both languages and for easy processing

extension_file=extensions.txt
rm -f $extension_file 2>&1 1>/dev/null

grep "|" $1 | while read line
do
	source=`echo $line | cut -f 1 -d :`
	dest=`echo $line | cut -f 2 -d :`
	titles=`echo $line | cut -f 3 -d :`
	
	source_title=`echo $titles | cut -f 1 -d\|`
	dest_title=`echo $titles | cut -f 2 -d\|`
	
	if [ "${source}" -gt "0" ] && [ "${dest}" -gt "0" ]; then
		echo "${source}:${dest}:${source_title}"
		echo "${dest}:${source}:${dest_title}" 1>&2
	else
		if [ "${dest}" -eq "0" ]
		then
			echo $titles >> $extension_file
		fi
	fi
	
#	exit
done