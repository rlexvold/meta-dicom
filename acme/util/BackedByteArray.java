package acme.util;

public class BackedByteArray
{
	// static final int BufferSize=4096;
	//
	// byte buffer[];
	// int size=0;
	// int start=0;
	// boolean backed = false;
	// File f = null;
	// RandomAccessFile ras = null;
	//
	// public void back() throws Exception {
	// if (backed)
	// return;
	// f = File.createTempFile("bba", "tmp");
	// ras.setLength(size);
	// if (size != 0) {
	// ras.seek(0);
	// ras.write(buffer, 0, size);
	// }
	// if (buffer != null)
	// buffer = null;
	// backed = true;
	// }
	// public BackedByteArray() {
	// backed = false;
	// byte buffer[] = new byte[BufferSize];
	// }
	// public BackedByteArray(File f) throws Exception {
	// backed = true;
	// this.f = f;
	// ras = new RandomAccessFile(f, "rw");
	// }
	//
	// public void close() throws Exception {
	// if (backed) {
	// ras.close();
	// ras = null;
	// }
	// }
	//
	// public int getSize() throws Exception {
	// return backed ? (int)ras.length() : size;
	// }
	// public void setSize(int newSize) throws Exception {
	// if (!backed && newSize >= BufferSize)
	// back();
	// if (backed)
	// ras.setLength(size);
	// else size = newSize;
	// }
	// public void append(byte[] b, int pos, int len) throws Exception {
	// if (!backed && size+len >= BufferSize)
	// back();
	// if (backed) {
	// ras.seek(size);
	// ras.write(b, pos, len);
	// } else {
	// System.arraycopy(b, pos, buffer, size, len);
	// size += len;
	// }
	// }
	// public int read(int pos) throws Exception {
	// if (backed) {
	// ras.seek(pos);
	// return ras.read();
	// }
	// else return buffer[pos];
	// }
	// public void write(int pos, byte[] b, int bpos, int len) throws Exception
	// {
	// if (!backed && pos+len >= BufferSize)
	// back();
	// if (backed) {
	// ras.seek(pos);
	// ras.write(b, pos, len);
	// } else {
	// System.arraycopy(b, bpos, buffer, pos, len);
	// if (pos+len > size)
	// size = pos+len;
	// }
	// }
	// public int read(int pos, byte[] b, int bpos, int len) throws Exception {
	// if (!backed && pos+len >= BufferSize)
	// back();
	// if (backed) {
	// ras.seek(pos);
	// ras.readFully(b, bpos, len);
	// } else {
	// System.arraycopy(buffer, pos, b, bpos, len);
	// }
	// return len;
	// }
}
