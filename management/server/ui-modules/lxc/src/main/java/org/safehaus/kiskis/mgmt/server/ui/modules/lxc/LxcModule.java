package org.safehaus.kiskis.mgmt.server.ui.modules.lxc;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms.LxcCloneForm;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms.LxcManageForm;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;

public class LxcModule implements Module {

    private static final Logger LOG = Logger.getLogger(LxcModule.class.getName());
    public static final String MODULE_NAME = "LXC";
    private ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements CommandListener {

        private final TabSheet commandsSheet;
        private final LxcCloneForm cloneForm;
        private final LxcManageForm manageForm;

        public ModuleComponent() {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            commandsSheet = new TabSheet();
            commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
            commandsSheet.setSizeFull();

            cloneForm = new LxcCloneForm();
            commandsSheet.addTab(cloneForm, "Clone");
            manageForm = new LxcManageForm();
            commandsSheet.addTab(manageForm, "Manage");

            verticalLayout.addComponent(commandsSheet);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public void onCommand(Response response) {
            cloneForm.outputResponse(response);
            manageForm.outputResponse(response);
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
            LOG.log(Level.INFO, "{0} registering with ModuleService", MODULE_NAME);
            service.registerModule(this);
        }
    }

    public void unsetModuleService(ModuleService service) {
        if (service != null) {
            service.unregisterModule(this);
            LOG.log(Level.INFO, "{0} Unregistering with ModuleService", MODULE_NAME);
        }
    }

}
