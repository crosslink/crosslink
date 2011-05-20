############################################################################
#
#  file: README.TXT
#  ----------------
#  This file contains information of the CJK counterparts of English
#  test topics 
#
############################################################################

[1]. Use removeCJKTopics.sh to remove CJK counterparts of English from test document collections

* On Unix-like system
Usage: removeCJKTopics.sh [CJK document collections path]

or

* On Windows
Usage: removeCJKTopics.bat [CJK document collections path]

[2]. If the scripts provided are not suitable for your needs, you may find the corresponding CJK counterparts in following files:

# Corresponding Chinese Topics
zh_topics_list.txt

# Corresponding Japanese Topics
ja_topics_list.txt

# Corresponding Korean Topics
ko_topics_list.txt

[3]. Move the CJK counterparts of English topics back to the corpus

* On Unix-like system
Usage: recoverCJKTopics.sh [CJK document collections path]

or

* On Windows
Usage: recoverCJKTopics.bat [CJK document collections path]
