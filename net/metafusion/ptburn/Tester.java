package net.metafusion.ptburn;

import static acme.util.Logger.log;
import static acme.util.Logger.setLoggerImpl;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import acme.util.Util;
import acme.util.Logger.LoggerImpl;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Tester
{
	private JButton button4;
	private JButton button2;
	private JList logList;
	private JList list2;
	private JList list1;
	private JButton button3;
	private JButton button1;
	private JPanel panel;
	static Tester tester = null;
	DefaultListModel dlm;

	Tester()
	{
		tester = this;
		logList.setModel(new DefaultListModel());
		showFrame(panel);
		dlm = (DefaultListModel) logList.getModel();
		setLoggerImpl(new LoggerImpl()
		{
			public void println(String s)
			{
				dlm.addElement(s);
			}
		});
	}

	void showFrame(JPanel p)
	{
		JFrame frame = new JFrame("Test");
		frame.setLocation(50, 50);
		frame.setSize(800, 600);
		frame.setContentPane(p);
		// frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void main(String[] args)
	{
		writeImage(new File("C:/foo.jpg"), drawImage("test it today now asd asdf dsf asdf asdf asdf asdf asdfasdf ", 125, 400));
	}

	static void writeImage(File f, BufferedImage bufferedImage)
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(f);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufferedImage);
			param.setQuality(1.0f, true);
			encoder.encode(bufferedImage, param);
		}
		catch (IOException e)
		{
			log("writeImage " + f.getAbsolutePath(), e);
		}
		finally
		{
			Util.safeClose(fos);
		}
	}

	static public void drawTextInBox(Graphics graphics, Rectangle2D box, String text)
	{
		Point pen = new Point(0, 0); // new Point((int)box.getWidth(),
										// (int)box.getHeight());
		Graphics2D g2d = (Graphics2D) graphics;
		FontRenderContext frc = g2d.getFontRenderContext();
		AttributedString attributedString = new AttributedString(text);
		AttributedCharacterIterator atc = attributedString.getIterator();
		LineBreakMeasurer measurer = new LineBreakMeasurer(atc, frc);
		float wrappingWidth = (float) box.getWidth() - 15;
		while (measurer.getPosition() < text.length())
		{
			TextLayout layout = measurer.nextLayout(wrappingWidth);
			pen.y += (layout.getAscent());
			float dx = layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance());
			layout.draw(g2d, pen.x + dx, pen.y);
			pen.y += layout.getDescent() + layout.getLeading();
		}
	}

	static BufferedImage drawImage(String s, int WIDTH, int HEIGHT)
	{
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = null;
		try
		{
			g2 = img.createGraphics();
			g2.setBackground(Color.white);
			g2.clearRect(0, 0, WIDTH, HEIGHT);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.black);
			Font font = new Font("Arial", Font.PLAIN, 14);
			g2.setFont(font);
			TextLayout tl = new TextLayout(s, font, g2.getFontRenderContext());
			Rectangle2D r = new Rectangle(0, 0, WIDTH, HEIGHT);
			// center the text
			drawTextInBox(g2, r, s);
			// tl.draw(g2,(float)((WIDTH - r.getWidth()) / 2),
			// (float)(((HEIGHT - r.getHeight()) / 2) +
			// r.getHeight()));
		}
		finally
		{
			if (g2 != null) g2.dispose();
		}
		return img;
	}
}