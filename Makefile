
# Use this file with ``make'' to compile and package VimCoder.
# Supported targets: all clean distclean dist fetch jar

project		= VimCoder
version		= 0.3.1

mainclass	= src/com/dogcows/VimCoder.java
library		= lib/ContestApplet.jar
jarfile		= $(project)-$(version).jar

JAVAC		= javac
JAVACFLAGS	= -d . -sourcepath src -classpath $(library)


all: $(classobj)

clean:
	rm -rf META-INF com

distclean: clean
	rm -rf lib

dist:
	git archive HEAD --prefix=vimcoder-$(version)/ | bzip2 >vimcoder-$(version).tar.bz2

fetch: $(library)

jar: $(jarfile)


classobj	= $(mainclass:src/%.java=%.class)


$(library):
	@echo "Fetching dependencies..."
	mkdir -p lib
	curl -o $@ http://www.topcoder.com/contest/classes/ContestApplet.jar

$(jarfile): $(classobj) META-INF/MANIFEST.MF
	@echo "Packaging jar file..."
	mkdir -p com/dogcows/resources
	cp src/com/dogcows/resources/* com/dogcows/resources
	rm -f $@
	zip $@ META-INF/MANIFEST.MF COPYING README $$(find com -type f | sort)
	@echo "Done."

$(classobj): $(mainclass)
	$(JAVAC) $(JAVACFLAGS) $<

META-INF/MANIFEST.MF:
	mkdir -p META-INF
	printf "Manifest-Version: 1.0\n\n" >$@


$(mainclass): src/com/dogcows/Util.java src/com/dogcows/Editor.java
$(classobj): $(library)

.PHONY: all clean distclean dist fetch jar

