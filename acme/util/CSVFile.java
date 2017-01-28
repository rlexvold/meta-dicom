package acme.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Enumeration;
import java.util.Vector;

public class CSVFile
{
	private final static int	MAXCOL		= 1024;
	private final static int	CR			= 0x0D, LF = 0x0A;
	private Vector				rows		= new Vector();
	private StringBuffer		sb			= new StringBuffer(256);
	private PushbackInputStream	pis;
	private int					row			= 0;
	private int					col			= 0;
	private boolean				endOfLine	= false;
	private boolean				endOfFile	= false;
	private int					delimiter	= 160;

	// load this file (replacing any data)
	public CSVFile(String name, int limiter)
	{
		delimiter = limiter;
		row = col = 0;
		rows.removeAllElements();
		try
		{
			pis = new PushbackInputStream(new FileInputStream(name));
			String r[] = new String[MAXCOL];
			int numCol = 0;
			for (;;)
			{
				readCell();
				String s = sb.toString();
				if (s.length() > 0)
					r[numCol] = s;
				numCol++;
				if (endOfLine || endOfFile)
				{
					if (endOfLine)
						rows.addElement(r);
					else if (endOfFile && numCol(r) != 0)
						rows.addElement(r);
					r = new String[MAXCOL];
					numCol = 0;
				}
				if (endOfFile)
					break;
			}
		}
		catch (Exception e)
		{
			Util.log("CSV load caught " + e);
		}
		finally
		{
			try
			{
				pis.close();
			}
			catch (Exception e)
			{
				;
			}
			pis = null;
		}
	}

	// store this file
	public void store(String name)
	{
		FileOutputStream f = null;
		try
		{
			f = new FileOutputStream(name);
			Enumeration e = rows.elements();
			char testChar = (char) delimiter;
			byte testByte = (byte) delimiter;
			while (e.hasMoreElements())
			{
				String s[] = (String[]) e.nextElement();
				int size = numCol(s);
				for (int i = 0; i < size; i++)
				{
					if (s[i] != null)
					{
						boolean addQuotes = s[i].indexOf(delimiter) != -1;
						if (addQuotes)
							f.write('"');
						for (int j = 0; j < s[i].length(); j++)
							f.write(s[i].charAt(j));
						if (addQuotes)
							f.write('"');
					}
					if (i < size - 1)
						f.write((char) delimiter);
				}
				// f.write((char)CR);
				f.write((char) LF);
			}
		}
		catch (Exception e)
		{
			Util.log("CSV store caught " + e);
		}
		finally
		{
			try
			{
				if (f != null)
					f.close();
			}
			catch (Exception e)
			{
				;
			}
		}
	}

	public void setNumRows(int num)
	{
		int size = rows.size();
		rows.setSize(num);
		for (int i = size; i < num; i++)
			rows.setElementAt(new String[MAXCOL], i);
	}

	public int getRow()
	{
		return row;
	}

	public int getCol()
	{
		return col;
	}

	public int getNumRow()
	{
		return rows.size();
	}

	public int getNumCol(int row)
	{
		if (row < rows.size())
			return numCol((String[]) rows.elementAt(row));
		else
			return 0;
	}

	// set position to use as origin
	public void setPos(int r, int c)
	{
		row = r;
		col = c;
	}

	// set position to cell in column c containg this tag
	public boolean findPos(String tag, int c)
	{
		setPos(0, 0);
		for (int r = 0; r < getNumRow(); r++)
		{
			if (tag.equals(getCell(r, c)))
			{
				row = r;
				col = c;
				return true;
			}
		}
		return false;
	}

	// get the cell at this offset from our position
	public String getCell(int rowOffset, int colOffset)
	{
		int r = row + rowOffset;
		int c = col + colOffset;
		if (r >= rows.size() || c >= MAXCOL)
			return null;
		String[] cols = (String[]) rows.elementAt(r);
		return cols[c] != null ? cols[c] : "";
	}

	// set the cell at this offset from our position
	public void setCell(String s, int rowOffset, int colOffset)
	{
		int r = row + rowOffset;
		int c = col + colOffset;
		if (r >= rows.size())
			setNumRows(r + 1);
		String[] cols = (String[]) rows.elementAt(r);
		cols[c] = s;
	}

	// move to a new column
	public void add()
	{
		col++;
	}

	// add a cell data and move to a new column
	public void add(Object o)
	{
		if (o != null)
			setCell(o.toString(), 0, 0);
		col++;
	}

	// add a list of cell data
	public void add(Object[] oa)
	{
		if (oa != null)
			for (int i = 0; i < oa.length; i++)
				add(oa[i]);
	}

	// add a row
	public void addRow()
	{
		col = 0;
		row++;
	}

	// add a cell and row
	public void addRow(Object o)
	{
		if (o != null)
			setCell(o.toString(), 0, 0);
		col = 0;
		row++;
	}

	// add a cell and row
	public void addRow(Object o1, Object o2)
	{
		add(o1);
		addRow(o2);
	}

	// add a list of cell and a row
	public void addRow(Object[] oa)
	{
		if (oa != null)
			for (int i = 0; i < oa.length; i++)
				add(oa[i]);
		col = 0;
		row++;
	}

	// get the number of non-null data columns
	private int numCol(String[] s)
	{
		if (s == null)
			return 0;
		int numCol = 0;
		for (int i = 0; i < MAXCOL; i++)
		{
			if (s[i] != null && s[i].length() != 0)
				numCol = i + 1;
		}
		return numCol;
	}

	// read the next cell from the file
	int getChar()
	{
		try
		{
			int ch = pis.read();
			if (ch == CR || ch == LF)
			{
				int next = pis.read();
				if (next != -1 && next != LF && next != CR)
					pis.unread(next);
				if (next == -1)
					endOfFile = true;
				endOfLine = true;
				ch = CR;
			}
			else if (ch == -1)
			{
				endOfFile = true;
			}
			return ch;
		}
		catch (IOException e)
		{
			endOfFile = true;
			return -1;
		}
	}

	private void readCell()
	{
		sb.setLength(0);
		if (endOfFile)
			return;
		endOfLine = false;
		int ch = -1;
		try
		{
			sb.setLength(0);
			ch = getChar();
			if (ch == '"')
			{
				ch = pis.read();
				while (!(ch == CR || ch == LF || ch == '"' || ch == -1))
				{
					sb.append((char) ch);
					ch = getChar();
				}
				while (!(ch == CR || ch == LF || ch == delimiter || ch == -1))
				{
					ch = getChar();
				}
			}
			else
			{
				while (!(ch == CR || ch == LF || ch == delimiter || ch == -1))
				{
					sb.append((char) ch);
					ch = getChar();
				}
			}
		}
		catch (Exception e)
		{
			Util.log("readCell caught " + e);
			endOfFile = true;
		}
	}
}