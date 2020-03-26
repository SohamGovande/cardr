module CardifyLauncher.main {
    requires javafx.graphics;
    requires java.sql;
    requires java.scripting;
    requires org.apache.logging.log4j;
    requires kotlin.stdlib;
    requires gson;
    requires org.apache.logging.log4j.core;
    requires commons.exec;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires zip4j;
    requires javafx.controls;
    requires java.desktop;
    exports me.matrix4f.cardcutter.launcher;
}