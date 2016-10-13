package main

import (
	"flag"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
)

var outputFile = flag.String("log", "", "output log file")
var addr = flag.String("addr", ":8080", "address where server running")

func main() {
	if AXIBASE_URL == "" {
		log.Fatalln("AXIBASE_URL is not specified")
	} else {
		log.Println(AXIBASE_URL)
	}

	flag.Parse()

	if len(*outputFile) > 0 {
		file, err := os.Create(*outputFile)
		if err != nil {
			fmt.Println("Unable to to open log file, cause", err)
			os.Exit(-1)
			return
		}
		defer file.Close()
		log.SetOutput(io.MultiWriter(os.Stdout, file))
	}

	http.Handle("/", &PortalHandler{})

	apiURL, _ := url.Parse(AXIBASE_APPS_URL)
	proxy := httputil.NewSingleHostReverseProxy(apiURL)
	lndProxy := LogNoResponseData(proxy)
	http.Handle("/api/", lndProxy)
	http.Handle("/hbs/", lndProxy)
	http.Handle("/hbss/", lndProxy)
	http.Handle("/chartlab/", proxy)

	http.Handle(PROPERTY_PROXY_PATH, LogNoResponseData(NewPropertyProxy()))
	http.Handle(ALERTS_PROXY_PATH, LogNoResponseData(AlertsHandler()))

	log.Println("Starting server on", *addr)
	err := http.ListenAndServe(*addr, nil)
	if err != nil {
		log.Fatalln(err)
	}
}
