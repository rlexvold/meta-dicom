package net.metafusion.ris4d;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.JList;

public class RisAudio extends JFrame
{
	static void log(String s)
	{
		System.out.println(s);
	}
	boolean stopCapture = false;
	boolean stopPlay = false;
	ByteArrayOutputStream byteArrayOutputStream;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;
	boolean playing = false;
	boolean recording = false;

	String status()
	{
		int p = pos / 800 / 2;
		int mp = maxPos / 800 / 2;
		return "" + (p / 10) + "." + (p % 10) + "/" + (mp / 10) + "." + (mp % 10);
	}

	void sync()
	{
		stopCapture = true;
		stopPlay = true;
		while (recording || playing)
		{
			try
			{
				Thread.sleep(25);
			}
			catch (InterruptedException e1)
			{
			}
		}
		stopCapture = false;
		stopPlay = false;
	}

	public static void main(String args[])
	{
		listDevices();
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				new RisAudio();
			}
		});
	}// end main
	int device;
	String[] strings = { "1-9 - set device", "c)lear", "p)lay", "r)ecord", "f)orward", "b)ack", "e)nd-record s)top" };

	void command(char k)
	{
		if (k >= '0' && k <= '9')
		{
			log("set device " + k);
			device = k - '0';
			return;
		}
		if (k == 'c')
		{
			pos = 0;
			maxPos = 0;
			log("clear: " + status());
			return;
		}
		if (k == 'p')
		{
			sync();
			playAudio();
			return;
		}
		if (k == 'r')
		{
			sync();
			captureAudio();
			return;
		}
		if (k == 'f')
		{
			int npos = pos + 4000 * 2;
			if (npos > maxPos) npos = maxPos;
			pos = npos;
			log("fwd: " + status());
			return;
		}
		if (k == 'b')
		{
			int npos = pos - 4000 * 2;
			if (npos < 0) npos = 0;
			pos = npos;
			log("back:" + status());
			return;
		}
		if (k == 'e')
		{
			sync();
			pos = maxPos;
			captureAudio();
			return;
		}
		if (k == 's')
		{
			sync();
			return;
		}
	}

	static void listDevices()
	{
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		System.out.println("Available mixers:");
		for (int cnt = 0; cnt < mixerInfo.length; cnt++)
		{
			System.out.println("" + cnt + ":" + mixerInfo[cnt].getName() + " " + mixerInfo[cnt].getDescription());
		}
	}

	public RisAudio()
	{// constructor
		new Thread(new Runnable()
		{
			public void run()
			{
				for (;;)
				{
					log(status());
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		}).start();
		JList list = new JList();
		list.setModel(new javax.swing.AbstractListModel()
		{
			String[] strings = { "1-9 - set device", "c)lear", "p)lay", "r)ecord", "f)orward", "b)ack", "e)nd-record", "s)top" };

			public int getSize()
			{
				return strings.length;
			}

			public Object getElementAt(int i)
			{
				return strings[i];
			}
		});
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(list); // , BorderLayout.CENTER);
		list.setEnabled(true);
		list.requestFocus();
		list.addKeyListener(new KeyListener()
		{
			public void keyTyped(KeyEvent e)
			{
				// log("keyTyped "+e.getKeyChar());
			}

			public void keyPressed(KeyEvent e)
			{
				log("keyPressed " + e.getKeyChar());
				command(e.getKeyChar());
			}

			public void keyReleased(KeyEvent e)
			{
				// log("keyReleased "+e.getKeyChar());
			}
		});
		//
		//
		// final JButton clearBtn =
		// new JButton("clear");
		// final JButton backBtn =
		// new JButton("<<1");
		// final JButton fwnBtn =
		// new JButton("1>>");
		// final JButton captureBtn =
		// new JButton("Capture");
		// final JButton stopBtn = new JButton("Stop");
		// final JButton playBtn =
		// new JButton("Playback");
		//
		// clearBtn.addActionListener(new ActionListener(){
		// public void actionPerformed( ActionEvent e){
		// pos = 0;
		// maxPos = 0;
		// log("clear: "+status());
		// }});
		// getContentPane().add(clearBtn);
		// backBtn.addActionListener(new ActionListener(){
		// public void actionPerformed( ActionEvent e){
		// int npos = pos-4000*2;
		// if (npos < 0)
		// npos = 0;
		// pos = npos;
		// log("back:"+status());
		// }});
		// getContentPane().add(backBtn);
		// fwnBtn.addActionListener(new ActionListener(){
		// public void actionPerformed( ActionEvent e){
		// int npos = pos+4000*2;
		// if (npos > maxPos)
		// npos = maxPos;
		// pos = npos;
		// log("fwd: "+status());
		// }});
		// getContentPane().add(fwnBtn);
		//
		// captureBtn.addActionListener(
		// new ActionListener(){
		// public void actionPerformed( ActionEvent e){
		// sync();
		// captureAudio();
		// }
		// }
		// );
		// getContentPane().add(captureBtn);
		//
		// stopBtn.addActionListener(
		// new ActionListener(){
		// public void actionPerformed(
		// ActionEvent e){
		// sync();
		//
		// }
		// }
		// );
		// getContentPane().add(stopBtn);
		//
		// playBtn.addActionListener(
		// new ActionListener(){
		// public void actionPerformed(
		// ActionEvent e){
		// sync();
		// playAudio();
		// }
		// }
		// );
		// getContentPane().add(playBtn);
		//
		// getContentPane().setEnabled(true);
		//
		//
		setTitle("Capture/Playback Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 400);
		setVisible(true);
	}
	byte buffer[] = new byte[32 * 1024 * 1024];
	int pos = 0;
	int maxPos = 0;

	// This method captures audio input from a
	// microphone and saves it in a
	// ByteArrayOutputStream object.
	private void captureAudio()
	{
		try
		{
			// Get and display a list of
			// available mixers.
			Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
			System.out.println("Available mixers:");
			for (int cnt = 0; cnt < mixerInfo.length; cnt++)
			{
				System.out.println("" + cnt + ":" + mixerInfo[cnt].getName());
			}// end for loop
			// Get everything set up for capture
			audioFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			// int dev = 4; //3;5 4 (C-Media Wave Device)
			log("trying device: " + device);
			// Select one of the available
			// mixers.
			Mixer mixer = AudioSystem.getMixer(mixerInfo[device]);
			// getMixer(mixerInfo[3]);
			// Get a TargetDataLine on the selected
			// mixer.
			targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
			// Prepare the line for use.
			targetDataLine.open(audioFormat);
			targetDataLine.start();
			recording = true;
			stopCapture = false;
			// Create a thread to capture the microphone
			// data and start it running. It will run
			// until the Stop button is clicked.
			Thread captureThread = new RisAudio.CaptureThread();
			captureThread.start();
		}
		catch (Exception e)
		{
			System.out.println(e);
			System.exit(0);
		}// end catch
	}// end captureAudio method

	// This method plays back the audio data that
	// has been saved in the ByteArrayOutputStream
	private void playAudio()
	{
		try
		{
			// Get everything set up for playback.
			// Get the previously-saved data into a byte
			// array object.
			// Get an input stream on the byte array
			// containing the data
			InputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0 * pos, buffer.length);
			AudioFormat audioFormat = getAudioFormat();
			int frameSize = audioFormat.getFrameSize();
			audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat,
			// audioData.length/audioFormat.getFrameSize()
					(buffer.length - pos) / frameSize);
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();
			// Create a thread to play back the data and
			// start it running. It will run until
			// all the data has been played back.
			Thread playThread = new RisAudio.PlayThread();
			playThread.start();
		}
		catch (Exception e)
		{
			System.out.println(e);
			System.exit(0);
		}// end catch
	}// end playAudio

	private AudioFormat getAudioFormat()
	{
		float sampleRate = 8000.0F;
		// 8000,11025,16000,22050,44100
		int sampleSizeInBits = 16; // 8;
		// 8,16
		int channels = 1;
		// 1,2
		boolean signed = true;
		// true,false
		boolean bigEndian = false;
		// true,false
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}// end getAudioFormat
	// =============================================//
	// Inner class to capture data from microphone
	class CaptureThread extends Thread
	{
		// An arbitrary-size temporary holding buffer
		byte tempBuffer[] = new byte[4000];

		public void run()
		{
			this.setPriority(this.getPriority() + 1);
			byteArrayOutputStream = new ByteArrayOutputStream();
			stopCapture = false;
			try
			{// Loop until stopCapture is set by
				// another thread that services the Stop
				// button.
				while (!stopCapture)
				{
					// Read data from the internal buffer of
					// the data line.
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					if (cnt > 0)
					{
						// Save data in output stream object.
						byteArrayOutputStream.write(tempBuffer, 0, cnt);
						System.arraycopy(tempBuffer, 0, buffer, pos, cnt);
						pos += cnt;
						if (pos > maxPos) maxPos = pos;
					}// end if
				}// end while
				byteArrayOutputStream.close();
				recording = false;
				targetDataLine.drain();
				targetDataLine.close();
			}
			catch (Exception e)
			{
				System.out.println(e);
				System.exit(0);
			}// end catch
		}// end run
	}// end inner class CaptureThread
	// ===================================//
	// Inner class to play back the data
	// that was saved.
	class PlayThread extends Thread
	{
		byte tempBuffer[] = new byte[4000];

		public void run()
		{
			try
			{
				int cnt;
				this.setPriority(this.getPriority() + 1);
				// Keep looping until the input read method
				// returns -1 for empty stream.
				// while((cnt = audioInputStream.read(
				// tempBuffer, 0,
				// tempBuffer.length)) != -1){
				// if(cnt > 0){
				// //Write data to the internal buffer of
				// // the data line where it will be
				// // delivered to the speaker.
				// sourceDataLine.write(tempBuffer,0,cnt);
				// }//end if
				// }//end while
				playing = true;
				stopPlay = false;
				log("avail=" + sourceDataLine.available());
				int avail = sourceDataLine.available();
				while (!stopPlay && pos <= maxPos)
				{
					int p = pos;
					pos += 4000;
					sourceDataLine.write(buffer, p, 4000);
				}
				int availEnd = sourceDataLine.available();
				sourceDataLine.flush();
				pos -= (avail - availEnd);
				if (pos < 0) pos = 0;
				log("flushed=" + (avail - availEnd));
				// Block and wait for internal buffer of the
				// data line to empty.
				sourceDataLine.drain();
				sourceDataLine.close();
				playing = false;
			}
			catch (Exception e)
			{
				System.out.println(e);
				System.exit(0);
			}// end catch
		}// end run
	}// end inner class PlayThread
	// =============================================//
}// end outer class test.ad.AudioCapture02.java
