#!/bin/sh

cd $(dirname $0)

PREFIX=$(cat ../.configure/prefix.var)
if [ $? -ne 0 ]; then
  echo "Project not properly configured"
  exit -1
fi

rm -rfv $PREFIX/lib/routeplanner $PREFIX/bin/routeplanner

