package main

import (
	"bytes"
	"compress/gzip"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"net/http/httptest"
	"os"
	"path"
	"path/filepath"
	"strings"
)

var gatheringMode = os.Getenv("GATHERING")

func init() {
	switch gatheringMode {
	case "create", "update", "append":
		log.Println("Entering gathering mode:", gatheringMode)
	}
}

type localCache struct {
	dir            string
	fileNameFormat string
}

type cacheHandler struct {
	parent http.Handler
	cache  *localCache
}

const defaultCacheFileNameFormat = "%s_%s_%s.cache"

func UseCache(dir string, parent http.Handler) http.Handler {
	return &cacheHandler{
		parent: parent,
		cache: &localCache{
			dir:            dir,
			fileNameFormat: defaultCacheFileNameFormat,
		},
	}
}

func (h *cacheHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	fileName := fmt.Sprintf(h.cache.fileNameFormat,
		r.FormValue("proxyId"),
		r.FormValue("proxyRev"),
		r.FormValue("proxyWgt"))

	dirs := strings.Split(r.URL.Path[1:], "/")
	dir := path.Join(dirs...)
	dir = path.Join(h.cache.dir, dir)
	err := os.MkdirAll(dir, 0755)
	if err != nil {
		log.Println("[ERROR] Unable to create cache dir:", err)
		h.parent.ServeHTTP(w, r)
		return
	}

	filePath := path.Join(dir, fileName)
	filePath = h.rewriteFilePathOnForecastRequest(filePath, r)

	switch gatheringMode {
	// Gather new cache for EACH request
	case "create":
		h.writeCacheAndServe(w, r, filePath)

	// Gather new cache for CACHED requests
	case "update":
		file, err := os.Open(filePath)
		if err != nil {
			if !os.IsNotExist(err) {
				log.Println("[ERROR] Unable to read cache file:", err)
			}
			h.parent.ServeHTTP(w, r)
		} else {
			file.Close()
			h.writeCacheAndServe(w, r, filePath)
		}

	// Gather cache for NOT CACHED requests
	case "append":
		file, err := os.Open(filePath)
		if err != nil {
			if os.IsNotExist(err) {
				h.writeCacheAndServe(w, r, filePath)
			} else {
				log.Println("[ERROR] Unable to read cache file:", err)
				h.parent.ServeHTTP(w, r)
			}
		} else {
			defer file.Close()
			h.serveCache(w, r, file)
		}

	// Gather cache for NO request
	default:
		file, err := os.Open(filePath)
		if err != nil {
			if !os.IsNotExist(err) {
				log.Println("[ERROR] Unable to read cache file:", err)
			}
			h.parent.ServeHTTP(w, r)
		} else {
			defer file.Close()
			h.serveCache(w, r, file)
		}
	}
}

func (h *cacheHandler) writeCacheAndServe(w http.ResponseWriter, r *http.Request, filePath string) {
	file, err := os.Create(filePath)
	if err != nil {
		log.Println("[ERROR] Unable to create cache file:", err)
		h.parent.ServeHTTP(w, r)
		return
	}
	defer file.Close()
	recorder := httptest.NewRecorder()
	h.parent.ServeHTTP(recorder, r)
	reader := io.TeeReader(recorder.Body, w)
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Content-Encoding", "gzip")
	if strings.Contains(recorder.Header().Get("Content-Encoding"), "gzip") {
		gz, err := gzip.NewReader(reader)
		if err != nil {
			log.Println("[WARN] Malformed GZIP for", filePath)
		} else {
			reader = gz
		}
	}
	io.Copy(file, reader)
}

func (h *cacheHandler) serveCache(w http.ResponseWriter, r *http.Request, file *os.File) {
	w.Header().Set("Content-Type", "application/json")
	io.Copy(w, file)
}

func (h *cacheHandler) rewriteFilePathOnForecastRequest(filePath string, r *http.Request) string {
	var buffer bytes.Buffer
	io.Copy(&buffer, r.Body)
	r.Body = ioutil.NopCloser(&buffer)

	// TODO it may be broken in future

	if strings.Contains(buffer.String(), "FORECAST") {
		fileDir, fileName := filepath.Split(filePath)
		fileName = "forecast_" + fileName
		filePath = path.Join(fileDir, fileName)
	}
	return filePath
}
