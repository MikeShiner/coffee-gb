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

	private Button parseButton(String button) {
		Button btn = null;
		switch (button) {
		case "A":
			btn = Button.A;
			break;
		case "B":
			btn = Button.B;
			break;
		case "UP":
			btn = Button.UP;
			break;
		case "DOWN":
			btn = Button.DOWN;
			break;
		case "LEFT":
			btn = Button.LEFT;
			break;
		case "RIGHT":
			btn = Button.RIGHT;
			break;
		case "START":
			btn = Button.START;
			break;
		case "SELECT":
			btn = Button.SELECT;
			break;
		}
		return btn;
	}

	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		try {
			Button btn = this.parseButton(textMessage.getText());
			if (btn != null) {
				this.control.pressBtn(btn);
			} else {
				LOG.error("Unrecognised Button Command: {}", textMessage.getText());
			}
		} catch (JMSException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
