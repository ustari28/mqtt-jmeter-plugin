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

    public MqttSamplerGui() {
        log.info("Creating GUI");
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
        StringBuilder sb = new StringBuilder();
        //Here we merge sampler with gui
        MqttSampler sampler = (MqttSampler) testElement;
        sampler.setCa(caText.getText().trim());
        sampler.setClientCrt(clientCrtText.getText().trim());
        sampler.setClientKey(clientKeyText.getText().trim());
        sampler.setClientPasswordKey(clientPasswordKeyText.getPassword());
        sampler.setSsl(Boolean.FALSE);
        if (tcpProtocolCheckBox.isSelected()) {
            sb.append("tcp://");
        } else if (sslProtocolCheckBox.isSelected()) {
            sb.append("ssl://");
            sampler.setSsl(Boolean.TRUE);
        }
        sb.append(serverText.getText().trim()).append(":").append(portText.getText().trim());
        sampler.setUrl(sb.toString());
        sampler.setUsername(userText.getText().trim());
        sampler.setPassword(passwordText.getPassword());
    }

    private void createComponents() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        //add(makeTitlePanel());
        add(makeMyPanel(), BorderLayout.CENTER);
    }

    private JPanel makeMyPanel() {
        JPanel panel = new JPanel();
        JPanel chkBoxes = new JPanel();
        JPanel coms = new JPanel();
        JPanel auth = new JPanel();
        JPanel certificates = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder("MQTT Config"));
        panel.setLayout(new FlowLayout());
        //panel.setMaximumSize(new Dimension(30, 30));

        chkBoxes.setLayout(new GridLayout(1, 3));
        coms.setLayout(new GridLayout(1, 4));
        auth.setLayout(new GridLayout(1, 4));
        certificates.setLayout(new GridLayout(4,3));
        //GroupLayout protocolLayout = new GroupLayout(panel);

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

        JFileChooser caFC = new JFileChooser();
        JFileChooser clientFC = new JFileChooser();
        JFileChooser clientKeyFC = new JFileChooser();
        caFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
        clientFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
        clientKeyFC.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JButton caButton = new JButton("...");
        JButton clientButton = new JButton("...");
        JButton clientKeyButton = new JButton("..");

        caButton.setMaximumSize(new Dimension(10,5));

        caButton.setToolTipText("Select CA chain");
        clientButton.setToolTipText("Select Client Certificate");
        clientKeyButton.setToolTipText("Select Client Private Key");
        caButton.addActionListener(e -> {
            int optionSelected = caFC.showOpenDialog(panel);
            if (optionSelected == JFileChooser.APPROVE_OPTION) {
                caText.setText(caFC.getSelectedFile().getAbsolutePath());
            }
        });
        clientButton.addActionListener(e -> {
            int optionSelected = clientFC.showOpenDialog(panel);
            if (optionSelected == JFileChooser.APPROVE_OPTION) {
                clientCrtText.setText(clientFC.getSelectedFile().getAbsolutePath());
            }
        });
        clientKeyButton.addActionListener(e -> {
            int optionSelected = clientKeyFC.showOpenDialog(panel);
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
        coms.add(serverLabel);
        coms.add(serverText);
        coms.add(portLabel);
        coms.add(portText);
        auth.add(userLabel);
        auth.add(userText);
        auth.add(passwordLabel);
        auth.add(passwordText);
        certificates.add(caLabel);
        certificates.add(caText);
        certificates.add(caButton);
        certificates.add(clientCrtLabel);
        certificates.add(clientCrtText);
        certificates.add(clientButton);
        certificates.add(clientKeyLabel);
        certificates.add(clientKeyText);
        certificates.add(clientKeyButton);
        certificates.add(clientPasswordKeyLabel);
        certificates.add(clientPasswordKeyText);

        panel.add(chkBoxes);
        panel.add(coms);
        panel.add(auth);
        panel.add(certificates);

/**
 protocolLayout.setVerticalGroup(
 protocolLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(protocolLabel)
 .addComponent(tcpProtocolCheckBox)
 .addComponent(sslProtocolCheckBox))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(serverLabel)
 .addComponent(serverText)
 .addComponent(portLabel)
 .addComponent(portText))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(userLabel)
 .addComponent(userText)
 .addComponent(passwordLabel)
 .addComponent(passwordText))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(caLabel)
 .addComponent(caText))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(clientCrtLabel)
 .addComponent(clientCrtText))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(clientKeyLabel)
 .addComponent(clientKeyText))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(clientPasswordKeyLabel)
 .addComponent(clientPasswordKeyText))
 .addGroup(protocolLayout.createSequentialGroup()
 .addComponent(messageLabel)
 .addComponent(messageText)));

 */

        return panel;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        //set values for you GUI
    }

    @Override
    public TestElement createTestElement() {
        MqttSampler sampler = new MqttSampler();
        configureTestElement(sampler);
        return sampler;
    }

}
