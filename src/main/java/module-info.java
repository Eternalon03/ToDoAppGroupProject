module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires kotlinx.serialization.json;
    requires java.net.http;
    requires okhttp;
    requires java.desktop;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}
