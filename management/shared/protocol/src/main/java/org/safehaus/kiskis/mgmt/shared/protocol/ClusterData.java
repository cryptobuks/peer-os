/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author bahadyr
 */
public class ClusterData {

    private UUID uuid;
    private String name;
    private List<String> nodes;
    private List<String> seeds;
    private String dataDir;
    private String commitLogDir;
    private String savedCacheDir;

    public ClusterData() {
        this.uuid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<String> seeds) {
        this.seeds = seeds;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getCommitLogDir() {
        return commitLogDir;
    }

    public void setCommitLogDir(String commitLogDir) {
        this.commitLogDir = commitLogDir;
    }

    public String getSavedCacheDir() {
        return savedCacheDir;
    }

    public void setSavedCacheDir(String savedCacheDir) {
        this.savedCacheDir = savedCacheDir;
    }

    @Override
    public String toString() {
        return "Cluster{" + "uuid=" + uuid + ", name=" + name + ", nodes=" + nodes + ", seeds=" + seeds + ", dataDir=" + dataDir + ", commitLogDir=" + commitLogDir + ", savedCacheDir=" + savedCacheDir + '}';
    }

}
