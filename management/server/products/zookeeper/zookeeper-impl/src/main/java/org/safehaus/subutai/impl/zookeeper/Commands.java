/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.zookeeper;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.AgentRequestBuilder;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandsSingleton;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;

import java.util.HashSet;
import java.util.Set;


/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton
{

    public static Command getCheckInstalledCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "dpkg -l | grep '^ii' | grep ksks" ), agents );
    }


    public static Command getInstallCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "sleep 20 ; apt-get --force-yes --assume-yes install ksks-zookeeper" )
            .withTimeout( 180 ).withStdOutRedirection( OutputRedirection.NO ), agents );
    }


    public static Command getUninstallCommand( Set<Agent> agents )
    {
        return createCommand(
            new RequestBuilder( "apt-get --force-yes --assume-yes purge ksks-zookeeper" ).withTimeout( 120 )
                .withStdOutRedirection(
                    OutputRedirection.NO ),
            agents );
    }


    public static Command getStartCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service zookeeper start" ), agents );
    }


    public static Command getRestartCommand( Set<Agent> agents )
    {
        return createCommand( new RequestBuilder( "service zookeeper restart" ), agents );
    }


    public static Command getStopCommand( Agent agent )
    {
        return createCommand( new RequestBuilder( "service zookeeper stop" ), Sets.newHashSet( agent ) );
    }


    public static Command getStatusCommand( Agent agent )
    {
        return createCommand( new RequestBuilder( "service zookeeper status" ), Sets.newHashSet( agent ) );
    }


    public static Command getUpdateSettingsCommand( Set<Agent> agents )
    {
        StringBuilder zkNames = new StringBuilder();
        for ( Agent agent : agents )
        {
            zkNames.append( agent.getHostname() ).append( " " );
        }

        Set<AgentRequestBuilder> requestBuilders = new HashSet<>();

        int id = 0;
        for ( Agent agent : agents )
        {
            requestBuilders.add( new AgentRequestBuilder( agent,
                String.format( ". /etc/profile && zookeeper-conf.sh %s && zookeeper-setID.sh %s", zkNames,
                    ++id ) ) );
        }

        return createCommand( requestBuilders );
    }


    public static Command getAddPropertyCommand( String fileName, String propertyName, String propertyValue,
        Set<Agent> agents )
    {
        return createCommand( new RequestBuilder(
            String.format( ". /etc/profile && zookeeper-property.sh add %s %s %s", fileName, propertyName,
                propertyValue ) ), agents );
    }


    public static Command getRemovePropertyCommand( String fileName, String propertyName, Set<Agent> agents )
    {
        return createCommand( new RequestBuilder(
            String.format( ". /etc/profile && zookeeper-property.sh remove %s %s", fileName,
                propertyName ) ), agents );
    }
}
