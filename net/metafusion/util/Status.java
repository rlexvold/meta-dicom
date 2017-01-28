package net.metafusion.util;

import java.util.HashMap;
import acme.util.Buffer;

public class Status
{
	static HashMap statusMap = new HashMap();

	synchronized static public Status get(int code)
	{
		Status status = (Status) statusMap.get(new Integer(code));
		if (status == null) status = new Status(code);
		return status;
	}

	static public Status get(Buffer b)
	{
		return get(b.getShort());
	}

	public Status(int code)
	{
		this.code = code;
		statusMap.put(new Integer(code), this);
	}
	int code;
	String name = "";
	String key = "UKNOWN";
	String type = "";

	public int getCode()
	{
		return code;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		return key + "[" + code + "]";
	}
	// Success: Success
	public static final Status Success = new Status(0x0000);
	// Cancel: Cancel
	public static final Status Cancel = new Status(0xFE00);
	// Pending: Pending
	public static final Status Pending = new Status(0xFF00);
	// Warning: Attribute list error
	public static final Status AttributeListError = new Status(0x0107);
	// Warning: Attribute Value Out of Range
	public static final Status AttributeValueOutOfRange = new Status(0x0116);
	// Failure: Refused: SOP class not supported
	public static final Status SOPClassNotSupported = new Status(0x0122);
	// Failure: Class-instance conflict
	public static final Status ClassInstanceConflict = new Status(0x0119);
	// Failure: Duplicate SOP instance
	public static final Status DuplicateSOPInstance = new Status(0x0111);
	// Failure: Duplicate invocation
	public static final Status DuplicateInvocation = new Status(0x0210);
	// Failure: Invalid argument value
	public static final Status InvalidArgumentValue = new Status(0x0115);
	// Failure: Invalid attribute value
	public static final Status InvalidAttributeValue = new Status(0x0106);
	// Failure: Invalid object instance
	public static final Status InvalidObjectInstance = new Status(0x0117);
	// Failure: Missing attribute
	public static final Status MissingAttribute = new Status(0x0120);
	// Failure: Missing attribute value
	public static final Status MissingAttributeValue = new Status(0x0121);
	// Failure: Mistyped argument
	public static final Status MistypedArgument = new Status(0x0212);
	// Failure: No such argument
	public static final Status NoSuchArgument = new Status(0x0114);
	// Failure: No such event type
	public static final Status NoSuchEventType = new Status(0x0113);
	// Failure: No Such object instance
	public static final Status NoSuchObjectInstance = new Status(0x0112);
	// Failure: No Such SOP class
	public static final Status NoSuchSOPClass = new Status(0x0118);
	// Failure: Processing failure
	public static final Status ProcessingFailure = new Status(0x0110);
	// Failure: Resource limitation
	public static final Status ResourceLimitation = new Status(0x0213);
	// Failure: Unrecognized operation
	public static final Status UnrecognizedOperation = new Status(0x0211);
	// Failure: No such action type
	public static final Status NoSuchActionType = new Status(0x0123);
	// Storage Failure: Out of Resources
	public static final Status StorageOutOfResources = new Status(0xA700);
	// Storage Failure: Message Set does not match SOP Class (Error)
	public static final Status DataSetDoesNotMatchSOPClassError = new Status(0xA900);
	// Storage Failure: Cannot understand
	public static final Status CannotUnderstand = new Status(0xC000);
	// Storage Warning: Coercion of Message Elements
	public static final Status CoercionOfDataElements = new Status(0xB000);
	// Storage Warning: Message Set does not match SOP Class (Warning)
	public static final Status DataSetDoesNotMatchSOPClassWarning = new Status(0xB007);
	// Storage Warning: Elements Discarded
	public static final Status ElementsDiscarded = new Status(0xB006);
	// QueryRetrieve Failure: Out of Resources
	public static final Status OutOfResources = new Status(0xA700);
	// QueryRetrieve Failure: Unable to calculate number of matches
	public static final Status UnableToCalculateNumberOfMatches = new Status(0xA701);
	// QueryRetrieve Failure: Unable to perform suboperations
	public static final Status UnableToPerformSuboperations = new Status(0xA702);
	// QueryRetrieve Failure: Move Destination unknown
	public static final Status MoveDestinationUnknown = new Status(0xA801);
	// QueryRetrieve Failure: Identifier does not match SOP Class
	public static final Status IdentifierDoesNotMatchSOPClass = new Status(0xA900);
	// QueryRetrieve Failure: Unable to process
	public static final Status UnableToProcess = new Status(0xC000);
	// QueryRetrieve Pending: Optional Keys Not Supported
	public static final Status OptionalKeysNotSupported = new Status(0xFF01);
	// QueryRetrieve Warning: Sub-operations Complete - One or more Failures
	public static final Status SubOpsOneOrMoreFailures = new Status(0xB000);
}
