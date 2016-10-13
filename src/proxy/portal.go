package main

import (
	"compress/gzip"
	"io"
	"net/http"
	"strings"
)

type Portal struct {
	Id            string
	Revision      string
	Endtime       string
	Configuration string
}

func (p *Portal) getConfiguration() (io.Reader, error) {
	resp, err := http.Get(CONFIGURATION_DIR + p.Id + "/" + p.Revision)
	if err != nil {
		return nil, err
	}
	reader := resp.Body
	if enc := resp.Header.Get("Content-Encoding"); strings.Contains(enc, "gzip") {
		reader, err = gzip.NewReader(reader)
		if err != nil {
			return nil, err
		}
	}

	return reader, nil
}
