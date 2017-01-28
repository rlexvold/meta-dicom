package net.metafusion.archive.primera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

public class PrimeraStatus
{
	private File		systemStatusFile	= null;
	private Properties	statusProperties	= null;
	public enum JobState
	{
		JOB_NOT_STARTED(0, "Job Not Started"), JOB_RUNNING(1, "Job Running"), JOB_COMPLETED_SUCCESSFULLY(2, "Job Completed Successfully"), JOB_FAILED(3, "Job Failed");
		private Integer	id;
		private String	label;

		JobState(Integer id, String label)
		{
			this.id = id;
			this.label = label;
		}

		public Integer getId()
		{
			return id;
		}

		public String toString()
		{
			return label;
		}
	};
	public enum LoadDiscState
	{
		DISC_LOADED(1), DISC_PROCESSING(2), DISC_PROCESSED(3), DISC_UNLOADING(4);
		private Integer	id;

		LoadDiscState(Integer id)
		{
			this.id = id;
		}
	};
	public enum DriveState
	{
		IDLE(0), RECORDING(1), READING(2), VERIFYING(3), DISC_LOADED(4), VEFIFY_FAILED(5), VERIFY_COMPLETE(6), RECORD_FAILED(7), RECORD_COMPLETE(8);
		private Integer	id;

		DriveState(Integer id)
		{
			this.id = id;
		}
	};
	public enum SystemError
	{
		NO_ERRORS(0, "No errors"), PRINTER_MOVE(1, "Tray movement error.  Press the Cartridge button (left button) on the unit to try again."), INVALID_CARTRIDGE(
				2,
				"There was a problem finding the ink cartridges.  Open the cover and press the Cartridge button (left button). Make sure the color cartridge is installed on the left and the black is on the right.  Then close the lid."), INPUT_EMPTY(
				3, "The input bin is empty.  Add more discs and push the Cartridge button (left button) on the unit."), INTERNAL_COMM(4,
				"There was an internal printer communications error. Press the Cartridge button (left button) on the unit to try again."), COLOR_LOW(
				5,
				"WARNING:  The COLOR cartridge (left side) is LOW on ink.  To replace the cartridge, open the cover on the unit and press the Cartridge button (left button), install the new cartridge, and close the cover.  To ignore the warning press the Cartridge button (left button)."), BLACK_LOW(
				6,
				"WARNING:  The BLACK cartridge (right side) is LOW on ink.  To replace the cartridge, open the cover on the unit and press the Cartridge button (left button), install the new cartridge, and close the cover.  To ignore the warning press the Cartridge button (left button)."), BOTH_LOW(
				7,
				"WARNING:  BOTH cartridges are LOW on ink.  To replace the cartridge, open the cover on the unit and press the Cartridge button (left button)., install the new cartridges, and close the cover.  To ignore the warning press the Cartridge button (left button)."), PICK(
				8, "The disc was not picked. Push the Cartridge button (left button) on the unit to try again."), ARM_MOVEMENT(9,
				"There was an arm movement error.  Press the Cartridge button (left button) on the unit to try again."), PRINTER_MOVEMENT(10,
				"Arm picker error (unable to home the picker or unable to hook up the picker).  Press the Cartridge button (left button) on the unit to try again."), ADMIN(11,
				"The current user does not have local administrator rights.  Please login with local administrator rights and re-start the software."), INTERNAL_SOFTWARE(12,
				"There was an internal software error.  Please re-start the software."), NO_ROBOTIC_DRIVES(
				13,
				"No external recorder drives were found.  Please unplug all cables from the Disc Publisher (including power).  Then plug all cables back into the Disc Publisher, re-boot the computer, and then re-start the software."), OFFLINE(
				14, "The Disc Publisher is offline.  Please ensure the unit is connected and powered on, and then shutdown and restart the software."), COVER_OPEN(15,
				"The Disc Publisher cover is open.  Please close the cover."), DISC_NOT_PICKED(
				16,
				"The disc was not picked from the printer.  Please manually remove the disc from the printer.  Then close the cover and press the power button (right button) several times to reset the printer."), PICK2(
				17,
				"Multiple discs were picked up and moved.  Please manually remove any extra discs that were moved, keeping a single disc in place.  Then close the cover and press the left button."), DROPPED_DISC_RECORDER(
				18, "The disc was dropped while moving into the Recorder.  Please manually place the disc into the Recorder tray.  Then close the cover and press the left button."), DROPPED_DISC_PRINTER(
				19, "The disc was dropped while moving into the Printer.  Please manually place the disc into the Printer tray.  Then close the cover and press the left button."), DROPPED_DISC_LEFT(
				20, "The disc was dropped while moving into the Left Bin.  Please manually place the disc into the Left Bin.  Then close the cover and press the left button."), DROPPED_DISC_REJECT(
				21,
				"The disc was dropped while moving to the Reject area (Front Slide).  Please remove the dropped disc from the printer.  Then close the cover and press the left button."), ALINGMENT(
				22, "The printer needs to be aligned.  Would you like to align the printer now? Click \"Yes\" to align the printer, or \"No\" to quit the application."), INVALID_COLOR_CARTRIDGE(
				23,
				"The color cartridge is invalid.  To change the cartridge open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), INVALID_BLACK_CARTRIDGE(
				24,
				"The black cartridge is invalid.  To change the cartridge open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), INVALID_BOTH_CARTRIDES(
				25,
				"Both cartridges are invalid.  To change the cartridges open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), NO_CARTRIDES(
				26,
				"No cartridges are installed.  To install cartridges open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), BLACK_IN_COLOR(
				27,
				"The black cartridge is in the color cartridge holder.  To change the cartridges open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), COLOR_IN_BLACK(
				28,
				"The color cartridge is in the black cartridge holder.  To change the cartridges open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), CARTRIDGES_SWAPPED(
				29,
				"Both cartridges are swapped (color in black and black in color holders).  To change the cartridges open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), PIGMENT_CARTRIDGE(
				30,
				"This printer is not compatible with a pigment based cartridge. To change the cartridges open the cover, press the \"Cartridge\" button, change the cartridge, close the cover and press \"Ok\" to continue."), ALIGNMENT_FAILED(
				31, "The alignment print failed.  Would you like to retry the alignment now? Click \"Yes\" to align the printer, or \"No\" to quit the application.");
		private Integer	id;
		private String	label;

