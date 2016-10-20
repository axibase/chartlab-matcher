package main

import (
	"html/template"
	"log"
	"net/http"
	"strings"
)

type Portal struct {
	Id            string
	Revision      string
	Endtime       string
	Configuration string
}

func (p Portal) String() string {
	return p.Id + "/" + p.Revision
}

type portalHandler struct {
	tmpl         *template.Template
	configurator PortalConfigurator
}

func NewPortalHandler(configurator PortalConfigurator) http.Handler {
	hand := &portalHandler{}
	hand.configurator = configurator
	hand.tmpl = template.Must(template.New("portal").Parse(portalHTML))
	return hand
}

func (h *portalHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	portal := CreatePortalFromRequest(r)
	if portal == nil {
		http.NotFound(w, r)
		return
	}

	portal.Configuration = h.configurator.GetConfiguration(portal)
	h.tmpl.Execute(w, portal)
}

func CreatePortalFromRequest(r *http.Request) *Portal {
	pathParts := strings.Split(r.URL.Path[1:], "/")
	switch len(pathParts) {
	case 0:
		log.Println("[WARN] portal not specified")
		return nil

	case 1:
		return &Portal{Id: pathParts[0], Revision: "1", Endtime: r.FormValue("endtime")}

	default:
		return &Portal{Id: pathParts[0], Revision: pathParts[1], Endtime: r.FormValue("endtime")}
	}
	panic("unreachable")
}

const portalHTML = `<!DOCTYPE html>
<meta charset="utf-8">
<title>Portal {{ .Id }} / {{ .Revision }}</title>
<link rel="stylesheet" href="/JavaScript/jquery-ui-1.9.0.custom/css/smoothness/jquery-ui-1.9.1.custom.min.css">
<link rel="stylesheet" href="/CSS/charts.min.css">
<script src="/JavaScript/portal_init.js"></script>
<script src="/JavaScript/jquery-ui-1.9.0.custom/js/jquery-1.8.2.min.js"></script>
<script src="/JavaScript/jquery-ui-1.9.0.custom/js/jquery-ui-1.9.0.custom.min.js"></script>
<script src="/JavaScript/d3.min.js"></script>
<script src="/JavaScript/highlight.pack.js"></script>
<script src="/JavaScript/charts.min.js"></script>
<style>
  * {
    font-family: monospace !important;
    font-smooth: never !important;
    -webkit-font-smoothing: none !important;
  }
</style>
<body onload="onBodyLoad()">
  <script>
    document.isPortalReady = function() {
      return (function () {
        // Any tooltip loaded
        return $('.axi-tooltip').size() > 0;
      })() && (function () {
        var loadingTooltips = $('.axi-tooltip-inner:contains("Load")');

        // Any loading tooltip exists in DOM
        var ltex = (loadingTooltips.size() > 0)

        // All loading tooltips are invisible
        return ltex && !loadingTooltips.is(":visible");
      })()
    };

    initializePortal(function() {
      var configText = "{{ .Configuration }}";
      return [configText, window.portalPlaceholders = getPortalPlaceholders()];
    });
  </script>
  <div class="portalView"></div>
  <div id="dialog"></div>
</body>`
