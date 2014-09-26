package org.safehaus.subutai.plugin.lucene.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;
import org.safehaus.subutai.plugin.lucene.impl.Commands;
import org.safehaus.subutai.plugin.lucene.impl.LuceneImpl;

import com.google.common.collect.Sets;


public class AddNodeOperationHandler extends AbstractOperationHandler<LuceneImpl>
{
    private final String lxcHostname;


    public AddNodeOperationHandler( LuceneImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( LuceneConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public void run()
    {
        LuceneConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        // Check if node agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation
                    .addLogFailed( String.format( "Node %s is not connected. Operation aborted", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        // Check installed ksks packages
        productOperation.addLog( "Checking prerequisites..." );
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation
                    .addLogFailed( "Failed to check presence of installed subutai packages. Installation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( "ksks-lucene" ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s already has Lucene installed. Installation aborted", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s has no Hadoop installation. Installation aborted", lxcHostname ) );
            return;
        }

        config.getNodes().add( agent );

        productOperation.addLog( "Installing Lucene..." );
        Command installCommand = manager.getCommands().getInstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            productOperation.addLog( "Installation succeeded. Updating db..." );

            try
            {
                manager.getDbManager().saveInfo2( LuceneConfig.PRODUCT_KEY, clusterName, config );

                productOperation.addLogDone( "Information updated in db" );
            }
            catch ( DBException e )
            {
                productOperation
                        .addLogFailed( String.format( "Failed to update information in db, %s", e.getMessage() ) );
            }
        }
        else
        {
            productOperation.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }
    }
}
