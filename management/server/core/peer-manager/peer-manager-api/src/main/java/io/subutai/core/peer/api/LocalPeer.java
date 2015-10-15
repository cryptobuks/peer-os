package io.subutai.core.peer.api;


import java.util.Set;

import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{
    /**
     * Binds host with given ID
     *
     * @param id ID of the host
     *
     * @return if host is registered and connected returns implementation of this host, otherwise throws exception.
     */
    public Host bindHost( String id ) throws HostNotFoundException;


    /**
     * Returns implementation of ResourceHost interface.
     *
     * @param hostname name of the resource host
     */

    /**
     * Returns resource host instance by its hostname
     */
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns resource host instance by its id
     */
    public ResourceHost getResourceHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns resource host instance by hostname of its container
     */
    public ResourceHost getResourceHostByContainerName( String containerName ) throws HostNotFoundException;

    /**
     * Returns resource host instance by id ot its container
     */
    public ResourceHost getResourceHostByContainerId( String hostId ) throws HostNotFoundException;


    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostname name of the container
     */

    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException;

    /**
     * Returns implementation of ContainerHost interface.
     *
     * @param hostId ID of the container
     */
    public ContainerHost getContainerHostById( String hostId ) throws HostNotFoundException;

    /**
     * Returns instance of management host
     */
    public ManagementHost getManagementHost() throws HostNotFoundException;

    /**
     * Returns all local peer's resource hosts
     */
    public Set<ResourceHost> getResourceHosts();


    /**
     * Creates container on the local peer
     *
     * @param resourceHost - target resource host where to host container
     * @param template - source template from which to clone container
     * @param containerName - container name
     */
    public ContainerHost createContainer( final ResourceHost resourceHost, final Template template,
                                          final String containerName ) throws PeerException;


    /**
     * Returns container group by container id
     *
     * @param containerId - id of container
     *
     * @return - {@code ContainerGroup}
     *
     * @throws ContainerGroupNotFoundException - thrown if container is created not as a part of environment
     */
    //    public ContainerGroup findContainerGroupByContainerId( String containerId ) throws
    // ContainerGroupNotFoundException;

    /**
     * Returns container group by environment id
     *
     * @param environmentId - id of environment
     *
     * @return - {@code ContainerGroup}
     *
     * @throws ContainerGroupNotFoundException - thrown if group is not found
     */
    //    public ContainerGroup findContainerGroupByEnvironmentId( String environmentId )
    //            throws ContainerGroupNotFoundException;

    /**
     * Returns set of container groups by owner id
     *
     * //     * @param ownerId - id of owner
     *
     * @return - set of {@code ContainerGroup}
     */
    //    public Set<ContainerGroup> findContainerGroupsByOwnerId( String ownerId );
    public void addRequestListener( RequestListener listener );

    public void removeRequestListener( RequestListener listener );

    public Set<RequestListener> getRequestListeners();

    /**
     * Returns domain assigned to vni if any
     *
     * @param vni - vni
     *
     * @return - domain or null if no domain assigned to the vni
     */
    public String getVniDomain( Long vni ) throws PeerException;

    /**
     * Removes domain from vni if any
     *
     * @param vni -vni
     */
    public void removeVniDomain( Long vni ) throws PeerException;

    /**
     * Assigns domain to vni
     *
     * @param vni - vni
     * @param domain -  domain to assign
     * @param domainLoadBalanceStrategy - strategy to load balance requests to the domain
     */

    public void setVniDomain( Long vni, String domain, DomainLoadBalanceStrategy domainLoadBalanceStrategy )
            throws PeerException;

    /**
     * Returns true if hostIp is added to domain by vni
     *
     * @param hostIp - ip of host to check
     * @param vni - vni
     */
    public boolean isIpInVniDomain( String hostIp, Long vni ) throws PeerException;

    public void addIpToVniDomain( String hostIp, Long vni ) throws PeerException;

    public void removeIpFromVniDomain( String hostIp, Long vni ) throws PeerException;

    Set<ContainerHost> findContainersByEnvironmentId( final String environmentId );

    Set<ContainerHost> findContainersByOwnerId( final String ownerId );
}