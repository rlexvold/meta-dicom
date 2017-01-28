package net.metafusion.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StudiesDumpBean implements Serializable
{
	// keys of hashmap are the StudyId
	public List studies = new LinkedList();
	public HashMap seriesListMap = new HashMap();
	public HashMap imageListMap = new HashMap();
	public HashMap patientMap = new HashMap();
}
