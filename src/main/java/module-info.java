module com.example.foodordersystem {
    // Required JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;


    // Third-party libraries
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.jfoenix;

    // Core Java modules
    requires java.desktop;
    requires java.sql;
    requires annotations;

    // Open packages for JavaFX and FXML reflection
    opens com.example.foodordersystem to javafx.fxml, javafx.graphics;
    opens com.example.foodordersystem.controller to javafx.fxml;
    exports com.example.foodordersystem to javafx.graphics;

    // Exported packages
    exports com.example.foodordersystem.controller;
    exports com.example.foodordersystem.model;
    exports com.example.foodordersystem.service;
    exports com.example.foodordersystem.repository;
    exports com.example.foodordersystem.database;

}