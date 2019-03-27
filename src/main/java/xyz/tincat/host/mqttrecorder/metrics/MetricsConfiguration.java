package xyz.tincat.host.mqttrecorder.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @ Date       ：Created in 17:16 2019/3/27
 * @ Modified By：
 * @ Version:     0.1
 */
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "metrics", name = "enable", havingValue = "true", matchIfMissing = true)
public class MetricsConfiguration {

    @Value("${metrics.interval:10}")
    private int metricsInterval;


    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public ConsoleReporter consoleReporter(MetricRegistry metrics) {
        ConsoleReporter reporter =  ConsoleReporter.forRegistry(metrics).build();
        reporter.start(metricsInterval, TimeUnit.SECONDS);
        return reporter;
    }

    public Counter msgCounter(MetricRegistry metrics){
        return metrics.counter(name(MetricsConfiguration.class, "msg-counter"));
    }

}