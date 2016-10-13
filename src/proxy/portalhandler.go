package main

import (
	"html/template"
	"net/http"
	"path"
	"strings"
	"sync"
)

const portalTemplateFile = "index.gohtml"

type PortalHandler struct {
	templateOnce   sync.Once
	portalTemplate *template.Template
}

func (p *PortalHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	portalTemplatePath := path.Join(TEMPLATE_DIR, portalTemplateFile)
	p.templateOnce.Do(func() {
		p.portalTemplate = template.Must(template.ParseFiles(portalTemplatePath))
	})

	portal := p.createPortalFromRequest(r)

	err := customizeConfiguration(portal)
	if err != nil {
		http.Error(w, err.Error(), http.StatusNotFound)
		return
	}

	p.portalTemplate.Execute(w, portal)
}

func (p *PortalHandler) createPortalFromRequest(req *http.Request) *Portal {
	pathParts := strings.Split(req.URL.Path, "/")[1:]
	proxyId := ""
	proxyRev := "1"
	if len(pathParts) > 0 {
		proxyId = pathParts[0]
		if len(pathParts) > 1 {
			proxyRev = pathParts[1]
		}
	}

	return &Portal{
		Id:       proxyId,
		Revision: proxyRev,
		Endtime:  req.FormValue("endtime"),
	}
}
