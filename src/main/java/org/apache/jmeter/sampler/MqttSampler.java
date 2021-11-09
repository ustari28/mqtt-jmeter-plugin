package org.apache.jmeter.sampler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Data
public class MqttSampler extends AbstractSampler {
    private String url;
    public static final String SERVER_PROP = "MQTT_SERVER";
    public static final String PORT_PROP = "MQTT_PORT";
    public static final String CA_PROP = "MQTT_CA";
    public static final String CLIENT_CER_PROP = "MQTT_CLIENT_CERT";
    public static final String CLIENT_KEY_PROP = "MQTT_CLIENT_KEY";
    public static final String CLIENT_PASSWORD_PROP = "MQTT_CLIENT_PASSWORD";
    public static final String USERNAME_PROP = "MQTT_USERNAME";
    public static final String PASSWORD_PROP = "MQTT_PASSWORD";
    public static final String SSL_PROP = "MQTT_SSL";
    public static final String MESSAGE_NUMBER_PROP = "MQTT_MESSAGE_NUMBER";
    private String prefix = "mqtt_sample_";
    private String topic = "/jmetter/sample";
    public static final String MESSAGE_PROP = "MQTT_MESSAGE";

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        try {
            JMeterVariables vars = JMeterContextService.getClientSideVariables();
            MqttMessage message = new MqttMessage(getPropertyAsString(MESSAGE_PROP).getBytes(StandardCharsets.UTF_8));
            MqttConnectOptions options = new MqttConnectOptions();
            options.setHttpsHostnameVerificationEnabled(Boolean.FALSE);
            options.setUserName(getPropertyAsString(USERNAME_PROP));
            options.setPassword(getPropertyAsString(PASSWORD_PROP).toCharArray());
            if (getPropertyAsBoolean(SSL_PROP)) {
                url = "ssl://".concat(getPropertyAsString(SERVER_PROP)).concat(":").concat(getPropertyAsString(PORT_PROP));
                String caStr = new String(new FileInputStream(getPropertyAsString(CA_PROP)).readAllBytes(), StandardCharsets.UTF_8);
                String clientCrtStr = new String(new FileInputStream(getPropertyAsString(CLIENT_CER_PROP)).readAllBytes(), StandardCharsets.UTF_8);
                String clientKeyStr = new String(new FileInputStream(getPropertyAsString(CLIENT_KEY_PROP)).readAllBytes(), StandardCharsets.UTF_8);
                options.setSocketFactory(MqttUtils.getSocketFactory(caStr, clientCrtStr, clientKeyStr, getPropertyAsString(CLIENT_PASSWORD_PROP).toCharArray(), "TLSv1.2"));
            } else {
                url = "tcp://".concat(getPropertyAsString(SERVER_PROP)).concat(":").concat(getPropertyAsString(PORT_PROP));
            }
            log.info("URL {}", url);
            options.setServerURIs(new String[]{url});
            MqttClient client = new MqttClient(url, prefix.concat(UUID.randomUUID().toString()));
            result.setSampleLabel("MQTT result ".concat(client.getClientId()));
            result.sampleStart();
            client.connect(options);
            IntStream.range(0, getPropertyAsInt(MESSAGE_NUMBER_PROP, 1))
                    .forEach(i -> {
                        try {
                            client.publish(topic, message);
                        } catch (MqttException e) {
                            log.error("The message {} can't be sent", i);
                        }
                    });
            client.disconnect();
            result.setResponseMessage("Connection successfully");
            result.setResponseOK();
            result.setSuccessful(Boolean.TRUE);
            result.setResponseMessageOK();
        } catch (Exception e) {
            e.printStackTrace();
            result.setResponseCode("500");
            result.setResponseMessage(e.getMessage());
            result.setSuccessful(Boolean.FALSE);
        } finally {
            result.sampleEnd();
        }
        return result;
    }
}
