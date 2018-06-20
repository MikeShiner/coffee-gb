package eu.rekawek.coffeegb.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsConfiguration {
    private Connection connection;
    private Session session;

    public JmsConfiguration(String brokerUrl) throws JMSException {
	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
	this.connection = connectionFactory.createConnection();

	this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public Connection getConnection() {
	return this.connection;
    }

    public Session getActiveMQSession() {
	return this.session;
    }
}
