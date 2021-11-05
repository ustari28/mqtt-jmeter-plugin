package org.apache.jmeter.sampler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Data
public class MqttSampler extends AbstractSampler {
    private String url;
    private String ca;
    private String clientCrt;
    private String clientKey;
    private char[] clientPasswordKey;
    private String username;
    private char[] password;
    private Boolean ssl;
    private String prefix = "mqtt_sample_";
    private String topic = "/mqtt/sample";
    private String messageText;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            String caStr = new String(new FileInputStream(ca).readAllBytes(), StandardCharsets.UTF_8);
            String clientCrtStr = new String(new FileInputStream(clientCrt).readAllBytes(), StandardCharsets.UTF_8);
            String clientKeyStr = new String(new FileInputStream(clientKey).readAllBytes(), StandardCharsets.UTF_8);

            result.setSampleLabel("mqtt result");
            result.setSuccessful(Boolean.TRUE);
            result.setResponseCode("200");

            MqttMessage message = new MqttMessage(messageText.getBytes(StandardCharsets.UTF_8));
            MqttConnectOptions options = new MqttConnectOptions();
            options.setServerURIs(new String[]{url});
            options.setUserName(username);
            options.setPassword(password);
            if (ssl) {
                options.setSocketFactory(MqttUtils.getSocketFactory(caStr, clientCrtStr, clientKeyStr, clientPasswordKey, "TLSv1.2"));
            }
            MqttClient client = new MqttClient(url, prefix.concat(UUID.randomUUID().toString()));
            client.connect(options);
            client.publish(topic, message);
            client.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            result.setResponseCode("500");
            result.setResponseMessage(e.getMessage());
            result.setSuccessful(Boolean.FALSE);
            //Thread.currentThread().interrupt();
        }
        result.sampleEnd();
        return result;
    }
}
