package eu.rekawek.coffeegb.controller;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.rekawek.coffeegb.controller.ButtonListener.Button;

public class QueueController implements Controller {
    private static final Logger LOG = LoggerFactory.getLogger(QueueController.class);

    private ButtonListener listener;

    private Map<Integer, Button> mapping;

    public QueueController(Properties properties) {
	EnumMap<Button, Integer> buttonToKey = new EnumMap<>(Button.class);

	buttonToKey.put(Button.LEFT, KeyEvent.VK_LEFT);
	buttonToKey.put(Button.RIGHT, KeyEvent.VK_RIGHT);
	buttonToKey.put(Button.UP, KeyEvent.VK_UP);
	buttonToKey.put(Button.DOWN, KeyEvent.VK_DOWN);
	buttonToKey.put(Button.A, KeyEvent.VK_Z);
	buttonToKey.put(Button.B, KeyEvent.VK_X);
	buttonToKey.put(Button.START, KeyEvent.VK_ENTER);
	buttonToKey.put(Button.SELECT, KeyEvent.VK_BACK_SPACE);

	for (String k : properties.stringPropertyNames()) {
	    String v = properties.getProperty(k);
	    if (k.startsWith("btn_") && v.startsWith("VK_")) {
		try {
		    Button button = Button.valueOf(k.substring(4).toUpperCase());
		    Field field = KeyEvent.class.getField(properties.getProperty(k));
		    if (field.getType() != int.class) {
			continue;
		    }
		    int value = field.getInt(null);
		    buttonToKey.put(button, value);
		} catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
		    LOG.error("Can't parse button configuration", e);
		}
	    }
	}

	mapping = buttonToKey.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
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
