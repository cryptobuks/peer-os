package lib

import (
	"encoding/json"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"strconv"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

type snap struct {
	Version string `json:"version"`
}

func getAvailable(name string) string {
	var update snap
	client := &http.Client{}
	resp, err := client.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/file/info?name=" + name)
	log.Check(log.FatalLevel, "GET: https://"+config.Cdn.Url+":"+config.Cdn.Sslport+"/kurjun/rest/file/info?name="+name, err)
	defer resp.Body.Close()
	js, err := ioutil.ReadAll(resp.Body)
	log.Check(log.FatalLevel, "Reading response", err)
	log.Check(log.FatalLevel, "Parsing file list", json.Unmarshal(js, &update))
	log.Debug("Available: " + update.Version)
	return update.Version
}

func getInstalled() string {
	f, err := ioutil.ReadFile(config.Agent.AppPrefix + "/meta/package.yaml")
	if !log.Check(log.DebugLevel, "Reading file package.yaml", err) {
		lines := strings.Split(string(f), "\n")
		for _, v := range lines {
			if strings.HasPrefix(v, "version: ") {
				if version := strings.Split(strings.TrimPrefix(v, "version: "), "-"); len(version) > 1 {
					log.Debug("Installed: " + version[1])
					return version[1]
				}
			}
		}
	}
	return "0"
}

func upgradeRh(name string) {
	log.Info("Updating Resource host")
	file, err := os.Create("/tmp/" + name)
	log.Check(log.FatalLevel, "Creating update file", err)
	defer file.Close()
	client := &http.Client{}
	resp, err := client.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/file/get?name=" + name)
	log.Check(log.FatalLevel, "GET: https://"+config.Cdn.Url+":"+config.Cdn.Sslport+"/kurjun/rest/file/get?name="+name, err)
	defer resp.Body.Close()
	_, err = io.Copy(file, resp.Body)
	log.Check(log.FatalLevel, "Writing response to file", err)

	log.Check(log.FatalLevel, "Installing update /tmp/"+name,
		exec.Command("snappy", "install", "--allow-unauthenticated", "/tmp/"+name).Run())
	log.Check(log.FatalLevel, "Removing update file /tmp/"+name, os.Remove("/tmp/"+name))

}

func Update(name string, check bool) {
	switch name {
	case "rh":
		if !lockSubutai("rh.update") {
			log.Error("Another update process is already running")
		}
		defer unlockSubutai()

		name := "subutai_" + config.Template.Version + "_" + config.Template.Arch + ".snap"
		if len(config.Template.Branch) != 0 {
			name = "subutai_" + config.Template.Version + "_" + config.Template.Arch + "-" + config.Template.Branch + ".snap"
		}

		installed, err := strconv.Atoi(getInstalled())
		log.Check(log.FatalLevel, "Converting installed package timestamp to int", err)
		available, err := strconv.Atoi(getAvailable(name))
		log.Check(log.FatalLevel, "Converting available package timestamp to int", err)

		if installed >= available {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update is avalable")
			os.Exit(0)
		}

		upgradeRh(name)

	default:
		if !container.IsContainer(name) {
			log.Error("no such instance \"" + name + "\"")
		}
		_, err := container.AttachExec(name, []string{"apt-get", "update", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-qq"})
		log.Check(log.FatalLevel, "Updating apt index", err)
		output, err := container.AttachExec(name, []string{"apt-get", "upgrade", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-s", "-qq"})
		log.Check(log.FatalLevel, "Checking for available updade", err)
		if len(output) == 0 {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update is avalable")
			os.Exit(0)
		}
		_, err = container.AttachExec(name, []string{"apt-get", "upgrade", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-qq"})
		log.Check(log.FatalLevel, "Updating container", err)
	}
}
