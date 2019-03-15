package xyz.tincat.host.mqttrecorder.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @ Date       ：Created in 11:38 2019/3/15
 * @ Modified By：
 * @ Version:     0.1
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "mqtt.test", name = "enable", havingValue = "true")
@ConditionalOnBean(MQTTTestConfiguration.class)
public class MQTTTestSender implements CommandLineRunner {

    @Autowired
    private MQTTTestConfiguration.MyGateway gateway;


    public static Thread sendForever(MQTTTestConfiguration.MyGateway gateway) {
       return new Thread(() -> {

           while (true) {
                String msg = UUID.randomUUID().toString();
                gateway.sendToMqtt(msg);
                log.info(msg);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"testSenderForeverThread");
    }

    @Override
    public void run(String... args) throws Exception {
        sendForever(gateway).start();
    }
}