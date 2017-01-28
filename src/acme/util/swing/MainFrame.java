package acme.util.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class MainFrame extends JFrame
{
	public MainFrame(JComponent mainComp)
	{
		super("MainFrame");
		JScrollPane scrollPane = new JScrollPane(mainComp);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}

	public static JFrame create(JComponent mainComp)
	{
		MainFrame frame = new MainFrame(mainComp);
		frame.pack();
		// acme.acme.XSwing.center(frame);
		frame.setVisible(true);
		return frame;
	}

	public static JFrame create(JComponent mainComp, Dimension dim)
	{
		MainFrame frame = new MainFrame(mainComp);
		mainComp.setPreferredSize(dim);
		frame.pack();
		// acme.acme.XSwing.center(frame);
		frame.setVisible(true);
		return frame;
	}

	public static void main(String[] args)
	{
		// create(new JLabel("Frame"));
		JFrame frame = create(new ImagePanel(BMPLoader.load("C:/Documents and Settings/mb/Desktop/imagesmall.bmp")), new Dimension(256, 256));
		frame.setIconImage(BMPLoader.load("C:/Documents and Settings/mb/Desktop/imagesmall.bmp"));
	}
}
