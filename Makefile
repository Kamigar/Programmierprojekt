
all: bin/routeplanner.jar

bin/routeplanner.jar: $(shell find src -name "*.java")
	./bin/build.sh

clean:
	./bin/clean.sh

install: all
	./bin/install.sh

uninstall:
	./bin/uninstall.sh

