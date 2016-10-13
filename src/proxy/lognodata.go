package main

import (
	"compress/gzip"
	"encoding/json"
	"io"
	"log"
	"net/http"
	"net/http/httptest"
	"strings"
)

func LogNoResponseData(hand http.Handler) http.Handler {
	onNoData := func(id, rev, wgt string) {
		log.Printf("[WARN] no data provided for portal %s/%s WGT#%s\n", id, rev, wgt)
	}

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		id, rev, wgt := r.FormValue("proxyId"), r.FormValue("proxyRev"), r.FormValue("proxyWgt")

		recorder := httptest.NewRecorder()
		hand.ServeHTTP(recorder, r)
		reader := io.TeeReader(recorder.Body, w)
		var err error
		if ce := recorder.Header().Get("Content-Encoding"); strings.Contains(ce, "gzip") {
			w.Header().Set("Content-Encoding", "gzip")
			reader, err = gzip.NewReader(reader)
			if err != nil {
				log.Println("[ERROR] reading GZIP:", err)
				io.Copy(w, recorder.Body)
				return
			}
		}

		decoder := json.NewDecoder(reader)
		var jsonData []map[string]json.RawMessage
		err = decoder.Decode(&jsonData)
		if err != nil {
			log.Println("[ERROR] reading JSON:", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		isEmpty := len(jsonData) == 0
		if isEmpty {
			onNoData(id, rev, wgt)
			return
		}

		for _, jsonObj := range jsonData {
			data, hasData := jsonObj["data"]
			_, hasTags := jsonObj["tags"]
			if hasData {
				var dataSlice []interface{}
				err = json.Unmarshal(data, &dataSlice)
				if err != nil {
					log.Println("[ERROR] reading JSON:", err)
					http.Error(w, err.Error(), http.StatusInternalServerError)
					return
				}

				hasNotEmptyData := len(dataSlice) > 0
				if !hasNotEmptyData {
					// Must contain data, but no data exists
					onNoData(id, rev, wgt)
					return
				}
			} else if !hasTags {
				// No data and no tags
				onNoData(id, rev, wgt)
				return
			}
		}
	})
}
