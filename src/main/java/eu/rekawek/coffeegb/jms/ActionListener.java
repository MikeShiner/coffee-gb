package eu.rekawek.coffeegb.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rekawek.coffeegb.controller.ButtonListener.Button;
import eu.rekawek.coffeegb.controller.QueueController;

public class ActionListener implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(ActionListener.class);
    private QueueController control;

    public ActionListener(QueueController control) {
	this.control = control;
    }

    public void onMessage(Message message) {
	TextMessage textMessage = (TextMessage) message;
	try {
	    LOG.info("New Action input detected: {}", textMessage.getText());

	    if (textMessage.getText().equals("A")) {
		this.control.pressBtn(Button.A);
	    } else {
		LOG.error("Unrecognised action input: {}", textMessage.getText());
	    }
	} catch (JMSException e) {
	    e.printStackTrace();
	}
    }
}
