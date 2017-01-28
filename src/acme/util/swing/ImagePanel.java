package acme.util.swing;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;
import acme.util.Util;

public class ImagePanel extends JPanel
{
	Image image;

	public ImagePanel()
	{
	}

	public ImagePanel(Image image)
	{
		this.image = image;
	}

	public Image getImage()
	{
		return image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image != null) g.drawImage(image, 0, 0, this);
		Util.log("ImagePanel.paintComponent");
	}
}