		SystemError(Integer id, String label)
		{
			this.id = id;
			this.label = label;
		}

		public Integer getId()
		{
			return id;
		}

		public String toString()
		{
			return label;
		}
	};
	public enum JobError
	{
		NO_ERRORS(0), INTERNAL_RECORDING_ENGINE(1), INTERNAL_JOB_PROCESSOR(2), INTERAL_ROBOTICS(3), FILE_INVALID(4), TOO_MANY_FILES(5), NO_FILES(6), PRINT_FILE_INVALID(7), JOB_FILE_INVALID(
				8), STATUS_FILE_INVALID(9), MEDIA_INVALID(10), MEDIA_TOO_SMALL(11), MEDIA_NOT_BLANK(12), DRIVE_OPEN_CLOSE(13), DRIVE_NOT_READY(14), DRIVE_NOT_ROBOTIC(15), JOB_ABORTED(
				16), DVD_INVALID(17), RECORDING_ERROR(18), VERIFY_ERROR(19), REJECTS_EXCEEDED(20), SESSION_INVALID(21), CLIENT_INVALID(22), CLIENT_MESSAGE_ERROR(23), UNKNOWN_JOB_TYPE(
				24), CRITICAL_KEY_INVAID(25), TEMP_FOLDER_OVERFLOW(26), CDTEXT_INVAILD(27), PRINT_APP_NOT_INSTALLED(28), PRINT_FILE_MISSING(29), INVALID_CARTRIDGES_FOR_PRINT(30), READ_LOCATION_TOO_SMALL(
				31), READING_ERROR(32), PVD_INVALID(33), INVALID_JOB_FOR_PVD(34);
		private Integer	id;

		JobError(Integer id)
		{
			this.id = id;
		}
	}

	public File getSystemStatusFile()
	{
		return systemStatusFile;
	}

	public void setSystemStatusFile(File systemStatusFile)
	{
		this.systemStatusFile = systemStatusFile;
	};

	public void readSystemStatus()
	{
		if (systemStatusFile == null)
			return;
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(systemStatusFile);
			statusProperties = new Properties();
			statusProperties.load(fis);
		}
		catch (Exception e)
		{
			return;
		}
		finally
		{
			try
			{
				if (fis != null)
					fis.close();
			}
			catch (Exception e)
			{
			}
		}
	}
}
