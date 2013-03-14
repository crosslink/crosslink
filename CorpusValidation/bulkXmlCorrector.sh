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
LOG_FILE_BAD=${cwd}/undonefiles.log

\rm -rf $LOG_FILE
\rm -rf $LOG_FILE_BAD

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

tmp_good=/tmp/temp_good.xml
cd $corpus_home
find . -name "*.xml" | while read line
do
  sub_path=`dirname $line`
  name=`basename $line`
  dest_path=${target_path}/${sub_path}
  mkdir -p ${dest_path}
  source_file=$corpus_home/$line
  ${base}/runValidator.sh  $source_file
  if [ "$?" -eq "0" ]
  then
    cp -uv $line ${dest_path}
  else
    ls ${source_file} >>$LOG_FILE
    # https://github.com/crosslink/xml_corrector
    dest_file=${dest_path}/${name}
    xml_corrector $source_file >${tmp_good}
    ${base}/runValidator.sh ${tmp_good}
    if [ "$?" -eq "0" ]
    then
      mv -f ${tmp_good} ${dest_file}
    else
      echo ${source_file} >$LOG_FILE_BAD  
    fi
  fi  
done

cd $cwd
