package net.metafusion.util;

import java.io.File;

import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageFile;
import acme.storage.SSStore;

public class FileChecker
{
	private ImageFile	image	= null;
	private File		dir		= null;

	public FileChecker(ImageFile image)
	{
		this.image = image;
		SSStore ss = DicomStore.get().getSSStore();
		dir = ss.getStudyDir(image.getStudyID());
	}

	public boolean studyDirExists()
	{
		return dir.exists();
	}

	public boolean imageExists()
	{
		File jpeg = new File(dir, image.getImageID() + ".jpg");
		return jpeg.exists();
	}

	public boolean mdfExists()
	{
		File mdf = new File(dir, image.getImageID() + ".mdf");
		return mdf.exists();
	}

	public boolean dcmExists()
	{
		File dcm = new File(dir, image.getImageID() + ".dcm");
		return dcm.exists();
	}

	public boolean zipExists()
	{
		File zip = new File(dir, dir + ".zip");
		return zip.exists();
	}
}
