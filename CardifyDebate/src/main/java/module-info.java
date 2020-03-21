module CardifyDebate.main {
    requires javafx.web;
    requires javafx.controls;
    requires javafx.base;
    requires java.desktop;
    requires java.base;
    requires java.sql;
    requires java.net.http;
    requires org.jsoup;
    requires org.apache.commons.codec;
    requires kotlin.stdlib;
    requires zip4j;
    requires commons.exec;
    requires commons.io;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires gson;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports me.matrix4f.cardcutter;
}