package net.metafusion.localstore.richmedia;

import java.io.File;

import net.metafusion.model.ImageView;
import net.metafusion.model.RichMedia;
import net.metafusion.model.RichMediaView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import acme.storage.SSStore;

public class RichMediaManager
{
	private Long getStudyId(String studyuid) throws Exception
	{
		Study study = StudyView.get().selectByUID(studyuid);
		return study.getStudyID();
	}

	public String getMediaDir(String studyuid) throws Exception
	{
		Long studyid = getStudyId(studyuid);
		String dir = SSStore.get().getStudyDir(studyid).getAbsolutePath();
		String extDir = dir.replaceFirst(SSStore.get().getRootDir().getAbsolutePath(), "");
		File mediaDir = new File(SSStore.get().getRichMediaRoot(), extDir);
		if (!mediaDir.exists())
			mediaDir.mkdirs();
		return mediaDir.getAbsolutePath();
	}

	public String logTransferStart(String source, String destination, String studyuid) throws Exception
	{
		RichMedia richMedia = new RichMedia();
		if (source == null || source.length() == 0)
			source = "no source specified";
		if (destination == null || destination.length() == 0)
			destination = "no dest specified";
		if (studyuid == null || studyuid.length() == 0)
			studyuid = "no studyuid specified";
		richMedia.setSource(source);
		richMedia.setDestination(destination);
		richMedia.setSize(0L);
		richMedia.setStatus("started");
		richMedia.setStudyid(getStudyId(studyuid));
		richMedia.setRichMediaID(RichMediaView.get().getNextId());
		RichMediaView.get().insert(richMedia);
		return richMedia.getRichMediaID().toString();
	}

	public void changeLogStatus(Long logId, String status) throws Exception
	{
		RichMedia richMedia = RichMediaView.get().selectById(logId);
		richMedia.setStatus(status);
		RichMediaView.get().update(richMedia);
	}
}
