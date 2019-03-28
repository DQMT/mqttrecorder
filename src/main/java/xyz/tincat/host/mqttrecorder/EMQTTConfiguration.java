package xyz.tincat.host.mqttrecorder;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import xyz.tincat.host.mqttrecorder.metrics.MetricsProxy;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ Description：
 * @ Author     ：songhangbo
 * @ Date       ：Created in 20:38 2019/3/7
 * @ Modified By：
 * @ Version:     0.1
 */
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "mqtt", name = "enable", havingValue = "true", matchIfMissing = true)
public class EMQTTConfiguration {
    @Value("${mqtt.username}")
    private String userName;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.host}")
    private String[] serverURIs;
    @Value("${mqtt.clientId}")
    private String clientId;
    @Value("${mqtt.topics}")
    private String[] topics;
    @Value("${mqtt.maxflight:10000}")
    private int maxflight;
    @Value("${mqtt.qos:1}")
    private int qos;
    @Autowired
    private MetricsProxy metricsProxy;

    @Bean
    public MessageChannel mqttInputChannel() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ExecutorChannel(
                new ThreadPoolExecutor(cores + 1,
                        (cores + 1),
                        60,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>()));
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(serverURIs);
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
        options.setMaxInflight(maxflight);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(3);
        options.setCleanSession(true);
        options.setKeepAliveInterval(5);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageProducer inbound(MqttPahoClientFactory mqttPahoClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + new Date().getTime(), mqttPahoClientFactory,
                        topics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(MqttPahoClientFactory mqttPahoClientFactory) {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                log.info((String) message.getPayload());
                metricsProxy.mark();
            }
        };
    }

}