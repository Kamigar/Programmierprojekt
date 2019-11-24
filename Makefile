
mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_dir := $(dir $(mkfile_path))

all: bin/routeplanner.jar

bin/routeplanner.jar: $(shell find $(current_dir)src -name "*.java")
	$(current_dir)bin/build.sh

clean:
	$(current_dir)bin/clean.sh

install: all
	$(current_dir)bin/install.sh

uninstall:
	$(current_dir)bin/uninstall.sh

