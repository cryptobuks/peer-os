package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.Peer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "message", description = "send message to a specified peer and prints response" )
public class MessageCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "peer id", multiValued = false, required = true, description = "Id of target peer" )
    protected String peerId;
    @Argument( index = 1, name = "message", multiValued = false, required = true, description = "Message to send" )
    private String message;

    private final PeerManager peerManager;
    private static final int TIMEOUT = 5;


    public MessageCommand( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Peer targetPeer = peerManager.getPeer( peerId );

        System.out.println( targetPeer.sendRequest( message, "ECHO_LISTENER", TIMEOUT, String.class, TIMEOUT, null ) );

        return null;
    }
}