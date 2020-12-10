package me.mrletsplay.streamdeckandroid.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BitmapUtils {
	
	public static BufferedImage newBufferedImage() {
		return new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
	}

	public static byte[] text(String text, float fontSize, Color textColor, Color backgroundColor) {
		BufferedImage b = newBufferedImage();
		Graphics2D g2d = b.createGraphics();

		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, b.getWidth(), b.getHeight());

		g2d.setColor(textColor);
		g2d.setFont(g2d.getFont().deriveFont(fontSize));

		drawLinesCentered(g2d, 64, 64, text.split("\n"));

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try {
			ImageIO.write(b, "PNG", bOut);
		}catch(IOException e) {}
		
		return bOut.toByteArray();
	}

	public static byte[] solidColor(String text, float fontSize, Color textColor, Color backgroundColor) {
		BufferedImage b = newBufferedImage();
		Graphics2D g2d = b.createGraphics();

		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, b.getWidth(), b.getHeight());

		g2d.setColor(textColor);
		g2d.setFont(g2d.getFont().deriveFont(fontSize));

		drawLinesCentered(g2d, 64, 64, text.split("\n"));

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try {
			ImageIO.write(b, "PNG", bOut);
		}catch(IOException e) {}
		
		return bOut.toByteArray();
	}

	public static void drawLinesCentered(Graphics2D g2d, int centerX, int centerY, String... lines) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int lineHeight = g2d.getFontMetrics().getHeight();
		for(int i = 0; i < lines.length; i++) {
			Rectangle2D rect = g2d.getFontMetrics().getStringBounds(lines[i], g2d);
			g2d.drawString(lines[i], (int) (centerX - rect.getCenterX()), (int) (centerY - lineHeight * (lines.length - 1) / 2 + lineHeight * i - rect.getCenterY()));
		}
	}

}
