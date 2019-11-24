#!/bin/sh

cd $(dirname $0)

javac -classpath . -d . $(find ../src -name "*.java")

jar cmf manifest.mf routeplanner.jar routeplanner

