package main

import (
	"bufio"
	"bytes"
	"fmt"
	"io"
	"regexp"
	"strings"
	"text/template"
)

const confTemplateStr = `[configuration]
  colors = blueviolet, aquamarine, burlywood, chartreuse, crimson, darkblue, darkgoldenrod, darkorange, darkslateblue, darkcyan, deeppink, dodgerblue, greenyellow, green, lightgreen, lightsalmon, mediumaquamarine, midnightblue, olivedrab, red, saddlebrown, skyblue, slategrey, tomato, yellow, yellowgreen, steelblue, khaki, coral, brown
  url-parameters = proxyId={{ .ProxyId }}&proxyRev={{ .ProxyRev }}
  {{ if .EndTime }}
  end-time = {{ .EndTime }}
  {{end}}
  {{ .ConfigurationBody }}
`

type ConfigurationData struct {
	ApiURL            string
	ProxyId           string
	ProxyRev          string
	EndTime           string
	ConfigurationBody string
}

var confTemplate = template.Must(template.New("conf").Parse(confTemplateStr))

func customizeConfiguration(p *Portal) error {
	confReader, err := p.getConfiguration()
	if err != nil {
		return err
	}

	shouldChangeEndtime := len(p.Endtime) > 0
	conf := removeBadEndtimesFrom(confReader, shouldChangeEndtime)

	wgtCounter := 0
	wgtRe := regexp.MustCompile(`\[widget\]`)
	conf = wgtRe.ReplaceAllStringFunc(conf, func(string) string {
		wgtCounter++
		return fmt.Sprintf("\n[widget]\n  url-parameters = proxyId=%s&proxyRev=%s&proxyWgt=%d",
			p.Id, p.Revision, wgtCounter)
	})

	if configurationContainsProperties(conf) {
		conf = resetEndtimeForProperties(conf)
	}

	conf = strings.Replace(conf, "new Date()", "new Date("+p.Endtime+")", -1)

	var buffer bytes.Buffer
	confTemplate.Execute(&buffer, ConfigurationData{
		ApiURL:            PROXY_PATH,
		ProxyId:           p.Id,
		ProxyRev:          p.Revision,
		EndTime:           p.Endtime,
		ConfigurationBody: conf,
	})

	p.Configuration = buffer.String()
	return nil
}

const endtimePattern = `e[^\w\d\s]*n[^\w\d\s]*d[^\w\d\s]*t[^\w\d\s]*i[^\w\d\s]*m[^\w\d\s]*e`
const goodEndtimePattern = endtimePattern + `[\t\f ]*=[\t\f ]*[0-9]{4}(-[0-9]{2}){2}`

func removeBadEndtimesFrom(reader io.Reader, cleanEndtimes bool) string {
	var buffer bytes.Buffer
	scanner := bufio.NewScanner(reader)
	for scanner.Scan() {
		line := scanner.Text()
		trimmedLine := strings.TrimSpace(line)
		if trimmedLine == "[configuration]" {
			continue
		}

		line = strings.Replace(line, "animat", "noanimat", -1)

		if !cleanEndtimes {
			buffer.WriteString(line)
			buffer.WriteByte('\n')
			continue
		}

		if ok, _ := regexp.MatchString(endtimePattern, trimmedLine); !ok {
			buffer.WriteString(line)
			buffer.WriteByte('\n')
		} else if ok, _ = regexp.MatchString(goodEndtimePattern, trimmedLine); ok {
			buffer.WriteString(line)
			buffer.WriteByte('\n')
		}
	}
	return buffer.String()
}

func configurationContainsProperties(conf string) bool {
	re := regexp.MustCompile(`\n[\t\f ]*t[^\d\w=]*y[^\d\w=]*p[^\d\w=]*e[\t\f ]*=[\t\f ]*property`)
	return re.MatchString(conf)
}

func resetEndtimeForProperties(conf string) string {
	re := regexp.MustCompile(`\n[\t\f ]*t[^\d\w=]*y[^\d\w=]*p[^\d\w=]*e[\t\f ]*=[\t\f ]*property`)
	conf = re.ReplaceAllString(conf, "\ntype = property\nendtime = now")
	return conf
}
