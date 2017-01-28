package net.metafusion.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StudyListBean
{
	public List studyList = new ArrayList();

	public StudyListBean()
	{
	}

	public StudyListBean(List studyList)
	{
		this.studyList = studyList;
	}

	public Iterator get()
	{
		return studyList.iterator();
	}

	public void add(String s)
	{
		studyList.add(s);
	}
}