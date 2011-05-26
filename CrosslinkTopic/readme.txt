############################################################################
#
#  file: README.TXT
#  ----------------
#  This file contains information of the CJK counterparts of English
#  test topics 
#
#  CJK document collections path: the home of the CJK Wikipedia 
#  collections will be the directory holding the zh , or ja , or ko folder, 
#  or all above folders
#
############################################################################

[1]. Use removeCJKTopics.sh to remove CJK counterparts of English from test document collections

	* On Unix-like system
	Usage: removeCJKTopics.sh [CJK document collections path]
	
	or
	
	* On Windows
	Usage: removeCJKTopics.bat [CJK document collections path]
	
	if running the script successfully, the CJK counterparts of English topics will be moved to [CJK document collections path]//topics directory.


[2]. If the scripts provided are not suitable for your needs, you may find the corresponding CJK counterparts in following files:

	* On Unix-like system
	------------------------------
	# Corresponding Chinese Topics
	zh_topics_list.txt
	
	# Corresponding Japanese Topics
	ja_topics_list.txt
	
	# Corresponding Korean Topics
	ko_topics_list.txt
	
	* On Windows
	------------------------------
	# Corresponding Chinese Topics
	zh_topics_list_win.txt
	
	# Corresponding Japanese Topics
	ja_topics_list_win.txt
	
	# Corresponding Korean Topics
	ko_topics_list_win.txt

[3]. Move the CJK counterparts of English topics back to the corpus

	* On Unix-like system
	Usage: recoverCJKTopics.sh [CJK document collections path]
	
	or
	
	* On Windows
	Usage: recoverCJKTopics.bat [CJK document collections path]
