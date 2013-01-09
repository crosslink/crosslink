

all-cjk2e: packageZh2En packageJa2En packageKo2En
all-e2cjk: packageEn2Zh packageEn2Ja packageEn2Ko
	
	
packageEn2Zh:
	printf "en:zh\ninbox\n" > lang.txt
	ant release-en2zh
	
packageEn2Ja:
	printf "en:ja\ninbox\n" > lang.txt
	ant release-en2ja
	
packageEn2Ko:
	printf "en:ko\ninbox\n" > lang.txt
	ant release-en2ko
	
packageZh2En:
	printf "zh:en\ninbox\n" > lang.txt
	ant release-zh2en
	
packageJa2En:
	printf "ja:en\ninbox\n" > lang.txt
	ant release-ja2en
	
packageKo2En:
	printf "ko:en\ninbox\n" > lang.txt
	ant release-ko2en
	
en2zh-gt:
	echo "en:zh" > lang.txt
	ant release-en2zh-gt
	
en2ja-gt:
	echo "en:ja" > lang.txt
	ant release-en2ja-gt
	
en2ko-gt:
	echo "en:ko" > lang.txt
	ant release-en2ko-gt
	
zh2en-gt:
	echo "zh:en" > lang.txt
	ant release-zh2en-gt
	
ja2en-gt:
	echo "ja:en" > lang.txt
	ant release-ja2en-gt
	
ko2en-gt:
	echo "ko:en" > lang.txt
	ant release-ko2en-gt	