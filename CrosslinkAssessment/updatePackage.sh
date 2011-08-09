#!/bin/bash

for lang in zh ko ja
do
  for i in `ls CrosslinkAssessment-*-${lang}.zip`
  do
	filename=`echo $i | cut -f 1 -d "."`
	zip -u $filename lib/*
  done
done
