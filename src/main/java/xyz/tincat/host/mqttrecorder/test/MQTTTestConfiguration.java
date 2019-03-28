package xyz.tincat.host.mqttrecorder.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ Date       ：Created in 11:31 2019/3/15
 * @ Modified By：
 * @ Version:     0.1
 */
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "mqtt.test", name = "enable", havingValue = "true")
public class MQTTTestConfiguration {

    @Value("${mqtt.username}")
    private String userName;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.host}")
    private String serverURI;
    @Value("${mqtt.clientId}")
    private String clientId;
    @Value("${mqtt.test.topic}")
    private String topic;


    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory mqttPahoClientFactory) {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId + new Date().getTime(), mqttPahoClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(topic);
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ExecutorChannel(
                new ThreadPoolExecutor(cores + 1,
                        (cores + 1),
                        60,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>()));
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MyGateway {
        void sendToMqtt(String data);
//        void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
    }

}