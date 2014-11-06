package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.core.environment.api.helper.Environment;


public interface ClusterConfigurationInterface{

    /**
     * Configures cluster with the given configuration
     * @param config cluster configuration object
     * @param environment TODO
     * @throws ClusterConfigurationException
     */
    public void configureCluster( ConfigBase config, Environment environment ) throws ClusterConfigurationException;
}
