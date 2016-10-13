package main

import (
	"io"
	"net/http"
	"os"
)

func AlertsHandler() http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		file, err := os.Open(ALERTS_SOURCE_FILE)
		if err != nil {
			if os.IsNotExist(err) {
				http.NotFound(w, r)
			} else {
				http.Error(w, err.Error(), http.StatusInternalServerError)
			}
			return
		}
		defer file.Close()
		io.Copy(w, file)
	})
}
