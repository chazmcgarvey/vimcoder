#!/bin/sh

action=$1
shift

case "$action"
in

	fetch)
		echo "Fetching dependencies..."
		mkdir -p lib
		curl -o $1 http://www.topcoder.com/contest/classes/ContestApplet.jar
		;;

	jar)
		echo "Packaging jar file..."
		rm -rf build
		mkdir -p build/META-INF
		printf "Manifest-Version: 1.0\n\n" >build/META-INF/MANIFEST.MF
		cp -R bin/com COPYING README build/
		cd build
		files=$(find com -type f | sort)
		zip $1 META-INF/MANIFEST.MF README COPYING $files
		cd ..
		mv build/$1 .
		rm -rf build
		echo "Done."
		;;
esac

