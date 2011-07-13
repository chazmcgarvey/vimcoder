
# Use this file with ``make'' to compile and package VimCoder.
# Supported targets: all clean distclean dist fetch jar

project		= VimCoder
version		= 0.3.1

sources		= src/com/dogcows/VimCoder.java src/com/dogcows/Util.java src/com/dogcows/Editor.java
library		= lib/ContestApplet.jar
jarfile		= $(project)-$(version).jar

JAVAC		= javac
JAVACFLAGS	= -d . -sourcepath src -classpath $(library)


classes		= $(sources:src/%.java=%.class)
all: $(firstword $(classes))

clean:
	rm -rf META-INF com

distclean: clean
	rm -rf lib

dist:
	git archive HEAD --prefix=vimcoder-$(version)/ | bzip2 >vimcoder-$(version).tar.bz2

fetch: $(library)

jar: $(jarfile)


$(library):
	@echo "Fetching dependencies..."
	mkdir -p lib
	curl -o $@ http://www.topcoder.com/contest/classes/ContestApplet.jar

$(jarfile): $(firstword $(classes)) META-INF/MANIFEST.MF
	@echo "Packaging jar file..."
	mkdir -p com/dogcows/resources
	cp src/com/dogcows/resources/* com/dogcows/resources
	rm -f $@
	zip $@ META-INF/MANIFEST.MF COPYING README $$(find com -type f | sort)
	@echo "Done."

$(classes): $(sources) $(library)
	$(JAVAC) $(JAVACFLAGS) $<

META-INF/MANIFEST.MF:
	mkdir -p META-INF
	printf "Manifest-Version: 1.0\n\n" >$@


.PHONY: all clean distclean dist fetch jar

