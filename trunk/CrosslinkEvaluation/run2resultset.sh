#!/bin/bash

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

append_jars_to_cp lib

java -cp ./bin:$cp genResultSet $1