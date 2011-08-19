:: RECOVERCJKTOPICS.BAT
:: remove the CJK counterparts of English topics from CJK document collections.

@ECHO OFF

setlocal EnableDelayedExpansion

setlocal

set HOME_PATH=%~dp0
echo HOME_PATH: %HOME_PATH%

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
	if EXIST %CJK_CORPORA_PATH%\%%A (
		echo "recovering %%A topics to corpus..."
		
		set REMOVED_TOPICS_PATH=%CJK_CORPORA_PATH%\topics\%%A
		if not EXIST %REMOVED_TOPICS_PATH% mkdir %REMOVED_TOPICS_PATH%
	
		set LANG=%%A
		echo %LANG%
		set /A TOPIC_FILE=%HOME_PATH%%%A_topics_list_win.txt
		
		IF EXIST "%HOME_PATH%%%A_topics_list_win.txt" (
			::echo "I found it"
			for /f "tokens=*" %%B in (%HOME_PATH%%%A_topics_list_win.txt) do (
				set line=%%B
				
				set target_file=%CJK_CORPORA_PATH%\%%A\!line!
				set topic_file=%CJK_CORPORA_PATH%\topics\%%A\!line!
				for %%F in (%CJK_CORPORA_PATH%\%%A\!line!) do set target_path=%%~dpF
				
				echo moving !topic_file! ...
				if EXIST %CJK_CORPORA_PATH%\topics\%%A\!line! (
					move %CJK_CORPORA_PATH%\topics\%%A\!line! !target_path!
				)
			)			
		)
	)	
)

goto EXIT

:PROCESS_TOPIC

:EXIT

endlocal