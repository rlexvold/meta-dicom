package net.metafusion.localstore.tasks;

import java.io.File;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import acme.storage.SSStore;
import acme.util.FileUtil;

public class IntegrityCheck extends Task
{
	public IntegrityCheck()
	{
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			if (!f.getName().endsWith(".mdf")) return;
			Image i = ImageView.get().selectByID(Long.parseLong(f.getName().substring(0, f.getName().length() - 4)));
			if (i == null) log("missing db for " + f.getAbsolutePath());
		}
	}
	ImageView iv;

	@Override
	public void run()
	{
		DicomStore ds = DicomStore.get();
		SSStore ss = ds.getSSStore();
		FileUtil.forEachFile(ds.getSSStore().getRootDir(), true, false, true, new FR());
	}

	public static void main(String[] args)
	{
		start(args, IntegrityCheck.class);
	}
}