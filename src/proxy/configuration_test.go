package main

import (
	"strings"
	"testing"
)

func TestItWorks(t *testing.T) {
	t.SkipNow()
}

func TestRemoveBadEndtimes(t *testing.T) {
	var exps, input []string

	goodConf, goodExp := `[configuration]
	endtime = 2016-09-10T00:00:00.0000Z`, `endtime = 2016-09-10T00:00:00.0000Z`
	exps = append(exps, goodExp)
	input = append(input, goodConf)

	badConf, badExp := `[configuration]
	endtime = `, ``
	exps = append(exps, badExp)
	input = append(input, badConf)

	badConf, badExp = `[widget]
	endtime = `, `[widget]`
	exps = append(exps, badExp)
	input = append(input, badConf)

	badConf, badExp = `[widget]
	someshit = ololo
	endtime =
	ololo = someshit`, `[widget]
	someshit = ololo
	ololo = someshit`
	exps = append(exps, badExp)
	input = append(input, badConf)

	badConf, badExp = `[configuration]
	endtime = 2016-09-20
	[widget]
	endtime = `, `
	
	endtime = 2016-09-20
	[widget]`
	exps = append(exps, badExp)
	input = append(input, badConf)

	badConf, badExp = `[configuration]
	endtime = now`, ``
	exps = append(exps, badExp)
	input = append(input, badConf)

	for i, conf := range input {
		reader := strings.NewReader(conf)
		got := removeBadEndtimesFrom(reader, true)
		got = strings.TrimSpace(got)

		exp := exps[i]
		exp = strings.TrimSpace(exp)

		if got != exp {
			t.Error("bad endtime removal,\n\texpected:", exp, "\n\tgot:", got)
		}
	}

}

func TestContainsProperties(t *testing.T) {
	var goods []string

	goods = append(goods, "[configuration]\n [widget]\n  type=property\n")
	goods = append(goods, "[configuration]\n [widget]\nty-pe=property\n")
	goods = append(goods, "[configuration]\n [widget]\n  type = property\n")
	goods = append(goods, "[configuration]\n ololo = ololo\n  [widget]\ntype=property\n")

	for _, conf := range goods {
		if !configurationContainsProperties(conf) {
			t.Error("Wrong negative on", conf)
		}
	}

	var bads []string

	bads = append(bads, "[configuration]\n [widget]\n  ty0pe=property\n")
	bads = append(bads, "[configuration]\n [widget]\nty-pe=console\n")
	bads = append(bads, "[configuration]\n [widget]\n  ololo = type \n property = property\n")
	bads = append(bads, "[configuration]\n ololo = type=property\n  [widget]\n")

	for _, conf := range bads {
		if configurationContainsProperties(conf) {
			t.Error("Wrong positive on", conf)
		}
	}
}

func TestResetEndtimeForProperties(t *testing.T) {
	var goods []string

	goods = append(goods, "[configuration]\n [widget]\n  type=property\n")
	goods = append(goods, "[configuration]\n [widget]\nty-pe=property\n")
	goods = append(goods, "[configuration]\n [widget]\n  type = property\n")
	goods = append(goods, "[configuration]\n ololo = ololo\n  [widget]\ntype=property\n")

	for _, conf := range goods {
		if fixed := resetEndtimeForProperties(conf); !strings.Contains(fixed, "endtime = now") {
			t.Error("Wrong negative on", conf, "\ngot:\n\t", fixed)
		}
	}

	var bads []string

	bads = append(bads, "[configuration]\n [widget]\n  ty0pe=property\n")
	bads = append(bads, "[configuration]\n [widget]\nty-pe=console\n")
	bads = append(bads, "[configuration]\n [widget]\n  ololo = type \n property = property\n")
	bads = append(bads, "[configuration]\n ololo = type=property\n  [widget]\n")

	for _, conf := range bads {
		if fixed := resetEndtimeForProperties(conf); strings.Contains(fixed, "endtime = now") {
			t.Error("Wrong positive on", conf, "\ngot:\n\t", fixed)
		}
	}
}
