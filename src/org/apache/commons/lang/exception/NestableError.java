/*
 * Copyright 2002-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * The base class of all errors which can contain other exceptions.
 * 
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @see org.apache.commons.lang.exception.NestableException
 * @since 1.0
 * @version $Id: NestableError.java,v 1.1 2008/07/14 19:24:33 rlexvold Exp $
 */
public class NestableError extends Error implements Nestable
{
	/**
	 * The helper instance which contains much of the code which we delegate to.
	 */
	protected NestableDelegate delegate = new NestableDelegate(this);
	/**
	 * Holds the reference to the exception or error that caused this exception
	 * to be thrown.
	 */
	private Throwable cause = null;

	/**
	 * Constructs a new <code>NestableError</code> without specified detail
	 * message.
	 */
	public NestableError()
	{
		super();
	}

	/**
	 * Constructs a new <code>NestableError</code> with specified detail
	 * message.
	 * 
	 * @param msg
	 *            The error message.
	 */
	public NestableError(String msg)
	{
		super(msg);
	}

	/**
	 * Constructs a new <code>NestableError</code> with specified nested
	 * <code>Throwable</code>.
	 * 
	 * @param cause
	 *            the exception or error that caused this exception to be thrown
	 */
	public NestableError(Throwable cause)
	{
		super();
		this.cause = cause;
	}

	/**
	 * Constructs a new <code>NestableError</code> with specified detail
	 * message and nested <code>Throwable</code>.
	 * 
	 * @param msg
	 *            the error message
	 * @param cause
	 *            the exception or error that caused this exception to be thrown
	 */
	public NestableError(String msg, Throwable cause)
	{
		super(msg);
		this.cause = cause;
	}

	public Throwable getCause()
	{
		return cause;
	}

	/**
	 * Returns the detail message string of this throwable. If it was created
	 * with a null message, returns the following: (cause==null ? null :
	 * cause.toString()).
	 * 
	 * @return String message string of the throwable
	 */
	public String getMessage()
	{
		if (super.getMessage() != null)
		{
			return super.getMessage();
		} else if (cause != null)
		{
			return cause.toString();
		} else
		{
			return null;
		}
	}

	public String getMessage(int index)
	{
		if (index == 0)
		{
			return super.getMessage();
		} else
		{
			return delegate.getMessage(index);
		}
	}

	public String[] getMessages()
	{
		return delegate.getMessages();
	}

	public Throwable getThrowable(int index)
	{
		return delegate.getThrowable(index);
	}

	public int getThrowableCount()
	{
		return delegate.getThrowableCount();
	}

	public Throwable[] getThrowables()
	{
		return delegate.getThrowables();
	}

	public int indexOfThrowable(Class type)
	{
		return delegate.indexOfThrowable(type, 0);
	}

	public int indexOfThrowable(Class type, int fromIndex)
	{
		return delegate.indexOfThrowable(type, fromIndex);
	}

	public void printStackTrace()
	{
		delegate.printStackTrace();
	}

	public void printStackTrace(PrintStream out)
	{
		delegate.printStackTrace(out);
	}

	public void printStackTrace(PrintWriter out)
	{
		delegate.printStackTrace(out);
	}

	public final void printPartialStackTrace(PrintWriter out)
	{
		super.printStackTrace(out);
	}
}
