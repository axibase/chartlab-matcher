package main

import (
	"net/http"
	"net/http/httputil"
	"net/url"

	"proxy/settings"
)

var RegisteredProxies = make(map[string]http.Handler, 8)

func CreateProxy(conf settings.ProxySettings) (http.Handler, error) {
	url, err := url.Parse(conf.ProxyTo)
	if err != nil {
		return nil, err
	}
	proxy := http.Handler(httputil.NewSingleHostReverseProxy(url))
	if len(conf.CacheDir) > 0 {
		proxy = UseCache(conf.CacheDir, proxy)
	}
	if conf.CheckData {
		proxy = LogNoResponseData(proxy)
	}
	RegisteredProxies[conf.Path] = proxy
	return proxy, nil
}
