/*
 * $Id: PdfUtils.java,v 1.1 2008/07/14 19:24:36 rlexvold Exp $
 * $Name:  $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * This class demonstrates copying a PDF file using iText.
 * @author Mark Thompson
 */
package net.metafusion.pdfutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PRAcroForm;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.SimpleBookmark;

import acme.util.Util;
import net.metafusion.util.GlobalProperties;
import net.metafusion.util.OsUtils;

public class PdfUtils
{
	public static void concatenate(String inFiles[], String outFile) throws Exception
	{
		try
		{
			int pageOffset = 0;
			ArrayList master = new ArrayList();
			int f = 0;
			Document document = null;
			PdfCopy writer = null;
			while (f < inFiles.length)
			{
				// we create a reader for a certain document
				PdfReader reader = new PdfReader(inFiles[f]);
				reader.consolidateNamedDestinations();
				// we retrieve the total number of pages
				int n = reader.getNumberOfPages();
				List bookmarks = SimpleBookmark.getBookmark(reader);
				if (bookmarks != null)
				{
					if (pageOffset != 0)
						SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
					master.addAll(bookmarks);
				}
				pageOffset += n;
				if (f == 0)
				{
					// step 1: creation of a document-object
					document = new Document(reader.getPageSizeWithRotation(1));
					// step 2: we create a writer that listens to the
					// document
					writer = new PdfCopy(document, new FileOutputStream(outFile));
					// step 3: we open the document
					document.open();
				}
				// step 4: we add content
				PdfImportedPage page;
				for (int i = 0; i < n;)
				{
					++i;
					page = writer.getImportedPage(reader, i);
					writer.addPage(page);
				}
				PRAcroForm form = reader.getAcroForm();
				if (form != null)
					// writer.copyAcroForm(reader);
					f++;
			}
			if (!master.isEmpty())
				writer.setOutlines(master);
			// step 5: we close the document
			document.close();
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public static void startPdfService(Integer servicePort) throws Exception
	{
		String cmd = GlobalProperties.get().getProperty("openOffice/cmd");
		if (cmd == null || cmd.length() == 0)
		{
			if (OsUtils.isMac())
			{
				cmd = "/soffice";
			}
			else
			{
				cmd = "c:\\Program Files\\OpenOffice.org 2.4\\program\\soffice.exe";
			}
		}
		cmd += " -headless -accept=\"socket,host=localhost,port=" + servicePort + ";urp;\" -nofirststartwizard";
		try
		{
			Process p = Runtime.getRuntime().exec(cmd);
			Util.sleep(2000);
			// p.waitFor();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Boolean pingServer(String host, Integer port)
	{
		String timeStamp = "";
		Socket socket = null;
		BufferedReader br = null;
		try
		{
			socket = new Socket(host, port);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			timeStamp = br.readLine();
			socket.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static String ConvertFileToPdf(String inFile) throws Exception
	{
		String[] splitList = inFile.split("\\.");
		if (splitList.length == 0)
		{
			Exception e = new Exception("No file extension found");
			throw e;
		}
		String extension = splitList[splitList.length - 1];
		if (extension.equalsIgnoreCase("rtf") || extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("ppt"))
		{
			String outFile = "";
			for (int i = 0; i < splitList.length - 1; i++)
			{
				outFile += splitList[i] + ".";
			}
			outFile += "pdf";
			convertOfficeDocToPdf(inFile, outFile);
			return outFile;
		}
		return inFile;
	}

	public static void convertOfficeDocToPdf(String inFile, String outFile) throws Exception
	{
		OpenOfficeConnection connection = null;
		String servicePort = GlobalProperties.get().getProperty("openOffice/port");
		if (servicePort == null || servicePort.length() < 3)
			servicePort = "8100";
		Integer port = new Integer(servicePort);
		if (!pingServer("localhost", port))
		{
			startPdfService(port);
		}
		connection = new SocketOpenOfficeConnection(port);
		connection.connect();
		// convert
		DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
		converter.convert(new File(inFile), new File(outFile));
		// close the connection
		if (connection != null)
			connection.disconnect();
	}
}
