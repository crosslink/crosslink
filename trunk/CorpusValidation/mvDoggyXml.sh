#!/bin/bash

input=$1

firstline=`head -n 1 $input`
echo $firstline | cut -f 1 -d : | awk '{ print $1}'
path_and_file=`echo $firstline | cut -f 1 -d : | awk 'BEGIN { FS = "pages" } ; { print $1":"$2 }'`

corpus_path=`echo $path_and_file | cut -f 1 -d :`

mkdir -p $corpus_path/doggy

lang=`basename $corpus_path`
#file=`echo $path_and_file | cut -f 2 -d :`

for i in `cat $input | cut -f 1 -d : | awk 'BEGIN { FS = "pages" } ; { print $1":"$2 }'`
do
  
  file=${corpus_path}/pages/`echo ${i} | cut -f 2 -d :`
  ls $file

done
