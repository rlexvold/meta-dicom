package acme.util;

public class NestedException extends RuntimeException
{
	private Exception e;
	private String message;

	public NestedException(String msg, Exception e)
	{
		this.message = msg;
		this.e = e;
	}

	public NestedException(Exception e)
	{
		this.message = "";
		this.e = e;
	}

	public Exception getException()
	{
		return e;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}