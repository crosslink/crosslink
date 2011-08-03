#!/bin/bash

for i in `ls /media/quthome/experiments/all/*.xml`
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
     		exit;
     	fi
     fi
     
     if [ $jump -eq "0" ]
     then
	     ./checkrun.sh $i 1>/dev/null 2>$bname.log
	     num=`wc -l $bname.log | cut -f 1 -d " "`
	     if [ $num -gt "2" ]
	     then
	     	exit 
	     fi
     fi
done
       bname=`basename $i`  ./checkrun.sh $i 1>/dev/null 2>$bname.log  num=`wc -l $bname.log | cut -f 1 -d " "`  if [ $num -gt "2"   then  exit  fi  done

