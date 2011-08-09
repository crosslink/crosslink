#!/bin/bash

for lang in zh ko ja
do
  for i in `ls CrosslinkAssessment-*-${lang}.zip`
  do
	filename=`echo $i | cut -f 1 -d "."`
	zip -u $filename lib/*
	zip -u $filename resources/Pool/zh/*
	zip -u $filename resources/Pool/ja/*
	zip -u $filename resources/Pool/ko/*
  done
done
