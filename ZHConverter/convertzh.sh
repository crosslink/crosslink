#!/bin/bash

jarfile=dist/ZHConverter.jar
if ! [ -e  $jarfile]
then
	ant dist
fi

java -jar $jarfile 