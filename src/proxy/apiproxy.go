package main

import (
	"compress/gzip"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"strings"
)

type ApiProxy struct {
	mapLock    chan bool
	badPortals map[string]bool
}

func NewApiProxy() *ApiProxy {
	return &ApiProxy{make(chan bool, 1), make(map[string]bool, 16)}
}

var axiApiURL, _ = url.Parse(AXIBASE_URL)

func (proxy *ApiProxy) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	proxyId := r.FormValue("proxyId")
	proxyRev := r.FormValue("proxyRev")

	if len(proxyId) > 0 {
		if len(proxyRev) == 0 {
			proxyRev = "1"
		}
	}

	urlStr := strings.Replace(r.URL.String(), PROXY_PATH, AXIBASE_URL, 1)
	req, err := http.NewRequest(r.Method, urlStr, r.Body)
	req.Header = r.Header
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		log.Println("[ERROR] proxy error:", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	for h, v := range map[string][]string(resp.Header) {
		for _, e := range v {
			w.Header().Add(h, e)
		}
	}

	tee := io.TeeReader(resp.Body, w)
	defer resp.Body.Close()
	if enc := resp.Header.Get("Content-Encoding"); strings.Contains(enc, "gzip") {
		tee, _ = gzip.NewReader(tee)
	}

	var respData []map[string]json.RawMessage
	err = json.NewDecoder(tee).Decode(&respData)

	proxy.mapLock <- true
	defer func() { <-proxy.mapLock }()

	portalStr := fmt.Sprintf("%s/%s", proxyId, proxyRev)
	if good, ok := proxy.badPortals[portalStr]; ok && good {
		return
	}

	if len(respData) == 0 {
		//onBadData()
		proxy.badPortals[portalStr] = false
		return
	}

	for _, e := range respData {
		data, hasData := e["data"]
		_, hasTags := e["tags"]

		if !hasData && !hasTags {
			continue
		}

		if hasData {
			dataSlice := []interface{}{}
			err = json.Unmarshal(data, &dataSlice)
			if err != nil || (len(dataSlice) == 0) {
				continue
			}
		}

		proxy.badPortals[portalStr] = true
		return
	}
	proxy.badPortals[portalStr] = false
}

func (proxy *ApiProxy) GetLogHandler() http.Handler {
	return &proxyLogHandler{proxy}
}

type EntityWData struct {
	Data []interface{} `json:"data"`
	Tags interface{}   `json:"tags"`
}

type proxyLogHandler struct {
	proxy *ApiProxy
}

func (l *proxyLogHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	for portal, status := range l.proxy.badPortals {
		fmt.Fprintf(w, "%s\t%v\n", portal, status)
	}
}
