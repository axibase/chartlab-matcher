MATCHER_DIR=src/matcher

all: bin matcher
	
bin:
	mkdir -p bin

matcher: bin
	mvn package -f $(MATCHER_DIR)/pom.xml
	cp $(MATCHER_DIR)/target/matcher.jar bin/

clean:
	mvn clean -f $(MATCHER_DIR)/pom.xml
	rm -r bin

install:   
	rm -rf /usr/local/chartlab-matcher
	cp -rf bin /usr/local/chartlab-matcher
	chmod -R 777 /usr/local/chartlab-matcher
	cp -f src/chartlab-matcher /usr/local/bin/chartlab-matcher

