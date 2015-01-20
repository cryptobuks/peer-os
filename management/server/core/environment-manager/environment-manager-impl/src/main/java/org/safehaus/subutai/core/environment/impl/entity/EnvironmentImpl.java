package org.safehaus.subutai.core.environment.impl.entity;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentStatusEnum;
import org.safehaus.subutai.common.peer.ContainerHost;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;


@Entity
@Table( name = "environment" )
@Access( AccessType.FIELD )
public class EnvironmentImpl implements Environment, Serializable
{
    @Id
    @Column( name = "environment_id" )
    private String environmentId;

    @Column( name = "name" )
    private String name;

    @OneToMany( mappedBy = "environment", fetch = FetchType.EAGER, targetEntity = EnvironmentContainerImpl.class,
                cascade = CascadeType.ALL, orphanRemoval = true )
    private Set<ContainerHost> containers = new HashSet<>();

    @Enumerated( EnumType.STRING )
    private EnvironmentStatusEnum status;

    @Column( name = "create_time" )
    private long creationTimestamp;

    @Column( name = "public_key", length = 3000 )
    private String publicKey;

    @Column( name = "subnetCidr" )
    private String subnetCidr;

    @Column( name = "lastUsedIpIndex" )
    private int lastUsedIpIndex;

    @Column( name = "peerVlanInfo" )
    private String peerVlanInfo;

    @Column( name = "vni" )
    private int vni;


    protected EnvironmentImpl()
    {
    }


    public EnvironmentImpl( String name )
    {
        this.name = name;
        this.environmentId = UUID.randomUUID().toString();
        this.status = EnvironmentStatusEnum.EMPTY;
        this.creationTimestamp = System.currentTimeMillis();
    }


    @Override
    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }


    @Override
    public EnvironmentStatusEnum getStatus()
    {
        return status;
    }


    @Override
    public void setStatus( final EnvironmentStatusEnum status )
    {
        this.status = status;
    }


    @Override
    public void addContainer( ContainerHost container )
    {
        if ( container == null )
        {
            throw new IllegalArgumentException( "Environment container could not be null." );
        }
        if ( !( container instanceof EnvironmentContainerImpl ) )
        {
            throw new IllegalArgumentException( "Unknown Environment container implementation." );
        }
        EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) container;
        environmentContainer.setEnvironment( this );
        this.containers.add( environmentContainer );
    }


    @Override
    public Set<ContainerHost> getContainerHosts()
    {
        return containers;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public UUID getId()
    {
        return UUID.fromString( environmentId );
    }


    @Override
    public String getPublicKey()
    {
        return publicKey;
    }


    @Override
    public void setPublicKey( String publicKey )
    {
        this.publicKey = publicKey;
    }


    @Override
    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    public void setSubnetCidr( String subnetCidr )
    {
        // this ctor checks CIDR notation format validity
        SubnetUtils cidr = new SubnetUtils( subnetCidr );
        this.subnetCidr = cidr.getInfo().getCidrSignature();
    }


    public int getLastUsedIpIndex()
    {
        return lastUsedIpIndex;
    }


    public void setLastUsedIpIndex( int lastUsedIpIndex )
    {
        this.lastUsedIpIndex = lastUsedIpIndex;
    }


    @Override
    public Map<UUID, Integer> getPeerVlanInfo()
    {
        Map<UUID, Integer> map = deserializePeerVlanInfo();
        return Collections.unmodifiableMap( map );
    }


    public void setPeerVlanInfo( UUID peerId, int vlanId )
    {
        Map<UUID, Integer> map = deserializePeerVlanInfo();
        map.put( peerId, vlanId );
        this.peerVlanInfo = JsonUtil.to( map );
    }


    @Override
    public int getVni()
    {
        return vni;
    }


    public void setVni( int vni )
    {
        this.vni = vni;
    }


    @Override
    public ContainerHost getContainerHostById( UUID uuid )
    {
        Iterator<ContainerHost> iterator = getContainerHosts().iterator();
        while ( iterator.hasNext() )
        {
            ContainerHost containerHost = iterator.next();
            if ( containerHost.getId().equals( uuid ) )
            {
                return containerHost;
            }
        }
        return null;
    }


    @Override
    public ContainerHost getContainerHostByHostname( String hostname )
    {
        Iterator<ContainerHost> iterator = getContainerHosts().iterator();
        while ( iterator.hasNext() )
        {
            ContainerHost containerHost = iterator.next();
            if ( containerHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return containerHost;
            }
        }
        return null;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids )
    {
        Set<ContainerHost> hosts = Sets.newHashSet();
        for ( UUID id : ids )
        {
            ContainerHost host = getContainerHostById( id );
            if ( host != null )
            {
                hosts.add( host );
            }
        }
        return hosts;
    }


    @Override
    public void addContainers( final Set<ContainerHost> containerHosts )
    {
        for ( ContainerHost containerHost : containerHosts )
        {
            EnvironmentContainerImpl environmentContainer = ( EnvironmentContainerImpl ) containerHost;
            environmentContainer.setEnvironment( this );
            containers.add( environmentContainer );
        }
    }


    @Override
    public void removeContainer( final ContainerHost containerHost )
    {
        getContainerHosts().remove( containerHost );
    }


    private Map<UUID, Integer> deserializePeerVlanInfo()
    {
        if ( peerVlanInfo == null || peerVlanInfo.isEmpty() )
        {
            return new HashMap<>();
        }
        TypeToken<Map<UUID, Integer>> typeToken = new TypeToken<Map<UUID, Integer>>()
        {
        };
        return JsonUtil.fromJson( peerVlanInfo, typeToken.getType() );
    }

}

