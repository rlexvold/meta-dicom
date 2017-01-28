package acme.util;

import java.io.Serializable;

public class Buffer implements Serializable
{
	boolean be = false;
	byte[] b;
	int start;
	int p;
	int last;

	public Buffer(byte[] b, int start, int len, int pos)
	{
		this.b = b;
		this.start = start;
		this.last = start + len;
		this.p = pos;
	}

	public Buffer(byte[] b, int start, int len)
	{
		this(b, start, len, start);
	}

	public Buffer(byte[] b)
	{
		this(b, 0, b.length, 0);
	}

	public Buffer(int maxSize)
	{
		this(new byte[maxSize], 0, 0, 0);
	}

	public Buffer(Buffer buffer, int len)
	{
		this(buffer.getBuffer(), buffer.getPos(), len);
		this.be = buffer.be;
	}

	public Buffer(Buffer buffer)
	{
		this(buffer.getBuffer(), buffer.getStart(), buffer.getLength());
		this.be = buffer.be;
	}

	public Buffer(Buffer buffer, byte[] bytes, int start, int len)
	{
		b = new byte[buffer.getLength() + len];
		System.arraycopy(b, 0, buffer.b, buffer.start, buffer.getLength());
		System.arraycopy(b, buffer.getLength(), bytes, start, len);
		p = 0;
		start = 0;
		last = start + buffer.getLength() + len;
		this.be = buffer.be;
	}

	public void growTo(int growSize)
	{
		int size = last - start;
		if (growSize <= size - 64) // fudge room
			return;
		int newSize = growSize + 1024 * 4;// extra room
		byte newB[] = new byte[newSize];
		System.arraycopy(newB, 0, b, start, getLength());
		b = newB;
		last -= start;
		start = 0;
		p -= start;
		b = newB;
	}

	public Buffer(Buffer buffer, byte[] bytes)
	{
		this(buffer, bytes, 0, bytes.length);
		this.be = buffer.be;
	}

	public void dump()
	{
		Util.dumpBytes(b, start, getLength());
	}

	public void dump(int max)
	{
		Util.dumpBytes(b, start, Math.min(getLength(), max));
	}

	public void dump(int start, int max)
	{
		Util.dumpBytes(b, start, MathUtil.pin(last - start, 0, max));
	}

	public String toString()
	{
		return Util.dumpBytesToString(b, start, getLength());
	}

	public String toString(int max)
	{
		return Util.dumpBytesToString(b, start, MathUtil.pin(last - start, 0, max));
	}

	public void clear()
	{
		p = start;
	}

	public boolean isBigEndian()
	{
		return be;
	}

	public void setBigEndian(boolean on)
	{
		be = on;
	}

	public boolean atEnd()
	{
		return p >= last;
	}

	public void skip(int d)
	{
		p += d;
	}

	public byte[] getBuffer()
	{
		return b;
	}

	public int getStart()
	{
		return start;
	}

	public int getPos()
	{
		return p;
	}

	public void setPos(int pos)
	{
		p = pos;
	}

	public void setLen(int len)
	{
		last = start + len;
	}

	// public int getSize() {
	// return maxLen!=0?(p-start):maxLen;
	// }
	public int getLength()
	{
		return last - start;
	}

	public int getBytesLeft()
	{
		return last - p;
	}

	final public void fixupLast()
	{
		if (p > last) last = p;
	}

	public void addByte(byte b)
	{
		this.b[p++] = b;
		fixupLast();
	}

	public int getByte()
	{
		return ((int) b[p++]) & 0xFF;
	}

	public void addShort(short s)
	{
		Util.encodeShort(be, s, b, p);
		p += 2;
		fixupLast();
	}

	public void addZero(int len)
	{
		while (len-- > 0)
			this.b[p++] = 0;
		fixupLast();
	}

	public void putShort(int pos, short s)
	{
		Util.encodeShort(be, s, b, pos);
		fixupLast();
	}

	public int getShort()
	{
		int s = Util.decodeShort(be, b, p);
		p += 2;
		return s;
	}

	public int getShort(int p)
	{
		int s = Util.decodeShort(be, b, p);
		return s;
	}

	public void addInt(int i)
	{
		Util.encodeInt(be, i, b, p);
		p += 4;
		fixupLast();
	}

	public void putInt(int pos, int i)
	{
		Util.encodeInt(be, i, b, pos);
	}

	public int getInt()
	{
		int i = Util.decodeInt(be, b, p);
		p += 4;
		return i;
	}

	public int getInt(int p)
	{
		int i = Util.decodeInt(be, b, p);
		return i;
	}

	public int addString(String s)
	{
		byte[] sb = s.getBytes();
		System.arraycopy(sb, 0, b, p, sb.length);
		p += sb.length;
		// if ((sb.length&1)==1)
		// addByte((byte)0x20);
		fixupLast();
		return sb.length;
	}

	public int addString(String s, int padTo)
	{
		byte[] sb = s.getBytes();
		int len = sb.length;
		if (len > padTo) len = padTo;
		System.arraycopy(sb, 0, b, p, len);
		p += len;
		// if ((sb.length&1)==1)
		// addByte((byte)0x20);
		fixupLast();
		while (len < padTo)
		{
			addByte((byte) ' ');
			len++;
		}
		return len;
	}

	public int addShortString(String s)
	{
		byte[] sb = s.getBytes();
		addShort((short) sb.length);
		System.arraycopy(sb, 0, b, p, sb.length);
		p += sb.length;
		// if ((sb.length&1)==1)
		// addByte((byte)0x20);
		fixupLast();
		return sb.length;
	}

	public String getString(int len)
	{
		String s = new String(b, p, len);
		p += len;
		// if ((len&1)==1)
		// p++;
		return s;
	}

	public String getString(int len, int p)
	{
		String s = new String(b, p, len);
		// if ((len&1)==1)
		// p++;
		return s;
	}

	public String getShortString()
	{
		int len = getShort();
		String s = new String(b, p, len);
		p += len;
		// if ((len&1)==1)
		// p++;
		return s;
	}

	public int addBuffer(Buffer b)
	{
		return addBytes(b.getBuffer(), b.getShort(), b.getLength());
	}

	public int addBytes(byte[] bs, int start, int len)
	{
		System.arraycopy(bs, start, b, p, len);
		p += len;
		fixupLast();
		return len;
	}

	public int addBytes(byte[] bs)
	{
		System.arraycopy(bs, 0, b, p, bs.length);
		p += bs.length;
		fixupLast();
		return bs.length;
	}

	public long getLong(int p)
	{
		long l = 0;
		for (int i = 0; i < 8; i++)
		{
			if (be)
				l = l | (((long) b[p + i]) << (7 - i) * 8);
			else l = l | (((long) b[p + i]) << i * 8);
		}
		return l;
	}

	public float getFloat(int p)
	{
		int i = getInt(p);
		return Float.intBitsToFloat(i); // endianness???
	}

	public double getDouble(int p)
	{
		long l = getLong(p);
		return Double.longBitsToDouble(l);
	}
}
