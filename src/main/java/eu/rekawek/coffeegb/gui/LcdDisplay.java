package eu.rekawek.coffeegb.gui;

import eu.rekawek.coffeegb.gpu.Display;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LcdDisplay extends JPanel implements Display {

    private final BufferedImage img = new BufferedImage(160 * 2, 144 * 2, BufferedImage.TYPE_INT_RGB);

    private static final int[] COLORS = new int[] {Color.WHITE.getRGB(), Color.LIGHT_GRAY.getRGB(), Color.DARK_GRAY.getRGB(), Color.BLACK.getRGB()};

    @Override
    public void setPixel(int x, int y, int color) {
        img.setRGB(x * 2, y * 2, COLORS[color]);
        img.setRGB(x * 2, y * 2 + 1, COLORS[color]);
        img.setRGB(x * 2 + 1, y * 2, COLORS[color]);
        img.setRGB(x * 2 + 1, y * 2 + 1, COLORS[color]);
    }

    @Override
    public void refresh() {
        validate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.drawImage(img, 0, 0, Color.WHITE, null);
        g2d.dispose();
    }


}