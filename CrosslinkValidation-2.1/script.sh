# some useful scripts here for easier debugging

grep -e "<anchor offset=\"" QUT_LTW_A2B_QO_02.xml | sed 's/<anchor offset=\"//g' | sed 's/\" length=\"/ /g' | sed 's/\" name=\"/ /g' | sed 's/">$//g' | sed 's/^[ \t]*//g' | sort -n
