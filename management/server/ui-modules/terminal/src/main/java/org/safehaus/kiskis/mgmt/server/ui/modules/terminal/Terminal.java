package org.safehaus.kiskis.mgmt.server.ui.modules.terminal;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public class Terminal implements Module {

    public static final String MODULE_NAME = "Terminal";

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final TextArea commandOutputTxtArea;
        private final AgentManager agentManager;
        private final TaskRunner taskRunner = new TaskRunner();

        public ModuleComponent() {
            agentManager = ServiceLocator.getService(AgentManager.class);

            GridLayout grid = new GridLayout(20, 2);
            grid.setSizeFull();
            grid.setMargin(true);
            grid.setSpacing(true);

            commandOutputTxtArea = new TextArea("Commands output");
            commandOutputTxtArea.setRows(40);
            commandOutputTxtArea.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            commandOutputTxtArea.setImmediate(true);
            commandOutputTxtArea.setWordwrap(false);
            grid.addComponent(commandOutputTxtArea, 0, 0, 19, 0);

            Label programLbl = new Label("Program");
            final TextField programTxtFld = new TextField();
            programTxtFld.setValue("pwd");
            programTxtFld.setWidth(100, Sizeable.UNITS_PERCENTAGE);

            grid.addComponent(programLbl, 0, 1, 1, 1);
            grid.addComponent(programTxtFld, 2, 1, 11, 1);
            Label workDirLbl = new Label("Cwd");
            final TextField workDirTxtFld = new TextField();
            workDirTxtFld.setValue("/");
            grid.addComponent(workDirLbl, 12, 1, 12, 1);
            grid.addComponent(workDirTxtFld, 13, 1, 13, 1);
            Label timeoutLbl = new Label("Timeout");
            final TextField timeoutTxtFld = new TextField();
            timeoutTxtFld.setValue("30");
            grid.addComponent(timeoutLbl, 14, 1, 15, 1);
            grid.addComponent(timeoutTxtFld, 16, 1, 16, 1);
            Button clearBtn = new Button("Clear");
            grid.addComponent(clearBtn, 17, 1, 17, 1);
            final Button sendBtn = new Button("Send");
            grid.addComponent(sendBtn, 18, 1, 18, 1);

            final Label indicator = MgmtApplication.createImage("indicator.gif", 50, 50);
            indicator.setVisible(false);
            grid.addComponent(indicator, 19, 1, 19, 1);

            setCompositionRoot(grid);

            programTxtFld.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {

                @Override
                public void handleAction(Object sender, Object target) {
                    sendBtn.click();
                }
            });

            sendBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = MgmtApplication.getSelectedAgents();
                    if (agents.isEmpty()) {
                        show("Please, select nodes");
                    } else if (programTxtFld.getValue() == null || Util.isStringEmpty(programTxtFld.getValue().toString())) {
                        show("Please, enter command");
                    } else {
                        Task task = new Task();
                        for (Agent agent : agents) {
                            Command cmd = getTemplate();
                            cmd.getRequest().setUuid(agent.getUuid());
                            cmd.getRequest().setProgram(programTxtFld.getValue().toString());
                            if (timeoutTxtFld.getValue() != null && Util.isNumeric(timeoutTxtFld.getValue().toString())) {
                                int timeout = Integer.valueOf(timeoutTxtFld.getValue().toString());
                                if (timeout > 0) {
                                    cmd.getRequest().setTimeout(timeout);
                                }
                            }
                            if (workDirTxtFld.getValue() != null && !Util.isStringEmpty(workDirTxtFld.getValue().toString())) {
                                cmd.getRequest().setWorkingDirectory(workDirTxtFld.getValue().toString());
                            }

                            task.addCommand(cmd);
                        }
                        indicator.setVisible(true);
                        taskRunner.runTask(task, new TaskCallback() {

                            @Override
                            public void onResponse(Task task, Response response) {
                                if (response != null && response.getUuid() != null) {
                                    Agent agent = agentManager.getAgentByUUID(response.getUuid());
                                    String host = agent == null
                                            ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();

                                    StringBuilder out = new StringBuilder(host).append(":\n");
                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut()).append("\n");
                                    }
                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                        out.append(response.getStdErr()).append("\n");
                                    }
                                    if (Util.isFinalResponse(response)) {
                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                            out.append("Exit code: ").append(response.getExitCode()).append("\n\n");
                                        } else {
                                            out.append("Command timed out").append("\n\n");
                                        }
                                    }
                                    addOutput(out.toString());
                                }

                                if (task.isCompleted() && taskRunner.getRemainingTaskCount() == 0) {
                                    indicator.setVisible(false);
                                }
                            }
                        });
                    }
                }
            });

            clearBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    commandOutputTxtArea.setValue("");
                }
            });

        }

        private void addOutput(String output) {
            if (!Util.isStringEmpty(output)) {
                commandOutputTxtArea.setValue(
                        String.format("%s%s",
                                commandOutputTxtArea.getValue(),
                                output));
                commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
            }
        }

        private void show(String notification) {
            getWindow().showNotification(notification);
        }

        public static Command getTemplate() {
            return CommandFactory.createRequest(
                    RequestType.EXECUTE_REQUEST, // type
                    null, //                        !! agent uuid
                    MODULE_NAME, //     source
                    null, //                        !! task uuid 
                    1, //                           !! request sequence number
                    "/", //                         cwd
                    "pwd", //                        program
                    OutputRedirection.RETURN, //    std output redirection 
                    OutputRedirection.RETURN, //    std error redirection
                    null, //                        stdout capture file path
                    null, //                        stderr capture file path
                    "root", //                      runas
                    null, //                        arg
                    null, //                        env vars
                    30); //  
        }

        @Override
        public void onCommand(Response response) {
            taskRunner.feedResponse(response);
        }

        @Override
        public String getName() {
            return Terminal.MODULE_NAME;
        }

    }

    @Override
    public String getName() {
        return Terminal.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

}
