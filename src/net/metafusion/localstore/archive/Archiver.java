package net.metafusion.localstore.archive;

import java.sql.Date;
import java.util.List;
import net.metafusion.model.StudyView;

public class Archiver
{
	static Archiver instance;

	static Archiver get()
	{
		return instance;
	}

	static void init()
	{
		instance = new Archiver();
	}

	private Archiver()
	{
	}

	void Archive(Date d)
	{
		List l = StudyView.get().selectBetween(d, new Date(d.getTime() + 24 * 60 * 60 * 1000));
		// DicomDir d = DicomDir(File rootDir, List studies)
	}
}