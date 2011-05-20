:: REMOVECJKTOPICS.BAT
:: remove the CJK counterparts of English topics from CJK document collections.

@ECHO OFF

set HOME_PATH=`dirname $0`

set langs="zh"
set CJK_CORPORA_PATH=%1

if X%CJK_CORPORA_PATH% == X (
	:USAGE
	echo Usage: %0 [CJK document collections path]
	goto EXIT
)

if NOT EXIST %CJK_CORPORA_PATH% (
	echo "Error: %CJK_CORPORA_PATH% does not exist"
	goto EXIT
)

FOR %%A IN (zh ja ko) DO (
	echo %%A 
	if EXIST %CJK_CORPORA_PATH%\%%A" (
		echo "removing %%A topics from corpus..."
		
		REMOVED_TOPICS_PATH=%CJK_CORPORA_PATH%/topics/${i}
		mkdir -v -p $REMOVED_TOPICS_PATH
	
		set TOPICS=$HOME_PATH/${i}_topics_list.txt
		
		if EXIST %TOPICS% (
			for topic in `cat %TOPICS%S`
			do
				topic_file=${CJK_CORPORA_PATH}/${i}/${topic}
				echo $topic
				target_file=${REMOVED_TOPICS_PATH}/${topic}
				target_path=`dirname $target_file`
				mkdir -p $target_path
				mv -v $topic_file $target_path
			done
		)
		else (
			echo "Error: no %TOPICS% found"
		)
	)	
)

:EXIT
