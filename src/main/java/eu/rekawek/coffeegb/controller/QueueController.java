package eu.rekawek.coffeegb.controller;

import java.util.Properties;

import eu.rekawek.coffeegb.controller.ButtonListener.Button;

public class QueueController implements Controller {

    private ButtonListener listener;

    public QueueController(Properties properties) {
    }

    @Override
    public void setButtonListener(ButtonListener listener) {
	this.listener = listener;
    }

    public void pressBtn(Button btn) {
	listener.onButtonPress(btn);
	listener.onButtonRelease(btn);
    }
}
