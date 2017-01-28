package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import acme.db.JDBCUtil;
import acme.db.View;

public class ImageView extends View
{
	static ImageView view = null;

	static public synchronized ImageView get()
	{
		if (view == null)
		{
			view = new ImageView();
		}
		return view;
	}

	ImageView()
	{
		super("dcm_image", new String[] { "imageID" }, new String[] { "studyID", "seriesID", "patientID", "imageType", "classUID", "imageUID", "transferSyntaxUID",
				"instanceNumber", "dateEntered", "dateModified", "status" });
	}

	public boolean exists(String uid)
	{
		return selectByUID(uid) != null;
	}

	public boolean exists(long id)
	{
		return selectByID(id) != null;
	}

	public Image selectByID(long id)
	{
		return (Image) doSelect1(buildSelect("imageID=?"), new Object[] { new Long(id) });
	}

	public Image selectByUID(String uid)
	{
		return (Image) doSelect1(buildSelect("imageUID=?"), new Object[] { uid });
	}

	public List selectByStudy(long studyID)
	{
		return selectWhere(" studyID=" + studyID);
	}

	public List selectBySeries(long seriesID)
	{
		return selectWhere(" seriesID=" + seriesID);
	}

	public int countImagesForSeries(long seriesID)
	{
		return doSelectInt("select count(*) from dcm_image where seriesID=" + seriesID, null);
	}

	public int countImagesForStudy(long studyID)
	{
		return doSelectInt("select count(*) from dcm_image where studyID=" + studyID, null);
	}

	public int countImagesForPatient(long patientID)
	{
		return doSelectInt("select count(*) from dcm_image where patientID=" + patientID, null);
	}

	public boolean setStatus(long imageID, char status)
	{
		String sql = "update dcm_image set status='" + status + "' where imageId=" + imageID;
		return JDBCUtil.get().update(sql) == 1;
	}

	//
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		Image i = new Image();
		i.setImageID(rs.getLong(offset++));
		i.setStudyID(rs.getLong(offset++));
		i.setSeriesID(rs.getLong(offset++));
		i.setPatientID(rs.getLong(offset++));
		i.setImageType(rs.getString(offset++));
		i.setClassUID(rs.getString(offset++));
		i.setImageUID(rs.getString(offset++));
		i.setTransferSyntaxUID(rs.getString(offset++));
		i.setInstanceNumber(rs.getString(offset++));
		i.setDateEntered(rs.getTimestamp(offset++));
		i.setDateModified(rs.getTimestamp(offset++));
		i.setStatus(rs.getString(offset++).charAt(0));
		return i;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		Image a = (Image) o;
		if (pk)
			ps.setLong(i++, a.getImageID());
		else
		{
			ps.setLong(i++, a.getStudyID());
			ps.setLong(i++, a.getSeriesID());
			ps.setLong(i++, a.getPatientID());
			ps.setString(i++, a.getImageType());
			ps.setString(i++, a.getClassUID());
			ps.setString(i++, a.getImageUID());
			ps.setString(i++, a.getTransferSyntaxUID());
			ps.setString(i++, a.getInstanceNumber());
			ps.setTimestamp(i++, a.getDateEntered());
			ps.setTimestamp(i++, a.getDateModified());
			ps.setString(i++, "" + a.getStatus());
		}
	}

	public static void main(String[] args)
	{
		log("start");
		try
		{
			Image a;
			ImageView v = ImageView.get();
			// v.insert(new Image(1,"2",new Date(System.currentTimeMillis())));
			List l = v.selectAll();
			for (int i = 0; i < l.size(); i++)
			{
				a = (Image) l.get(i);
				log("" + l.get(i));
				// a.setB("bbbbbb");
				// v.update(a);
				// v.delete(a);
			}
			a = (Image) v.select1(1);
			log("" + a);
			l = v.selectAll();
			for (int i = 0; i < l.size(); i++)
			{
				a = (Image) l.get(i);
				log("" + l.get(i));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("Error: failed with " + e);
			System.exit(-1);
		}
	}
}
