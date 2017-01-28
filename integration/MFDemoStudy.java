package integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MFDemoStudy implements Serializable
{
	private String				studyType	= "CT";
	private Integer				studyNumber	= 1;
	private Map<String, String>	pairs;

	public MFDemoStudy()
	{
		pairs = new HashMap<String, String>();
	}

	public String getValue(String key)
	{
		return pairs.get(key);
	}

	public void addValue(String key, String value)
	{
		pairs.put(key, value);
	}

	public String getStudyType()
	{
		return studyType;
	}

	public void setStudyType(String studyType)
	{
		this.studyType = studyType;
	}

	public Integer getStudyNumber()
	{
		return studyNumber;
	}

	public void setStudyNumber(Integer studyNumber)
	{
		this.studyNumber = studyNumber;
	}

	public Map getPairs()
	{
		return pairs;
	}

	public void setPairs(Map pairs)
	{
		this.pairs = pairs;
	}
}
