package io.subutai.core.channel.impl.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;

import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.test.Step1Interceptor;
import io.subutai.core.channel.impl.test.Step2Interceptor;
import io.subutai.core.channel.impl.test.Step3Interceptor;
import io.subutai.core.channel.impl.test.Step4Interceptor;


/**
 * Bus listener class
 */
public class ServerBusListener extends AbstractFeature
{
    private final static Logger LOG = LoggerFactory.getLogger( ServerBusListener.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public void busRegistered( Bus bus )
    {
        LOG.info( "Adding LoggingFeature interceptor on bus: " + bus );

        // initialise the feature on the bus, which will add the interceptors

        //***** PRE_STREAM **********************************
        bus.getOutInterceptors().add( new ClientOutInterceptor(channelManagerImpl) );

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new ServerInInterceptor(channelManagerImpl) );

        //***** PRE_STREAM **********************************
        bus.getOutInterceptors().add( new ServerOutInterceptor(channelManagerImpl) );

        //***** RECEIVE    **********************************
        bus.getInInterceptors().add( new ClientInInterceptor(channelManagerImpl) );


        //        bus.getInInterceptors().add( new ServerOutInterceptor(channelManagerImpl) );
        //        bus.getOutInterceptors().add( new TestOutInterceptor() );

        //bus.getOutInterceptors().add( new Step1Interceptor() );
        //bus.getInInterceptors().add( new Step2Interceptor() );
        //bus.getOutInterceptors().add( new Step3Interceptor() );
        //bus.getInInterceptors().add( new Step4Interceptor() );

        LOG.info( "Successfully added LoggingFeature interceptor on bus: " + bus );
    }


    public ChannelManagerImpl getChannelManagerImpl()
    {
        return channelManagerImpl;
    }


    public void setChannelManager( final ChannelManagerImpl channelManagerImpl )
    {
        this.channelManagerImpl = channelManagerImpl;
    }
}
