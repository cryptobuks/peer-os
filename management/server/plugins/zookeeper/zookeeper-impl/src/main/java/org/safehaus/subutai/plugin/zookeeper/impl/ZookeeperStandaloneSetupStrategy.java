package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.shared.protocol.FileUtil;
import org.safehaus.subutai.shared.protocol.NodeGroup;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * This is a standalone zk cluster setup strategy.
 */
public class ZookeeperStandaloneSetupStrategy implements ClusterSetupStrategy {

    private final ZookeeperClusterConfig config;
    private final ZookeeperImpl zookeeperManager;
    private final ProductOperation po;


    public ZookeeperStandaloneSetupStrategy( final ZookeeperClusterConfig config, ProductOperation po,
                                             ZookeeperImpl zookeeperManager ) {
        this.config = config;
        this.po = po;
        this.zookeeperManager = zookeeperManager;
    }


    public static PlacementStrategy getNodePlacementStrategy() {
        return PlacementStrategy.ROUND_ROBIN;
    }


    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( ZookeeperClusterConfig config ) {


        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", ZookeeperClusterConfig.PRODUCT_KEY, UUID.randomUUID() ) );

        //node group
        NodeGroup nodesGroup = new NodeGroup();
        nodesGroup.setName( "DEFAULT" );
        nodesGroup.setNumberOfNodes( config.getNumberOfNodes() );
        nodesGroup.setTemplateName( config.getTemplateName() );
        nodesGroup.setPlacementStrategy( getNodePlacementStrategy() );


        environmentBlueprint.setNodeGroups( Sets.newHashSet( nodesGroup ) );

        return environmentBlueprint;
    }


    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {

        try {

            if ( config.getNodes() == null || config.getNodes().isEmpty() ) {
                //setup environment
                po.addLog( "Building environment..." );
                try {
                    Environment env = zookeeperManager.getEnvironmentManager().buildEnvironmentAndReturn(
                            getDefaultEnvironmentBlueprint( config ) );

                    Set<Agent> zkAgents = new HashSet<>();
                    for ( Node node : env.getNodes() ) {
                        zkAgents.add( node.getAgent() );
                    }
                    config.setNodes( zkAgents );
                }
                catch ( EnvironmentBuildException e ) {
                    throw new ClusterSetupException(
                            String.format( "Error building environment: %s", e.getMessage() ) );
                }
            }
            po.addLog( "Configuring cluster..." );

            Command configureClusterCommand = Commands.getConfigureClusterCommand( config.getNodes(),
                    ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                    prepareConfiguration( config.getNodes() ), ConfigParams.CONFIG_FILE_PATH.getParamValue() );

            zookeeperManager.getCommandRunner().runCommand( configureClusterCommand );

            if ( configureClusterCommand.hasSucceeded() ) {

                po.addLog( String.format( "Cluster configured\nStarting %s...", ZookeeperClusterConfig.PRODUCT_KEY ) );
                //start all nodes
                Command startCommand = Commands.getStartCommand( config.getNodes() );
                final AtomicInteger count = new AtomicInteger();
                zookeeperManager.getCommandRunner().runCommand( startCommand, new CommandCallback() {
                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command ) {
                        if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                            if ( count.incrementAndGet() == config.getNodes().size() ) {
                                stop();
                            }
                        }
                    }
                } );

                if ( count.get() == config.getNodes().size() ) {
                    po.addLog( String.format( "Starting %s succeeded\nDone", ZookeeperClusterConfig.PRODUCT_KEY ) );
                }
                else {
                    po.addLog( String.format( "Starting %s failed, %s, skipping...", ZookeeperClusterConfig.PRODUCT_KEY,
                            startCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterSetupException(
                        String.format( "Failed to configure cluster, %s", configureClusterCommand.getAllErrors() ) );
            }
        }
        catch ( ClusterConfigurationException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }

        return config;
    }


    //temporary workaround until we get full configuration injection working
    public static String prepareConfiguration( Set<Agent> nodes ) throws ClusterConfigurationException {
        String zooCfgFile = FileUtil.getContent( "conf/zoo.cfg", ZookeeperStandaloneSetupStrategy.class );

        if ( Strings.isNullOrEmpty( zooCfgFile ) ) {
            throw new ClusterConfigurationException( "Zoo.cfg resource is missing" );
        }

        zooCfgFile = zooCfgFile
                .replace( "$" + ConfigParams.DATA_DIR.getPlaceHolder(), ConfigParams.DATA_DIR.getParamValue() );

        /*
        1=zookeeper1:2888:3888
        2=zookeeper2:2888:3888
        3=zookeeper3:2888:3888
         */

        StringBuilder serversBuilder = new StringBuilder();
        int id = 0;
        for ( Agent agent : nodes ) {
            serversBuilder.append( ++id ).append( "=" ).append( agent.getHostname() )
                          .append( ConfigParams.PORTS.getParamValue() ).append( "\n" );
        }

        zooCfgFile = zooCfgFile.replace( "$" + ConfigParams.SERVERS.getPlaceHolder(), serversBuilder.toString() );


        return zooCfgFile;
    }
}
