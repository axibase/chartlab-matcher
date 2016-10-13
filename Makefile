MATCHER_DIR=src/matcher
PROXY_RES_DIR=src/proxy
GOPATH=$(PWD)

all: bin matcher proxy proxy_res
	
bin:
	mkdir -p bin

matcher: bin
	mvn package -f $(MATCHER_DIR)/pom.xml
	cp $(MATCHER_DIR)/target/matcher.jar bin/

proxy: bin
	go get -v proxy && go install proxy

proxy_res: proxy
	cp -r $(PROXY_RES_DIR)/data $(PROXY_RES_DIR)/templates bin/

clean:
	mvn clean -f $(MATCHER_DIR)/pom.xml
	go clean proxy
	rm -r pkg
	rm -r bin
	rm -rf src/github.com

install:   
	rm -rf /usr/local/chartlab-matcher
	cp -rf bin /usr/local/chartlab-matcher
	chmod -R 777 /usr/local/chartlab-matcher
	ln -sf /usr/local/chartlab-matcher/proxy /usr/local/bin/configurator
	chmod 775 /usr/local/bin/configurator
	cp -f src/chartlab-matcher /usr/local/bin/chartlab-matcher

