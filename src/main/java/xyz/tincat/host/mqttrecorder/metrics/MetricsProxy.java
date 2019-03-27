package xyz.tincat.host.mqttrecorder.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @ Date       ：Created in 17:33 2019/3/27
 * @ Modified By：
 * @ Version:     0.1
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "metrics", name = "enable", havingValue = "true", matchIfMissing = true)
public class MetricsProxy {

    @Autowired
    private MetricRegistry metricRegistry;

    @Value("${metrics.meter.name:MQTT MESSAGE}")
    private String meterName;

    public void mark() {
        metricRegistry.meter(meterName).mark();
    }
}