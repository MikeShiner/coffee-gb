package eu.rekawek.coffeegb.gui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.swing.JPanel;

import org.jline.utils.Log;

import eu.rekawek.coffeegb.gpu.Display;

public class ScreenshotDisplay extends JPanel implements Display, Runnable {

	public static final int DISPLAY_WIDTH = 160;

	public static final int DISPLAY_HEIGHT = 144;

	private final BufferedImage img;

	public static final int[] COLORS = new int[] { 0xe6f8da, 0x99c886, 0x437969, 0x051f2a };

	private final int[] rgb;

	private boolean enabled;

	private int scale;

	private boolean doStop;

	private boolean doRefresh;

	private int i;

	private int frameCounter = 0;

	private MessageProducer displayProducer;
	private Session jmsSession;

	public ScreenshotDisplay(Session jmsSession, MessageProducer displayProducer) {
		super();

		this.displayProducer = displayProducer;
		this.jmsSession = jmsSession;
		img = new BufferedImage(DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_INT_RGB);
		rgb = new int[DISPLAY_WIDTH * DISPLAY_HEIGHT];
		this.scale = scale;
	}

	@Override
	public void putDmgPixel(int color) {
		rgb[i++] = COLORS[color];
		i = i % rgb.length;
	}

	@Override
	public void putColorPixel(int gbcRgb) {
		rgb[i++] = translateGbcRgb(gbcRgb);
	}

	public static int translateGbcRgb(int gbcRgb) {
		int r = (gbcRgb >> 0) & 0x1f;
		int g = (gbcRgb >> 5) & 0x1f;
		int b = (gbcRgb >> 10) & 0x1f;
		int result = (r * 8) << 16;
		result |= (g * 8) << 8;
		result |= (b * 8) << 0;
		return result;
	}

	@Override
	public synchronized void requestRefresh() {
		doRefresh = true;
		notifyAll();
	}

	@Override
	public synchronized void waitForRefresh() {
		while (doRefresh) {
			try {
				wait(1);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	@Override
	public void enableLcd() {
		enabled = true;
	}

	@Override
	public void disableLcd() {
		enabled = false;
	}

	@Override
	public void run() {
		doStop = false;
		doRefresh = false;
		enabled = true;
		while (!doStop) {
			synchronized (this) {
				try {
					wait(1);
				} catch (InterruptedException e) {
					break;
				}
			}

			if (doRefresh) {
				img.setRGB(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, rgb, 0, DISPLAY_WIDTH);
				this.frameCounter++;

				if (this.frameCounter == 10) {
					encodeAndSend();
					this.frameCounter = 0;
				}

				synchronized (this) {
					i = 0;
					doRefresh = false;
					notifyAll();
				}
			}
		}
	}

	private void encodeAndSend() {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ImageIO.write(img, "png", os);
			String code = Base64.getEncoder().encodeToString(os.toByteArray());
			this.displayProducer.send(this.jmsSession.createTextMessage(code));
		} catch (final IOException | JMSException ioe) {
			ioe.printStackTrace();
		}
	}

	public void stop() {
		doStop = true;
	}
}
