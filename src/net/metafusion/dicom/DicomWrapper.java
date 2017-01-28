package net.metafusion.dicom;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

import acme.util.Log;
import net.metafusion.dataset.DS;
import net.metafusion.importer.ImageImportHeader;
import net.metafusion.importer.ImageImportView;
import net.metafusion.service.CFind;
import net.metafusion.service.CMove;
import net.metafusion.util.Tag;

public class DicomWrapper
{
	public ArrayList<DS> DicomSearch(String sourceAE, DS ds)
	{
		CFind find = new CFind();
		find.setAttrSet(ds);
		find.setSourceAE(sourceAE);
		find.run();
		ArrayList<DS> results = find.getResults();
		return results;
	}

	public boolean DicomMove(String sourceAE, String destAE, String[] moveList, String level)
	{
		try
		{
			CMove move = new CMove();
			move.setDestAE(destAE);
			move.setSourceAE(sourceAE);
			for (int i = 0; i < moveList.length; i++)
			{
				DS ds = CMove.buildSearch(Tag.StudyInstanceUID, moveList[i]);
				ds.putString(Tag.StudyInstanceUID, moveList[i]);
				ds.putString(Tag.QueryRetrieveLevel, level);
				move.setAttrSet(ds);
				move.run();
			}
		}
		catch (Exception e)
		{
			Log.log("Problem with DicomCmove: ", e);
			return false;
		}
		return true;
	}
}
