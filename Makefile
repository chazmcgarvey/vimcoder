
# Use this file with GNU make to compile and package VimCoder.
# Supported targets: all clean distclean dist fetch jar

project		= VimCoder
version		= 0.3.5

sources		= $(wildcard src/com/dogcows/*.java)
classes		= $(sources:src/%.java=%.class)
library		= lib/ContestApplet.jar
jarfile		= $(project)-$(version).jar
resource_path	= com/dogcows/resources
resources	= $(wildcard src/$(resource_path)/*)

JAVAC		= javac
JAVACFLAGS	= -d . -sourcepath src -classpath $(library)


all: $(classes) $(resources:src/%=%)

clean:
	rm -rf com

distclean: clean
	rm -rf lib

dist:
	git archive HEAD --prefix=vimcoder-$(version)/ | bzip2 >vimcoder-$(version).tar.bz2

fetch: $(library)

jar: $(jarfile)


$(classes): $(sources) | $(library)
	$(JAVAC) $(JAVACFLAGS) $^

$(resource_path):
	mkdir -p "$@"

$(resource_path)/%: src/$(resource_path)/% | $(resource_path)
	cp "$<" "$@"


$(library):
	mkdir -p lib
	curl -o $@ http://www.topcoder.com/contest/classes/ContestApplet.jar

$(jarfile): all
	rm -f $@
	jar cvf $@ COPYING README.md com


.PHONY: all clean distclean dist fetch jar

# vim:noet:ts=8
