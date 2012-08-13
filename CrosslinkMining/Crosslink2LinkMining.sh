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

# Chinese to English

for lang in zh ja ko
do
	source=$lang
	target=en
	$exec  $source:$target \
	 /users/tables/all \
	~/experiments/ntcir-10-crosslink/topics/all-local/${source} \
	~/experiments/ntcir-10-crosslink/topics/all-local/${target} \
	/data/corpus/wikipedia/all/

	source=en
	target=$lang
	
	$exec  $source:$target \
	 /users/tables/all \
	~/experiments/ntcir-10-crosslink/topics/all-local/${source} \
	~/experiments/ntcir-10-crosslink/topics/all-local/${target} \
	/data/corpus/wikipedia/all/
# English to Chinese

# English to Japanese

# English to Korean