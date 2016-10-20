package main

import (
	"bufio"
	"bytes"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"proxy/settings"
	"regexp"
	"strings"
)

type PortalConfigurator interface {
	GetConfiguration(*Portal) string
}

func NewPortalConfigurator(settings *settings.ConfiguratorSettings) PortalConfigurator {
	if settings == nil {
		panic("no configurator settings")
	}
	if len(settings.Src) == 0 {
		panic("no confugurator source (this feature may be added later)")
	}

	return newRemoteConfigurator(settings)
}

type remoteConfigurator struct {
	url      *url.URL
	settings *settings.ConfiguratorSettings
}

func newRemoteConfigurator(settings *settings.ConfiguratorSettings) *remoteConfigurator {
	return &remoteConfigurator{
		settings: settings,
	}
}

func (c *remoteConfigurator) GetConfiguration(portal *Portal) string {
	urlString := fmt.Sprintf("%s/%s/%s", c.settings.Src, portal.Id, portal.Revision)
	resp, err := http.Get(urlString)
	if err != nil {
		log.Println("[ERROR] Unable to read configuration for", portal, ":", err)
		return ""
	}

	bytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Println("[ERROR] Unable to read configuration for", portal, ":", err)
		return ""
	}

	conf := string(bytes)
	conf = c.fixEndtimes(conf, portal)
	conf = c.disableAnimation(conf)
	conf = c.addColors(conf, c.settings.Colors)
	conf = c.enumerateWidgets(conf, portal)
	return conf
}

const endtimePattern = `e[^\w\d\s]*n[^\w\d\s]*d[^\w\d\s]*t[^\w\d\s]*i[^\w\d\s]*m[^\w\d\s]*e`
const goodEndtimePattern = endtimePattern + `[\t\f ]*=[\t\f ]*[0-9]{4}(-[0-9]{2}){2}`

func (c *remoteConfigurator) fixEndtimes(conf string, portal *Portal) string {
	if len(portal.Endtime) == 0 {
		// No need to set endtime
		return conf
	} else if portal.Endtime == "none" {
		// Not specified endtime
		log.Printf("[WARN] Endtime for portal %s is not specified, results may be incorrect\n", portal)
		return conf
	}

	// Remove bad endtimes
	var buffer bytes.Buffer
	var scanner = bufio.NewScanner(strings.NewReader(conf))
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if ok, _ := regexp.MatchString(endtimePattern, line); !ok {
			buffer.WriteString(line)
			buffer.WriteByte('\n')
		} else if ok, _ = regexp.MatchString(goodEndtimePattern, line); ok {
			buffer.WriteString(line)
			buffer.WriteByte('\n')
		}
	}
	conf = buffer.String()

	// Set global endtime
	conf = strings.Replace(conf, "[configuration]", "[configuration]\nendtime = "+portal.Endtime, 1)

	// Replace new Date() with endtime
	conf = strings.Replace(conf, "new Date()", "new Date("+portal.Endtime+")", -1)

	// Reset endtimes for properties
	re := regexp.MustCompile(`\n[\t\f ]*t[^\d\w=]*y[^\d\w=]*p[^\d\w=]*e[\t\f ]*=[\t\f ]*property`)
	conf = re.ReplaceAllString(conf, "\ntype = property\nendtime = now")

	// Reset endtimes for console
	re = regexp.MustCompile(`\n[\t\f ]*t[^\d\w=]*y[^\d\w=]*p[^\d\w=]*e[\t\f ]*=[\t\f ]*console`)
	conf = re.ReplaceAllString(conf, "\ntype = console\nendtime = now")

	// Reset update interval
	re = regexp.MustCompile(`\n[\t\f ]*u[^\d\w=]*p[^\d\w=]*d[^\d\w=]*a[^\d\w=]*t[^\d\w=]*e[^\d\w=]*i[^\d\w=]*n[^\d\w=]*t[^\d\w=]*e[^\d\w=]*r[^\d\w=]*v[^\d\w=]*a[^\d\w=]*l[\t\f ]*=[^\n]*`)
	conf = re.ReplaceAllString(conf, "")
	return conf
}

func (c *remoteConfigurator) addColors(conf string, colors []string) string {
	if len(colors) > 0 {
		colorsStr := strings.Join(colors, ", ")
		conf = strings.Replace(conf, "[configuration]", "[configuration]\ncolors = "+colorsStr, 1)
	}
	return conf
}

func (c *remoteConfigurator) disableAnimation(conf string) string {
	conf = strings.Replace(conf, "animat", "noanimat", -1)
	return conf
}

func (c *remoteConfigurator) enumerateWidgets(conf string, portal *Portal) string {
	var wgtCounter = 0
	wgtRe := regexp.MustCompile(`\[widget\]`)
	conf = wgtRe.ReplaceAllStringFunc(conf, func(string) string {
		wgtCounter++
		return fmt.Sprintf("\n[widget]\nurl-parameters = proxyId=%s&proxyRev=%s&proxyWgt=%d",
			portal.Id, portal.Revision, wgtCounter)
	})
	return conf
}
