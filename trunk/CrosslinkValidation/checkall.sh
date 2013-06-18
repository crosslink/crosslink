#!/bin/bash

usage {
  echo "usage: $0 /path/of/runs /corpus/path /topic/path"
  exit
}

topic_path=
corpus_path=

if [ -z "$1" ]
then
  usage
fi

if [ -z "$2" ]
then
  usage
  echo "missing corpus path"
else
  
fi

if [ -z "$2" ]
then
  usage
  echo "missing topic path"
fi


for i in `ls $1/*.xml`
do
     echo $i
     bname=`basename $i`
     jump=0
     if [ -e $bname.log ]
     then
     	grep Exception $bname.log
     	if [ $? -eq "1" ]
     	then
     		jump=1
     	else
     		\rm $bname.log
     		exit
     	fi
     fi
     
     if [ $jump -eq "0" ]
     then
	     ./checkrun.sh -c $corpus_path -t $topic_path $i 1>/dev/null 2>$bname.log
	     
	     #num=`wc -l $bname.log | cut -f 1 -d " "`
	     #if [ $num -gt "2" ]
	     #then
      	grep Exception $bname.log
     	if [ $? -eq "0" ]
     	then
	     	exit 
	     fi
     fi
done
       bname=`basename $i`  ./checkrun.sh $i 1>/dev/null 2>$bname.log  num=`wc -l $bname.log | cut -f 1 -d " "`  if [ $num -gt "2"   then  exit  fi  done

