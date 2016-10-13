package main

import (
	"path"
	"strings"

	"os"

	"github.com/kardianos/osext"
)

//
// Server configurations
//

// Default proxy server TCP port
const SERVER_TCP_PORT = ":8080"

// File name format to store properties
const PROPERTY_CACHE_FILE_NAME_FORMAT = "prop_%s_%s_%s.json.gz" // prop_<id>_<revision>_<widget#>.json

//
// Routing configuration
//

// Path where API proxy is served
const PROXY_PATH = "/proxy"

// Path, where properties are served
const PROPERTY_PROXY_PATH = "/api/v1/properties/"

// Path, where alerts are served
const ALERTS_PROXY_PATH = "/api/v1/alerts/"

//
// Server environment
//

// Working directory
var SERVER_ROOT_DIR, _ = osext.ExecutableFolder()

// Template directory
var TEMPLATE_DIR = path.Join(SERVER_ROOT_DIR, "templates")

// Directory for property cache
var PROPERTY_CACHE_DIR = path.Join(SERVER_ROOT_DIR, "data", "properties")

// Default alerts file
var ALERTS_SOURCE_FILE = path.Join(SERVER_ROOT_DIR, "data", "alerts.json")

//
// Axibase server defaults
//

// Axibase root
var AXIBASE_URL = os.Getenv("AXIBASE_URL")

// Axibase applications root
var AXIBASE_APPS_URL = strings.Replace(AXIBASE_URL, "//", "//apps.", 1)

// Axibase portal configurations path
var CONFIGURATION_DIR = AXIBASE_APPS_URL + "chartlab/directories/"
