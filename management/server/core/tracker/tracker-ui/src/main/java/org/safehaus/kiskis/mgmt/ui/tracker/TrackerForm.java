/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.tracker;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.services.MainUISelectedTabChangeListener;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class TrackerForm extends CustomComponent implements MainUISelectedTabChangeListener {

    private static final Logger LOG = Logger.getLogger(TrackerForm.class.getName());

    private final VerticalLayout contentRoot;
    private final Table operationsTable;
    private final TextArea outputTxtArea;
    private final String okIconSource = "icons/16/ok.png";
    private final String errorIconSource = "icons/16/cancel.png";
    private final String loadIconSource = "../base/common/img/loading-indicator.gif";
    private final PopupDateField fromDate, toDate;
    private final ComboBox sourcesCombo;
    private Date fromDateValue, toDateValue;
    private volatile UUID trackID;
    private volatile boolean track = false;
    private List<ProductOperationView> currentOperations = new ArrayList<ProductOperationView>();
    private String source;
    private int limit = 10;

    public TrackerForm() {
        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setMargin(true);

        VerticalLayout content = new VerticalLayout();
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setSpacing(true);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);

        sourcesCombo = new ComboBox("Source");
        sourcesCombo.setMultiSelect(false);
        sourcesCombo.setImmediate(true);
        sourcesCombo.setTextInputAllowed(false);
        sourcesCombo.setNullSelectionAllowed(false);
        sourcesCombo.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                source = (String) event.getProperty().getValue();
                trackID = null;
                outputTxtArea.setValue("");
            }
        });
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            fromDateValue = df.parse(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + String.format("%02d", (cal.get(Calendar.MONTH) + 1)) + cal.get(Calendar.YEAR) + " 00:00:00");
            toDateValue = df.parse(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + String.format("%02d", (cal.get(Calendar.MONTH) + 1)) + cal.get(Calendar.YEAR) + " 23:59:59");
        } catch (java.text.ParseException ex) {
            Logger.getLogger(TrackerForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        fromDate = new PopupDateField("From", fromDateValue);
        toDate = new PopupDateField("To", toDateValue);
        fromDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
        toDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fromDate.setResolution(PopupDateField.RESOLUTION_SEC);
        toDate.setResolution(PopupDateField.RESOLUTION_SEC);
        fromDate.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof Date) {
                    fromDateValue = (Date) event.getProperty().getValue();
                }
            }
        });
        toDate.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof Date) {
                    toDateValue = (Date) event.getProperty().getValue();
                }
            }
        });

        ComboBox limitCombo = new ComboBox("Show last", Arrays.asList(10, 50, 100));
        limitCombo.setMultiSelect(false);
        limitCombo.setImmediate(true);
        limitCombo.setTextInputAllowed(false);
        limitCombo.setNullSelectionAllowed(false);
        limitCombo.setValue(limit);
        limitCombo.addListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                limit = (Integer) event.getProperty().getValue();
            }
        });

        filterLayout.addComponent(sourcesCombo);
        filterLayout.addComponent(fromDate);
        filterLayout.addComponent(toDate);
        filterLayout.addComponent(limitCombo);

        operationsTable = createTableTemplate("Operations", 250);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows(20);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(filterLayout);
        content.addComponent(operationsTable);
        content.addComponent(outputTxtArea);
        content.setComponentAlignment(operationsTable, Alignment.TOP_CENTER);
        content.setComponentAlignment(outputTxtArea, Alignment.TOP_CENTER);

        setCompositionRoot(contentRoot);

    }

    private void startTracking() {
        if (!track) {
            track = true;

            TrackerUI.getExecutor().execute(new Runnable() {

                public void run() {
                    while (track) {
                        try {
                            populateOperations();
                            populateLogs();

                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Error in tracker", e);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            break;
                        }

                    }
                }
            });
        }
    }

    private void stopTracking() {
        track = false;
    }

    private void populateLogs() {
        if (trackID != null && !Util.isStringEmpty(source)) {
            ProductOperationView po = TrackerUI.getTracker().getProductOperation(source, trackID);
            if (po != null) {
                setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                if (po.getState() != ProductOperationState.RUNNING) {
                    trackID = null;
                }
            } else {
                setOutput("Product operation not found. Check logs");
            }
        }
    }

    private void populateOperations() {
        if (!Util.isStringEmpty(source)) {
            List<ProductOperationView> operations = TrackerUI.getTracker().getProductOperations(
                    source, fromDateValue, toDateValue, limit);
            if (operations.isEmpty()) {
                trackID = null;
                outputTxtArea.setValue("");
            }
            IndexedContainer container = (IndexedContainer) operationsTable.getContainerDataSource();
            currentOperations.removeAll(operations);

            for (ProductOperationView po : currentOperations) {
                container.removeItem(po.getId());
            }
            boolean sortNeeded = false;
            for (final ProductOperationView po : operations) {
                Embedded progressIcon;
                if (po.getState() == ProductOperationState.RUNNING) {
                    progressIcon = new Embedded("", new ThemeResource(loadIconSource));
                } else if (po.getState() == ProductOperationState.FAILED) {
                    progressIcon = new Embedded("", new ThemeResource(errorIconSource));
                } else {
                    progressIcon = new Embedded("", new ThemeResource(okIconSource));
                }

                Item item = container.getItem(po.getId());
                if (item == null) {
                    final Button trackLogsBtn = new Button("View logs");
                    trackLogsBtn.addListener(new Button.ClickListener() {

                        public void buttonClick(Button.ClickEvent event) {
                            trackID = po.getId();
                        }
                    });

                    item = container.addItem(po.getId());
                    item.getItemProperty("Date").setValue(po.getCreateDate());
                    item.getItemProperty("Operation").setValue(po.getDescription());
                    item.getItemProperty("Check").setValue(trackLogsBtn);
                    item.getItemProperty("Status").setValue(progressIcon);

                    sortNeeded = true;
                } else {
                    if (!((Embedded) item.getItemProperty("Status").getValue()).getSource().equals(progressIcon.getSource())) {
                        item.getItemProperty("Status").setValue(progressIcon);
                    }
                }

            }

            if (sortNeeded) {
                Object[] properties = {"Date"};
                boolean[] ordering = {false};
                operationsTable.sort(properties, ordering);
            }

            currentOperations = operations;
        }
    }

    private Table createTableTemplate(String caption, int height) {
        Table table = new Table(caption);
        table.setContainerDataSource(new IndexedContainer());
        table.addContainerProperty("Date", Date.class, null);
        table.addContainerProperty("Operation", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(height, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private void setOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

    void refreshSources() {
        String oldSource = source;
        sourcesCombo.removeAllItems();
        List<String> sources = TrackerUI.getTracker().getProductOperationSources();
        for (String src : sources) {
            sourcesCombo.addItem(src);
        }
        if (!Util.isStringEmpty(oldSource)) {
            sourcesCombo.setValue(oldSource);
        } else if (!sources.isEmpty()) {
            sourcesCombo.setValue(sources.iterator().next());
        }
    }

    public void selectedTabChanged(TabSheet.Tab selectedTab) {
        if (TrackerUI.MODULE_NAME.equals(selectedTab.getCaption())) {
            refreshSources();
            startTracking();
        } else {
            stopTracking();
        }
    }

    @Override
    public void detach() {
        super.detach();
        stopTracking();
    }

}
