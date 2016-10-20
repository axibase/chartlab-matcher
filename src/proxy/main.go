package main

import (
	"flag"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"proxy/settings"
	"time"
)

const DefaultAddr = ":8080"

var logFilePtr = flag.String("log", "", "log file path (default stdout)")
var addr = flag.String("addr", "", "Override configuration addr")

func main() {
	flag.Parse()
	if logFilePtr != nil && len(*logFilePtr) > 0 {
		logFile, err := os.Create(*logFilePtr)
		if err != nil {
			log.Println("[ERROR] Unable to initialize log:", err)
			log.Println("[ERROR] Writing log to stdout")
		} else {
			defer logFile.Close()
			log.SetOutput(logFile)
		}
	}

	filename := "proxy.conf"
	serverConf, err := settings.GetSettingsFrom(filename)
	if err != nil {
		log.Fatalln("[ERROR] Unable to read configuration:", err)
	}
	if addr != nil && len(*addr) > 0 {
		serverConf.Addr = *addr
	}

	http.DefaultClient.Timeout = 2 * time.Minute

	configureProxies(serverConf)
	configurePortalPage(serverConf)
	runServer(serverConf)
}

func initLog() {
}

func configureProxies(serverConf *settings.ServerSettings) {
	for _, proxyConf := range serverConf.Proxies {
		proxy, err := CreateProxy(proxyConf)
		if err != nil {
			log.Fatalln("Unable to create proxy:", err)
		}
		http.Handle(proxyConf.Path, proxy)
	}
}

func runServer(serverConf *settings.ServerSettings) {
	if len(serverConf.Addr) == 0 {
		serverConf.Addr = DefaultAddr
	}
	log.Println("Starting server on", serverConf.Addr)
	err := http.ListenAndServe(serverConf.Addr, nil)
	if err != nil {
		log.Fatalln(err)
	}
}

func configurePortalPage(serverConf *settings.ServerSettings) {
	portalC := NewPortalConfigurator(serverConf.Configurator)
	portalH := NewPortalHandler(portalC)
	http.Handle("/", portalH)
}

func configureMainPageProxy() {
	handler, ok := RegisteredProxies["/"]
	if !ok {
		log.Println("No main page proxy")
		return
	}

	proxy, ok := handler.(*httputil.ReverseProxy)
	if !(ok) {
		log.Println("Main page handler is not a proxy")
		return
	}

	oldDirector := proxy.Director
	proxy.Director = func(r *http.Request) {
		if len(r.URL.Path) == 0 || r.URL.Path == "/" {
			q := url.Values{}
			q.Set("id", r.FormValue("id"))
			q.Set("v", r.FormValue("rev"))
			q.Set("theme", "default")
			q.Set("dataSource", "default")
			r.URL.RawQuery = q.Encode()
		}

		oldDirector(r)
	}
	RegisteredProxies["/"] = proxy
}
