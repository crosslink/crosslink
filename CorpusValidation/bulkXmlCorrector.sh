#!/bin/bash

usage () {
   echo usage: $0 corpus_path target_path
   exit -1
}

cwd=`pwd`
base=`dirname $0`
cd $base
base=`pwd`
corpus_home=$1
target_path=$2
 
LOG_FILE=${cwd}/errorfiles.log

\rm -rf $LOG_FILE

if [ -z "$corpus_home" ]
then
  echo "missing corpus path"
  usage
fi

if [ -z "$target_path" ]
then
  echo "missing target path"
  usage
fi

corpus_path_len=`echo ${#corpus_home}`

cd $corpus_home
find . -name "*.xml" | while read line
do
  sub_path=`dirname $line`
  name=`basename $line`
  dest_path=${target_path}/${sub_path}
  mkdir -p ${dest_path}
  ${base}/runValidator.sh  $corpus_home/$line
  if [ "$?" -eq "0" ]
  then
    cp -uv $line ${dest_path}
  else
    # https://github.com/crosslink/xml_corrector
    dest_file=${dest_path}/${name}
    xml_corrector $line >${dest_file}
    ls ${dest_file} >>$LOG_FILE
  fi  
done

cd $cwd
