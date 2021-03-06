package eu.rekawek.coffeegb.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsConfiguration {
	private Connection connection;
	private Session session;
	private Queue instructionsQueue;
	private Queue displayQueue;

	public JmsConfiguration(String brokerUrl, String instructionsQueueName, String displayQueueName) throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
		this.connection = connectionFactory.createConnection();

		this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.instructionsQueue = session.createQueue(instructionsQueueName);
		this.displayQueue = session.createQueue(displayQueueName);
	}

	public Connection getConnection() {
		return this.connection;
	}

	public Session getSession() {
		return session;
	}

	public Queue getDisplayQueue() {
		return displayQueue;
	}

	public void setDisplayQueue(Queue displayQueue) {
		this.displayQueue = displayQueue;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Queue getInstructionsQueue() {
		return instructionsQueue;
	}

	public void setInstructionsQueue(Queue instructionsQueue) {
		this.instructionsQueue = instructionsQueue;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Session getActiveMQSession() {
		return this.session;
	}
}
