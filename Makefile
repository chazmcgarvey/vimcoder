
project		:= VimCoder
version		:= 0.3.1

mainclass	= bin/com/dogcows/VimCoder.class
library		= lib/ContestApplet.jar
jarfile		= $(project)-$(version).jar

JAVAC		:= javac
JAVACFLAGS	:= -d bin -sourcepath src -classpath bin:$(library)


.PHONY: all clean distclean dist fetch jar

all: $(library) $(mainclass)

clean:
	rm -rf bin build

distclean: clean
	rm -rf lib

dist:
	git archive HEAD --prefix=vimcoder-$(version)/ | bzip2 >vimcoder-$(version).tar.bz2

fetch: $(library)

jar: all $(jarfile)


$(library):
	sh make.sh fetch $@

bin/com/dogcows/%.class: src/com/dogcows/%.java
	mkdir -p bin/com/dogcows/resources &&\
$(JAVAC) $(JAVACFLAGS) $< &&\
cp -R src/com/dogcows/resources bin/com/dogcows/

$(jarfile): $(mainclass)
	sh make.sh jar $@


$(mainclass): src/com/dogcows/Util.java src/com/dogcows/Editor.java

