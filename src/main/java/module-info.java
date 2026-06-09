module com.npopov.philharmonic.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires atlantafx.base;
    requires java.net.http;
    requires commons.io;

    opens com.npopov.philharmonic.client to javafx.fxml;
    opens com.npopov.philharmonic.client.ui.auth to javafx.fxml;
    opens com.npopov.philharmonic.client.ui.main to javafx.fxml;
    opens com.npopov.philharmonic.client.ui.tabs to javafx.fxml;
    opens com.npopov.philharmonic.client.ui.components to javafx.fxml;
    opens com.npopov.philharmonic.client.model to com.fasterxml.jackson.databind;

    exports com.npopov.philharmonic.client;
    exports com.npopov.philharmonic.client.api;
    exports com.npopov.philharmonic.client.model;
    exports com.npopov.philharmonic.client.session;
    exports com.npopov.philharmonic.client.util;
    exports com.npopov.philharmonic.client.ui.auth;
    exports com.npopov.philharmonic.client.ui.main;
    exports com.npopov.philharmonic.client.ui.tabs;
    exports com.npopov.philharmonic.client.ui.components;
}
