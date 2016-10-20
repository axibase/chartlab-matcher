package settings

import (
	"encoding/json"
	"os"
)

type ServerSettings struct {
	Addr         string                `json:"addr"`
	Proxies      []ProxySettings       `json:"proxies"`
	Configurator *ConfiguratorSettings `json:"configurator"`
}

type ProxySettings struct {
	ProxyTo   string `json:"proxyTo"`
	Method    string `json:"method"`
	Path      string `json:"path"`
	CacheDir  string `json:"cache"`
	CheckData bool   `json:"checkData"`
}

func GetSettingsFrom(filename string) (*ServerSettings, error) {
	file, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	defer file.Close()
	conf := &ServerSettings{}
	err = json.NewDecoder(file).Decode(conf)
	return conf, err
}
