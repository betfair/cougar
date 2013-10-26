#!/bin/bash

for i in `find . -name surefire-reports`; do
  PATH1=`echo "$i" | cut -c2-1000`
  PATH_SO_FAR=""
  SEP=""
  for dir in `echo "$PATH1" | sed -e 's/\// /g'`; do
    NEW_PATH_SO_FAR="$PATH_SO_FAR$SEP$dir"
    SEP="/"
    OUTPUT=$PATH_SO_FAR/index.html
    if [ -z "$PATH_SO_FAR" ]; then
      OUTPUT=index.html
    fi
    echo "<a href='$dir/index.html'>$dir</a><br/>" >> $OUTPUT
    travis-artifacts upload --target-path cougar --path $OUTPUT
    if [ $dir == "surefire-reports" ]; then
      for file in `ls $NEW_PATH_SO_FAR`; do
        echo "<a href='$file'>$file</a><br/>" >> $NEW_PATH_SO_FAR/index.html
      done
      travis-artifacts upload --target-path cougar --path $NEW_PATH_SO_FAR
    fi
    PATH_SO_FAR=$NEW_PATH_SO_FAR
  done
done
