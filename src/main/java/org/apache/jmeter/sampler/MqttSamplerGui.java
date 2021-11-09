package org.apache.jmeter.sampler;

import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

@Slf4j
public class MqttSamplerGui extends AbstractSamplerGui {

    JCheckBox tcpProtocolCheckBox = new JCheckBox("TCP");
    JCheckBox sslProtocolCheckBox = new JCheckBox("SSL");
    ButtonGroup protocolGroup = new ButtonGroup();
    JTextField serverText = new JTextField();
    JTextField portText = new JTextField();
    JTextField userText = new JTextField();
    JPasswordField passwordText = new JPasswordField();
    JTextField caText = new JTextField();
    JTextField clientCrtText = new JTextField();
    JTextField clientKeyText = new JTextField();
    JPasswordField clientPasswordKeyText = new JPasswordField();
    JTextField messageText = new JTextField();
    JTextField responseCodeText = new JTextField();
    JTextField responseMessageText = new JTextField();
    JTextField responseTimeText = new JTextField();

    public MqttSamplerGui() {
        log.info("Creating GUI");
        responseCodeText.setEnabled(false);
        responseMessageText.setEnabled(false);
        responseTimeText.setEnabled(false);
        messageText.setText("Some message");
        createComponents();
    }

    @Override
    public String getLabelResource() {
        return "MQTT Sampler";
    }

    @Override
    public String getStaticLabel() {
        return getLabelResource();
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        super.configureTestElement(testElement);
        //Here we merge sampler with gui
        testElement.setProperty(MqttSampler.CA_PROP, caText.getText().trim());
        testElement.setProperty(MqttSampler.CLIENT_CER_PROP, clientCrtText.getText().trim());
        testElement.setProperty(MqttSampler.CLIENT_KEY_PROP, clientKeyText.getText().trim());
        testElement.setProperty(MqttSampler.CLIENT_PASSWORD_PROP, String.valueOf(clientPasswordKeyText.getPassword()));
        testElement.setProperty(MqttSampler.SSL_PROP, sslProtocolCheckBox.isSelected());
        testElement.setProperty(MqttSampler.SERVER_PROP, serverText.getText().trim());
        testElement.setProperty(MqttSampler.PORT_PROP, portText.getText().trim());
        testElement.setProperty(MqttSampler.USERNAME_PROP, userText.getText().trim());
        testElement.setProperty(MqttSampler.PASSWORD_PROP, String.valueOf(passwordText.getPassword()));
        testElement.setProperty(MqttSampler.MESSAGE_PROP, messageText.getText().trim());
    }

