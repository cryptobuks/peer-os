package org.safehaus.subutai.core.peer.ui.forms;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerGroup;
import org.safehaus.subutai.core.peer.ui.PeerManagerPortalModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class PeerGroupComponent extends CustomComponent
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerRegisterForm.class.getName() );
    private static final String INFO = "Info";
    private static final String CREATE_GROUP = "Create peer group";

    @AutoGenerated
    private AbsoluteLayout mainLayout;
    @AutoGenerated
    private Table peersTable;
    @AutoGenerated
    private Button showPeerGroupsButton;
    private Button createPeerGroupButton;
    private PeerManagerPortalModule peerManagerPortalModule;


    /**
     * The constructor should first build the main layout, set the composition root and then do any custom
     * initialization. <p/> The constructor will not be automatically regenerated by the visual editor.
     */
    public PeerGroupComponent( final PeerManagerPortalModule peerManagerPortalModule )
    {
        buildMainLayout();
        setCompositionRoot( mainLayout );

        this.peerManagerPortalModule = peerManagerPortalModule;
    }


    @AutoGenerated
    private AbsoluteLayout buildMainLayout()
    {
        // common part: create layout
        mainLayout = new AbsoluteLayout();
        mainLayout.setImmediate( false );
        mainLayout.setWidth( "100%" );
        mainLayout.setHeight( "100%" );

        // top-level component properties
        setWidth( "100.0%" );
        setHeight( "100.0%" );

        // peerRegisterLayout
        final VerticalLayout peerRegisterLayout = buildVerticalLayout();
        mainLayout.addComponent( peerRegisterLayout, "top:20.0px;right:0.0px;bottom:-20.0px;left:0.0px;" );

        return mainLayout;
    }


    @AutoGenerated
    private VerticalLayout buildVerticalLayout()
    {

        // common part: create layout
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.setMargin( true );
        layout.setImmediate( false );

        // showPeerGroupsButton
        showPeerGroupsButton = createShowPeersButton();
        createPeerGroupButton = newPeerGroupButton();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent( showPeerGroupsButton );
        horizontalLayout.addComponent( createPeerGroupButton );


        layout.addComponent( horizontalLayout );

        // peersTable
        peersTable = new Table();
        peersTable.setCaption( "Peers groups" );
        peersTable.setImmediate( false );
        peersTable.setPageLength( 10 );
        peersTable.setSelectable( false );
        peersTable.setEnabled( true );
        peersTable.setSizeFull();
        layout.addComponent( peersTable );

        return layout;
    }


    private Button newPeerGroupButton()
    {
        Button button = new Button( "Create" );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Window window = createWindow( CREATE_GROUP );
                VerticalLayout layout = getPeersLayout( window );
                window.setContent( layout );
                getUI().addWindow( window );
                window.setVisible( true );
            }
        } );
        return button;
    }


    private VerticalLayout getPeersLayout( final Window window )
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setImmediate( false );

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setImmediate( false );

        final Label label = new Label( "Peer group name" );
        final TextField groupName = new TextField();
        final Button checkCharacteristics = new Button( "Check" );
        checkCharacteristics.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Notification.show( "Checking characteristics" );
            }
        } );

        horizontalLayout.addComponent( label );
        horizontalLayout.addComponent( groupName );
        horizontalLayout.addComponent( checkCharacteristics );

        layout.addComponent( horizontalLayout );

        final Table info = createPeersTable();

        Button button = new Button( "Save" );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                String name = groupName.getValue();
                if ( name.length() == 0 )
                {
                    Notification.show( "Please provide group name." );
                    return;
                }
                PeerGroup group = new PeerGroup();
                group.setName( name );
                for ( Object obj : info.getItemIds() )
                {
                    CheckBox checkBox = ( CheckBox ) info.getItem( obj ).getItemProperty( "Select" ).getValue();
                    if ( checkBox.getValue() && obj instanceof Peer )
                    {
                        Peer peer = ( Peer ) obj;
                        group.addPeerUUID( peer.getId() );
                    }
                }
                if ( group.getPeerIds().isEmpty() || group.getPeerIds().size() == 1 )
                {
                    Notification.show( "Please select at least two peers" );
                    return;
                }
                peerManagerPortalModule.getPeerManager().savePeerGroup( group );
                window.close();
                populateData();
            }
        } );

        layout.addComponent( info );
        layout.addComponent( button );

        return layout;
    }


    private Table createPeersTable()
    {
        List<Peer> peers = peerManagerPortalModule.getPeerManager().getPeers();
        Table info = new Table();
        info.setHeight( "500px" );
        info.setWidth( "800px" );
        info.addContainerProperty( "Name", String.class, null );
        info.addContainerProperty( "Select", CheckBox.class, null );
        for ( Peer peer : peers )
        {
            CheckBox checkBox = new CheckBox();
            info.addItem( new Object[] { peer.getName(), checkBox }, peer );
        }
        return info;
    }


    private Button createShowPeersButton()
    {
        showPeerGroupsButton = new Button();
        showPeerGroupsButton.setCaption( "Show peers groups" );
        showPeerGroupsButton.setImmediate( false );
        showPeerGroupsButton.setWidth( "-1px" );
        showPeerGroupsButton.setHeight( "-1px" );

        showPeerGroupsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                populateData();
                peersTable.refreshRowCache();
            }
        } );

        return showPeerGroupsButton;
    }


    private void populateData()
    {
        List<PeerGroup> peerGroups = peerManagerPortalModule.getPeerManager().peersGroups();
        peersTable.removeAllItems();
        peersTable.addContainerProperty( "Name", String.class, null );
        peersTable.addContainerProperty( "Info", Button.class, null );
        peersTable.addContainerProperty( "Actions", Button.class, null );

        for ( final PeerGroup group : peerGroups )
        {
            Button info = new Button( "Info" );

            info.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    Window window = createWindow( "Peer group: " + group.getName() );
                    VerticalLayout layout = infoLayout( group );
                    window.setContent( layout );
                    getUI().addWindow( window );
                    window.setVisible( true );
                }
            } );

            Button delete = new Button( "Delete" );

            delete.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    peerManagerPortalModule.getPeerManager().deletePeerGroup( group );
                    showPeerGroupsButton.click();
                }
            } );
            peersTable.addItem( new Object[] { group.getName(), info, delete }, null );
        }
    }


    private VerticalLayout infoLayout( PeerGroup group )
    {
        VerticalLayout layout = new VerticalLayout();
        final Table info = createInfoTable( group );
        layout.addComponent( info );

        // PeerGroup contains only peer ids, this button lazily gets peer name
        Button btn = new Button( "Get peer names" );
        btn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                for ( Object itemId : info.getItemIds() )
                {
                    Item item = info.getItem( itemId );
                    Object value = item.getItemProperty( "UUID" ).getValue();
                    Peer peer = peerManagerPortalModule.getPeerManager().getPeer( value.toString() );
                    if ( peer != null )
                    {
                        item.getItemProperty( "Name" ).setValue( peer.getName() );
                    }
                }
            }
        } );
        layout.addComponent( btn );

        return layout;
    }


    private Table createInfoTable( final PeerGroup group )
    {
        final Table info = new Table();
        String nameProperty = "Name";
        info.setWidth( "800px" );
        info.addContainerProperty( "UUID", String.class, null );
        info.addContainerProperty( nameProperty, String.class, null );
        info.setColumnExpandRatio( nameProperty, 0.8f );
        for ( UUID uuid : group.getPeerIds() )
        {
            Object[] cells = new Object[]
            {
                uuid.toString(), ""
            };
            info.addItem( cells, uuid );
        }
        return info;
    }


    private Window createWindow( String caption )
    {
        Window window = new Window();
        window.setCaption( caption );
        window.setModal( true );
        window.setClosable( true );
        return window;
    }
}
