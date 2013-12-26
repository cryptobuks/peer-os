package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.MongoWizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.management.MongoManager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoModule implements Module {

    public static final String MODULE_NAME = "MongoDB";

    private static final Logger LOG = Logger.getLogger(MongoModule.class.getName());
    private ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final MongoWizard mongoWizard;
        private final MongoManager mongoManager;

        public ModuleComponent() {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet mongoSheet = new TabSheet();
            mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
            mongoSheet.setSizeFull();
            mongoWizard = new MongoWizard();
            mongoManager = new MongoManager();
            mongoSheet.addTab(mongoWizard.getContent(), "Install");
            mongoSheet.addTab(mongoManager.getContent(), "Manage");

            verticalLayout.addComponent(mongoSheet);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public void onCommand(Response response) {
            if (mongoWizard.getSource().equalsIgnoreCase(response.getSource())) {
                mongoWizard.onResponse(response);
            } else if (mongoManager.getSource().equalsIgnoreCase(response.getSource())) {
                mongoManager.onResponse(response);
            }
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        try {
            component = new ModuleComponent();
            ServiceLocator.getService(CommandManagerInterface.class).addListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in createComponent", e);
        }
        return component;
    }

    @Override
    public void dispose() {
        try {
            ServiceLocator.getService(CommandManagerInterface.class).removeListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in dispose", e);
        }
    }

    public void setModuleService(ModuleService service) {
        if (service != null) {
            LOG.log(Level.INFO, "{0}: registering with ModuleService", MODULE_NAME);
            service.registerModule(this);
        }
    }

    public void unsetModuleService(ModuleService service) {
        if (service != null) {
            service.unregisterModule(this);
            LOG.log(Level.INFO, "{0}: Unregistering with ModuleService", MODULE_NAME);
        }
    }
}
