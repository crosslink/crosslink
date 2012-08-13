#!/bin/bash

base=`dirname $0`
cp=

append_jars_to_cp() {
    # N.b.  This adds all the jars of a directory to $cp.
    # That may be ok in some other situation, but you probably should
    # NOT run "append_jars_to_cp $HSQLDB_HOME/lib" (for one thing, that
    # directory may contain non-inter-compatible libraries).
    dir="$1"
    for ex in jar zip ; do
        if [ "`echo ${dir}/*.$ex`" != "${dir}/*.$ex" ] ; then
            for x in ${dir}/*.$ex ; do
                if [ ! -z "$cp" ] ; then cp="$cp:" ; fi
                cp="$cp$x"
            done
        fi
    done
}

append_jars_to_cp $base/lib

exec="java -cp $cp:$base/bin crosslink.CrosslinkMining" 
echo "Program: $exec"

# Chinese to English

for lang in zh ja ko
do
	source=$lang
	target=en
	
	if [ "$source" == "zh" ]
	then
	  S_CODE="C"
	fi
	if [ "$source" == "en" ]
	then
	  S_CODE="E"
	fi
	if [ "$source" == "ja" ]
	then
	  S_CODE="J"
	fi
	if [ "$source" == "ko" ]
	then
	  S_CODE="K"
	fi
	
	if [ "$target" == "zh" ]
	then
	  T_CODE="C"
	fi
	if [ "$target" == "en" ]
	then
	  T_CODE="E"
	fi
	if [ "$target" == "ja" ]
	then
	  T_CODE="J"
	fi
	if [ "$target" == "ko" ]
	then
	  T_CODE="K"
	fi

	$exec  $source:$target \
	 /users/tables/all \
	~/experiments/ntcir-10-crosslink/topics/with-links/${source} \
	~/experiments/ntcir-10-crosslink/topics/with-links/${target} \
	/data/corpus/wikipedia/all/ > A2FWikiGroundTruthResultSet-${S_CODE}2${T_CODE}.xml

	source=en
	target=$lang
	
	TMP_CODE=$S_CODE
	S_CODE=$T_CODE
	T_CODE=$TMP_CODE
	
	$exec  $source:$target \
	 /users/tables/all \
	~/experiments/ntcir-10-crosslink/topics/with-links/${source} \
	~/experiments/ntcir-10-crosslink/topics/with-links/${target} \
	/data/corpus/wikipedia/all/  > A2FWikiGroundTruthResultSet-${S_CODE}2${T_CODE}.xml
	
done
# English to Chinese

# English to Japanese

# English to Korean