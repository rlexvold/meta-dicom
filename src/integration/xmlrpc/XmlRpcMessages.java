package integration.xmlrpc;

import java.util.ListResourceBundle;

public class XmlRpcMessages extends ListResourceBundle
{
	private static XmlRpcMessages	instance	= null;

	public XmlRpcMessages()
	{
		instance = this;
	}

	public static XmlRpcMessages get()
	{
		if (instance == null)
			instance = new XmlRpcMessages();
		return instance;
	}

	protected Object[][] getContents()
	{
		return new Object[][] {
		// LOCALIZE THE SECOND STRING OF EACH ARRAY (e.g., "OK")
				{ "XmlRpcClient.NetworkError", "A network error occurred." },
				{ "XmlRpcClient.ParseError", "The response could not be parsed." },
				{ "XmlRpcClient.Encoding", "UTF-8" },
				{ "XmlRpcServlet.Encoding", "UTF-8" },
				{ "XmlRpcServlet.ServiceClassNotFound", "The service class cannot not found: " },
				{ "XmlRpcServlet.ServiceClassNotInstantiable", "The service class cannot be instantiated: " },
				{ "XmlRpcServlet.ServiceClassNotAccessible", "The service class is not accessible: " },
				{ "XmlRpcServlet.InvalidServicesFormat", "The services parameter format is invalid: " },
				{ "XmlRpcSerializer.UnsupportedType", "Could not serialize response. Unsupported type: " },
				{ "XmlRpcValue.IllegalDate", "Illegal date encountered:" },
				{ "XmlRpcValue.UnexpectedNestedValue", "Nested value encountered for a non-composite value" },
				{ "XmlRpcParser.ReaderInstantiationError", "Could not instantiate XMLReader parser" },
				{ "XmlRpcParser.ParsingError", "A problem occured during parsing" },
				{ "XmlRpcDispatcher.HandlerNotFound", "The specified handler cannot be found" },
				{ "XmlRpcDispatcher.InvalidMethodNameFormat", "Invalid method name format" },
				{ "XmlRpcDispatcher.InvocationCancelled", "The invocation was cancelled by a processor object" },
				{ "XmlRpcDispatcher.ErrorSendingFault", "Could not send fault back to client due to communication problems" },
				{ "Base64.InvalidDataLength", "Error decoding BASE64 element: Miscalculated data length" },
				{ "ReflectiveInvocationHandler.MethodNotPublished", "The method has not been published or does not exist" },
				{ "ReflectiveInvocationHandler.MethodDontExist", "The method cannot be found. Signature: " },
				{ "IntrospectingSerializer.SerializationError", "Could not serialize property: 	" }
		// END OF MATERIAL TO LOCALIZE
		};
	}
}