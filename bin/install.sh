#!/bin/sh

cd $(dirname $0)

PREFIX=$(cat ../.configure/prefix.var)
if [ $? -ne 0 ]; then
  echo "Project not properly configured"
  exit -1
fi

install -v -m 755 -d $PREFIX/lib/routeplanner $PREFIX/bin

install -v -m 644 routeplanner.jar $PREFIX/lib/routeplanner
install -v -m 755 ../.configure/routeplanner $PREFIX/bin