    private void createComponents() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        //add(makeTitlePanel());
        add(makeMyPanel(), BorderLayout.CENTER);
    }

    private JPanel makeMyPanel() {
        JPanel mqtt = new JPanel();
        JPanel result = new JPanel();
        JPanel config = new JPanel();
        JPanel chkBoxes = new JPanel();
        JPanel auth = new JPanel();
        JPanel certificates = new JPanel();

        JPanel caPanel = new JPanel();
        JPanel certPanel = new JPanel();
        JPanel keyPanel = new JPanel();
        JPanel keyPasswordPanel = new JPanel();
        caPanel.setLayout(new BoxLayout(caPanel, BoxLayout.X_AXIS));
        certPanel.setLayout(new BoxLayout(certPanel, BoxLayout.X_AXIS));
        keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.X_AXIS));
        keyPasswordPanel.setLayout(new BoxLayout(keyPasswordPanel, BoxLayout.X_AXIS));

        config.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("MQTT Config")));
        result.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("MQTT Result")));
        config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
        result.setLayout(new GridLayout(3, 3, 20, 20));
        mqtt.setLayout(new BoxLayout(mqtt, BoxLayout.Y_AXIS));

        config.setMaximumSize(new Dimension(800, 250));
        result.setMaximumSize(new Dimension(800, 200));
        mqtt.setMaximumSize(new Dimension(800, 450));

        chkBoxes.setLayout(new GridLayout(1, 3));
        auth.setLayout(new GridLayout(3, 2));
        certificates.setLayout(new BoxLayout(certificates, BoxLayout.Y_AXIS));

        JLabel protocolLabel = new JLabel("Protocol");
        JLabel serverLabel = new JLabel("Server IP");
        JLabel portLabel = new JLabel("Port");
        JLabel userLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");
        JLabel caLabel = new JLabel("Path CA");
        JLabel clientCrtLabel = new JLabel("Client Crt");
        JLabel clientKeyLabel = new JLabel("Client Key");
        JLabel clientPasswordKeyLabel = new JLabel("Client Password Key");
        JLabel messageLabel = new JLabel("Message");
        JLabel responseCodeLabel = new JLabel("Response code");
        JLabel responseMessageLabel = new JLabel("Response message");
        JLabel responseTime = new JLabel("Response Time");

        caLabel.setMinimumSize(new Dimension(200, 30));

        JFileChooser caFC = new JFileChooser();
        JFileChooser clientFC = new JFileChooser();
        JFileChooser clientKeyFC = new JFileChooser();
        caFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
        clientFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
        clientKeyFC.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JButton caButton = new JButton("...");
        JButton clientButton = new JButton("...");
        JButton clientKeyButton = new JButton("...");

        caButton.setMinimumSize(new Dimension(50, 20));

        caButton.setToolTipText("Select CA chain");
        clientButton.setToolTipText("Select Client Certificate");
        clientKeyButton.setToolTipText("Select Client Private Key");
        caButton.addActionListener(e -> {
            int optionSelected = caFC.showOpenDialog(config);
            if (optionSelected == JFileChooser.APPROVE_OPTION) {
                caText.setText(caFC.getSelectedFile().getAbsolutePath());
            }
        });
        clientButton.addActionListener(e -> {
            int optionSelected = clientFC.showOpenDialog(config);
            if (optionSelected == JFileChooser.APPROVE_OPTION) {
                clientCrtText.setText(clientFC.getSelectedFile().getAbsolutePath());
            }
        });
        clientKeyButton.addActionListener(e -> {
            int optionSelected = clientKeyFC.showOpenDialog(config);
            if (optionSelected == JFileChooser.APPROVE_OPTION) {
                clientKeyText.setText(clientKeyFC.getSelectedFile().getAbsolutePath());
            }
        });

        caFC.addChoosableFileFilter(new FileNameExtensionFilter("Cert Files", "pem", "crt", "cert"));
        clientFC.addChoosableFileFilter(new FileNameExtensionFilter("Cert Files", "pem", "crt", "cert"));
        clientKeyFC.addChoosableFileFilter(new FileNameExtensionFilter("Key Files", "key"));

        tcpProtocolCheckBox.setSelected(Boolean.TRUE);
        protocolGroup.add(tcpProtocolCheckBox);
        protocolGroup.add(sslProtocolCheckBox);

        chkBoxes.add(protocolLabel);
        chkBoxes.add(tcpProtocolCheckBox);
        chkBoxes.add(sslProtocolCheckBox);
        auth.add(serverLabel);
        auth.add(serverText);
        auth.add(portLabel);
        auth.add(portText);
        auth.add(userLabel);
        auth.add(userText);
        auth.add(passwordLabel);
        auth.add(passwordText);
        auth.add(messageLabel);
        auth.add(messageText);
        caPanel.add(caLabel);
        caPanel.add(caText);
        caPanel.add(caButton);


        certPanel.add(clientCrtLabel);
        certPanel.add(clientCrtText);
        certPanel.add(clientButton);
        keyPanel.add(clientKeyLabel);
        keyPanel.add(clientKeyText);
        keyPanel.add(clientKeyButton);
        keyPasswordPanel.add(clientPasswordKeyLabel);
        keyPasswordPanel.add(clientPasswordKeyText);

        certificates.add(caPanel);
        certificates.add(certPanel);
        certificates.add(keyPanel);
        certificates.add(keyPasswordPanel);

        config.add(chkBoxes);
        config.add(auth);
        config.add(certificates);

        result.add(responseCodeLabel);
        result.add(responseCodeText);
        result.add(responseTime);
        result.add(responseTimeText);
        result.add(responseMessageLabel);
        result.add(responseMessageText);

        mqtt.add(config);
        mqtt.add(result);

        return mqtt;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        serverText.setText(element.getPropertyAsString("serverIP", "127.0.0.1"));
        portText.setText(element.getPropertyAsString("port", "1883"));
        userText.setText(element.getPropertyAsString("username", "guest"));
        passwordText.setText(element.getPropertyAsString("password", "guest"));
    }

    @Override
    public TestElement createTestElement() {
        MqttSampler sampler = new MqttSampler();
        modifyTestElement(sampler);
        return sampler;
    }

}
