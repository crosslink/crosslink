#!/bin/bash

input=$1
corpus_path=$2

firstline=`head -n 1 $input`
#echo $firstline | cut -f 1 -d : | awk '{ print $1}'
path_and_file=`echo $firstline | cut -f 1 -d : | awk 'BEGIN { FS = "pages" } ; { print $1":"$2 }'`

input_corpus_path=`echo $path_and_file | cut -f 1 -d :`
lang=`basename $input_corpus_path`
if [ -z $corpus_path ]
then

corpus_path=`dirname $input_corpus_path`
fi

#exit

doggypath=$corpus_path/${lang}/doggy
#mkdir -p $doggypath

#file=`echo $path_and_file | cut -f 2 -d :`

for i in `cat $input | cut -f 1 -d : | awk 'BEGIN { FS = "pages" } ; { print $1":"$2 }'`
do
  file=`echo ${i} | cut -f 2 -d :`
  pathfile=${corpus_path}/${lang}/pages/$file
  destdir=`dirname $file`

  echo "mkdir -p $doggypath/$destdir"
  echo "\mv $pathfile $doggypath/$destdir/"
  
  #ls $file

done
