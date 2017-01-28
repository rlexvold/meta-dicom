package acme.util.swing;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordDialog extends JDialog
{
	final boolean allowName = false;
	protected JTextField name;
	protected JPasswordField pass;
	protected JButton okButton;
	protected JButton cancelButton;
	protected JLabel nameLabel;
	protected JLabel passLabel;

	public void setName(String name)
	{
		this.name.setText(name);
	}

	public void setPass(String pass)
	{
		this.pass.setText(pass);
	}

	public void setOKText(String ok)
	{
		this.okButton.setText(ok);
		pack();
	}

	public void setCancelText(String cancel)
	{
		this.cancelButton.setText(cancel);
		pack();
	}

	public void setNameLabel(String name)
	{
		this.nameLabel.setText(name);
		pack();
	}

	public void setPassLabel(String pass)
	{
		this.passLabel.setText(pass);
		pack();
	}

	public String getName()
	{
		return name.getText();
	}

	public String getPass()
	{
		return new String(pass.getPassword());
	}

	public boolean okPressed()
	{
		return pressed_OK;
	}
	private boolean pressed_OK = false;

	public PasswordDialog(Frame parent, String title)
	{
		super(parent, title, true);
		if (title == null)
		{
			setTitle("Enter Password");
		}
		if (parent != null)
		{
			setLocationRelativeTo(parent);
		}
		// super calls dialogInit, so we don't need to do it again.
	}

	public PasswordDialog(Frame parent)
	{
		this(parent, null);
	}

	public PasswordDialog()
	{
		this(null, null);
	}

	protected void dialogInit()
	{
		name = new JTextField("", 20);
		pass = new JPasswordField("", 20);
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		nameLabel = new JLabel("Name ");
		passLabel = new JLabel("Password ");
		super.dialogInit();
		KeyListener keyListener = (new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER))
				{
					pressed_OK = false;
					PasswordDialog.this.setVisible(false);
				}
				if (e.getSource() == okButton && e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					pressed_OK = true;
					PasswordDialog.this.setVisible(false);
				}
			}
		});
		addKeyListener(keyListener);
		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				if (source == name)
				{
					// the user pressed enter in the name field.
					name.transferFocus();
				} else
				{
					// other actions close the dialog.
					pressed_OK = (source == pass || source == okButton);
					PasswordDialog.this.setVisible(false);
				}
			}
		};
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		JPanel pane = new JPanel(gridbag);
		pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		JLabel label;
		c.anchor = GridBagConstraints.EAST;
		if (allowName)
		{
			gridbag.setConstraints(nameLabel, c);
			pane.add(nameLabel);
			gridbag.setConstraints(name, c);
			name.addActionListener(actionListener);
			name.addKeyListener(keyListener);
			pane.add(name);
		}
		c.gridy = 1;
		gridbag.setConstraints(passLabel, c);
		pane.add(passLabel);
		gridbag.setConstraints(pass, c);
		pass.addActionListener(actionListener);
		pass.addKeyListener(keyListener);
		pane.add(pass);
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel panel = new JPanel();
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		panel.add(okButton);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		panel.add(cancelButton);
		gridbag.setConstraints(panel, c);
		pane.add(panel);
		getContentPane().add(pane);
		pack();
		center(this);
	}

	public static void center(Window window)
	{
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// Determine the new location of the window
		int w = window.getSize().width;
		int h = window.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		// Move the window
		window.setLocation(x, y);
	}

	public boolean showDialog()
	{
		setVisible(true);
		return okPressed();
	}

	static public String getPasswordFromDialog()
	{
		String pwd = null;
		PasswordDialog p = new PasswordDialog();
		if (p.showDialog()) pwd = p.getPass();
		p.dispose();
		return pwd;
	}

	static void main(String[] args)
	{
		PasswordDialog p = new PasswordDialog();
		if (args.length > 0)
		{
			p.setName(args[0]);
		}
		if (args.length > 1)
		{
			p.setPass(args[1]);
		}
		if (p.showDialog())
		{
			// System.out.println("Name: " + p.getName());
			System.out.println("Pass: " + p.getPass());
		} else
		{
			System.out.println("User selected cancel");
		}
		p.dispose();
		p = null;
		System.exit(0);
	}
}
