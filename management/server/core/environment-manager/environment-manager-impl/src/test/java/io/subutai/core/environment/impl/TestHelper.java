package io.subutai.core.environment.impl;


import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.security.api.crypto.KeyManager;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class TestHelper
{
    public static final String SUBNET_CIDR = "192.168.0.1/24";
    public static final String CONTAINER_ID = "123";
    public static final String PEER_NAME = "peer123";
    public static final String SSH_KEY = "key";
    public static final String RH_ID = "123";
    public static final String PEER_ID = "123";
    public static final String ENV_ID = "123";
    public static final String OWNER_ID = "123";
    public static final String TEMPLATE_ID = "123";
    public static final String TEMPLATE_NAME = "template";
    public static final String HOSTNAME = "hostname";
    public static final String CONTAINER_NAME = "container";
    public static final String MESSAGE = "msg";
    public static final EnvironmentId ENVIRONMENT_ID = new EnvironmentId( ENV_ID );
    public static final HostId RES_HOST_ID = new HostId( RH_ID );
    public static final ContainerId CONT_HOST_ID = new ContainerId( CONTAINER_ID );
    private static final String P2P_SUBNET = "10.10.10.1";


    public static EnvironmentImpl ENVIRONMENT()
    {
        EnvironmentImpl ENVIRONMENT = mock( EnvironmentImpl.class );
        doReturn( new EnvironmentId( ENV_ID ) ).when( ENVIRONMENT ).getEnvironmentId();
        doReturn( ENV_ID ).when( ENVIRONMENT ).getId();
        doReturn( SUBNET_CIDR ).when( ENVIRONMENT ).getSubnetCidr();
        doReturn( P2P_SUBNET ).when( ENVIRONMENT ).getP2pSubnet();
        doReturn( Sets.newHashSet( SSH_KEY ) ).when( ENVIRONMENT ).getSshKeys();


        return ENVIRONMENT;
    }


    public static SshKeys SSH_KEYS()
    {
        SshKeys sshKeys =
                new SshKeys( Sets.newHashSet( new SshKey( CONTAINER_ID, SshEncryptionType.RSA, TestHelper.SSH_KEY ) ) );
        return sshKeys;
    }


    public static KeyManager KEY_MANAGER()
    {
        return mock( KeyManager.class );
    }


    public static Node NODE()
    {
        Node NODE = mock( Node.class );
        doReturn( ContainerSize.SMALL ).when( NODE ).getType();
        doReturn( TEMPLATE_ID ).when( NODE ).getTemplateId();
        doReturn( RH_ID ).when( NODE ).getHostId();
        doReturn( HOSTNAME ).when( NODE ).getHostname();
        doReturn( CONTAINER_NAME ).when( NODE ).getName();

        return NODE;
    }


    public static TrackerOperation TRACKER_OPERATION()
    {
        return mock( TrackerOperation.class );
    }


    public static Peer PEER()
    {
        Peer PEER = mock( Peer.class );
        doReturn( PEER_ID ).when( PEER ).getId();
        doReturn( PEER_NAME ).when( PEER ).getName();

        return PEER;
    }


    public static LocalPeer LOCAL_PEER()
    {
        LocalPeer LOCAL_PEER = mock( LocalPeer.class );
        doReturn( PEER_ID ).when( LOCAL_PEER ).getId();
        doReturn( PEER_NAME ).when( LOCAL_PEER ).getName();
        doReturn( OWNER_ID ).when( LOCAL_PEER ).getOwnerId();


        return LOCAL_PEER;
    }


    public static EnvironmentContainerImpl ENV_CONTAINER()
    {
        EnvironmentContainerImpl environmentContainer = mock( EnvironmentContainerImpl.class );
        doReturn( ENVIRONMENT_ID ).when( environmentContainer ).getEnvironmentId();
        doReturn( CONTAINER_ID ).when( environmentContainer ).getId();
        doReturn( CONT_HOST_ID ).when( environmentContainer ).getContainerId();
        doReturn( PEER_ID ).when( environmentContainer ).getPeerId();
        doReturn( CONTAINER_NAME ).when( environmentContainer ).getContainerName();
        doReturn( HOSTNAME ).when( environmentContainer ).getHostname();
        doReturn( RES_HOST_ID ).when( environmentContainer ).getResourceHostId();
        doReturn( OWNER_ID ).when( environmentContainer ).getOwnerId();
        doReturn( TEMPLATE_ID ).when( environmentContainer ).getTemplateId();
        doReturn( TEMPLATE_NAME ).when( environmentContainer ).getTemplateName();
        HostInterfaceModel hostInterface =
                new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, Common.LOCAL_HOST_IP );
        doReturn( new HostInterfaces( CONTAINER_ID, Sets.newHashSet( hostInterface ) ) ).when( environmentContainer )
                                                                                        .getHostInterfaces();
        doCallRealMethod().when( environmentContainer ).getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

        return environmentContainer;
    }


    public static void bind( Environment environment, Peer peer, PeerUtil peerUtil,
                             PeerUtil.PeerTaskResults peerTaskResults, PeerUtil.PeerTaskResult peerTaskResult )
            throws PeerException
    {

        doReturn( Sets.newHashSet( peer ) ).when( environment ).getPeers();
        doReturn( peerTaskResults ).when( peerUtil ).executeParallel();
        doReturn( peerTaskResults ).when( peerUtil ).executeParallelFailFast();
        doReturn( Sets.newHashSet( peerTaskResult ) ).when( peerTaskResults ).getPeerTaskResults();
        doReturn( peer ).when( peerTaskResult ).getPeer();
    }
}