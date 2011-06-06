#!/bin/bash

base=`dirname $0`
java -cp $base/lib/xml2txt.jar crosslink.XML2TXT "$@" 
