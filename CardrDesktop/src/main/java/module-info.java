module CardrDesktop.main {
    requires javafx.web;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;
    requires java.desktop;
    requires java.base;
    requires java.sql;
    requires java.net.http;
    requires java.scripting;
    requires org.jsoup;
    requires org.apache.commons.codec;
    requires org.apache.commons.text;
    requires kotlin.stdlib;
    requires commons.exec;
    requires commons.io;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires gson;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires centerdevice.nsmenufx;
    requires jdk.crypto.ec;
    requires org.apache.commons.compress;
    requires jdk.jsobject;

    exports me.sohamgovande.cardr;
    opens me.sohamgovande.cardr.core.ui.windows.markup to javafx.web;
}