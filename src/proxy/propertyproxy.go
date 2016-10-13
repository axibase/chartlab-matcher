package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"net/http/httptest"
	"net/http/httputil"
	"net/url"
	"os"
	"path"
	"sync"
)

type propertyProxy struct {
	defaultDataSource http.Handler
	cacheLock         sync.RWMutex
}

func NewPropertyProxy() http.Handler {
	apiURL, _ := url.Parse(AXIBASE_APPS_URL)
	proxy := httputil.NewSingleHostReverseProxy(apiURL)
	return &propertyProxy{
		defaultDataSource: proxy,
	}
}

func (proxy *propertyProxy) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	id, rev, wgt := r.FormValue("proxyId"), r.FormValue("proxyRev"), r.FormValue("proxyWgt")

	if len(id) == 0 {
		proxy.useDefaultDataSource(w, r, id, rev, wgt)
	}
	if len(rev) == 0 {
		rev = "1"
	}

	err := proxy.tryUseCache(w, r, id, rev, wgt)
	if err != nil {
		log.Println("[INFO]", err)
		proxy.useDefaultDataSource(w, r, id, rev, wgt)
	}
}

func (proxy *propertyProxy) useDefaultDataSource(w http.ResponseWriter, r *http.Request, id, rev, wgt string) {
	proxy.cacheLock.Lock()
	defer proxy.cacheLock.Unlock()
	if e := os.MkdirAll(PROPERTY_CACHE_DIR, 0777); e != nil && !os.IsExist(e) {
		panic(e)
	}
	fileName := fmt.Sprintf(PROPERTY_CACHE_FILE_NAME_FORMAT, id, rev, wgt)
	file, err := os.Create(path.Join(PROPERTY_CACHE_DIR, fileName))
	writer := w
	if err == nil {
		defer file.Close()
		writer = httptest.NewRecorder()
	} else {
		log.Println("[ERROR]", err, "used default file source, not cached")
	}

	proxy.defaultDataSource.ServeHTTP(writer, r)
	if err == nil {
		recorder := writer.(*httptest.ResponseRecorder)
		output := io.MultiWriter(w, file)
		w.Header().Set("Content-Encoding", "gzip")
		io.Copy(output, recorder.Body)
	}

}

func (proxy *propertyProxy) tryUseCache(w http.ResponseWriter, r *http.Request, id, rev, wgt string) error {
	proxy.cacheLock.RLock()
	defer proxy.cacheLock.RUnlock()
	fileName := fmt.Sprintf(PROPERTY_CACHE_FILE_NAME_FORMAT, id, rev, wgt)
	confFile, err := os.Open(path.Join(PROPERTY_CACHE_DIR, fileName))
	if err != nil {
		return fmt.Errorf("cache miss for %s/%s Wgt#%s", id, rev, wgt)
	}
	defer confFile.Close()

	w.Header().Set("Content-Encoding", "gzip")
	io.Copy(w, confFile)
	return nil
}
