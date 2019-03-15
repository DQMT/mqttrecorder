package xyz.tincat.host.mqttrecorder;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class MqttrecorderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MqttrecorderApplication.class).web(WebApplicationType.NONE).run();
    }

}
