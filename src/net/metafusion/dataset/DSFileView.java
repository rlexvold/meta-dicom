package net.metafusion.dataset;

import net.metafusion.util.Tag;

public class DSFileView
{
	public String			ImageType;								// ORIGINAL\PRIMARY\AXIAL
	public String			SOPClassUID;							// e.g. Storage SOP Class
	public String			SOPInstanceUID;						// imageuid
	public String			InstanceCreationDate;					// 19950126
	public String			InstanceCreationTime;					// 094200.000
	public String			StudyDate;								// 19950126
	public String			SeriesDate;							// 19950126
	public String			AcquisitionDate;						// 19950126
	public String			AcquisitionDatetime;					// alt format newer
	public String			StudyTime;								// 094200.000
	public String			SeriesTime;							// 094200.000
	public String			AcquisitionTime;						// 094200.000
	public String			AccessionNumber;						// PIKR0003
	public String			Modality;								// CT
	public String			Manufacturer;							// GE MEDICAL SYSTEMS
	public String			ReferringPhysicianName;				// GEMO
	public String			StationName;							// Picket_CT
	public String			StudyDescription;						// CERVCIAL
	public String			SeriesDescription;						// 3-pl T2* FGRE S
	public String			PerformingPhysicianName;
	public String			NameOfPhysicianReadingStudy;
	public String			OperatorName;
	public String			PatientName;							// SMITH^HAROLD
	public String			PatientID;								// PIKR750000
	public String			PatientBirthDate;
	public String			PatientAge;							// 046Y
	public String			PatientWeight;							// 90.718
	public String			BodyPartExamined;						// ?
	public String			PatientSex;							// M
	public String			StudyInstanceUID;						// UID
	public String			SeriesInstanceUID;						// UID
	public String			StudyID;								// 1665
	public String			SeriesNumber;							// 1
	public String			AcquisitionNumber;						// 1
	public String			InstanceNumber;						// 59?
	public String			AdditionalPatientHistory;				// CANCER
	public String			SliceThickness;						// 3
	public String			SpacingBetweenSlices;					// 15000
	public String			PatientPosition;						// HFS
	public String			ImagePosition;							// -8\-130\130
	public String			ImageOrientation;						// 0\1\0\0\0\-1
	public String			SliceLocation;							// 8.000000000
	public short			SamplesPerPixel;						// 1
	public String			PhotometricInterpretation;				// MONOCHROME2
	public short			Rows;									// 256
	public short			Columns;								// 256
	public String			PixelSpacing;							// 1.015625\1.015625
	public short			BitsAllocated;							// 16
	public short			BitsStored;							// 16
	public short			HighBit;								// 15
	public String			WindowCenter;							// 809
	public String			WindowWidth;							// 1618
	public short			PlanarConfiguration;
	public String			NumberOfFrames;
	public short			PixelRepresentation;
	public String			ViewPosition;
	public String			InstitutionName;
	public String			ProtocolName;
	public short			PixelPaddingValue;
	public String			RescaleIntercept;
	public String			RescaleSlope;
	public int				ImageOffset;							// psuedo-field
	public int				ImageSize;								// pseudo-field
	public static DSViewDef	viewMap	= new DSViewDef(DSFileView.class, new Tag[] {
			Tag.ImageType,
			Tag.SOPClassUID,
			Tag.SOPInstanceUID,
			Tag.InstanceCreationDate,
			Tag.InstanceCreationTime,
			Tag.StudyDate,
			Tag.SeriesDate,
			Tag.AcquisitionDate,
			Tag.AcquisitionDatetime,
			Tag.StudyTime,
			Tag.SeriesTime,
			Tag.AcquisitionTime,
			Tag.AccessionNumber,
			Tag.Modality,
			Tag.Manufacturer,
			Tag.ReferringPhysicianName,
			Tag.StationName,
			Tag.StudyDescription,
			Tag.SeriesDescription,
			Tag.PerformingPhysicianName,
			Tag.NameOfPhysicianReadingStudy,
			Tag.OperatorName,
			Tag.PatientName,
			Tag.PatientID,
			Tag.PatientBirthDate,
			Tag.PatientAge,
			Tag.PatientWeight,
			Tag.BodyPartExamined,
			Tag.PatientSex,
			Tag.StudyInstanceUID,
			Tag.SeriesInstanceUID,
			Tag.StudyID,
			Tag.SeriesNumber,
			Tag.AcquisitionNumber,
			Tag.InstanceNumber,
			Tag.AdditionalPatientHistory,
			Tag.SliceThickness,
			Tag.SpacingBetweenSlices,
			Tag.PatientPosition,
			Tag.ImagePosition,
			Tag.ImageOrientation,
			Tag.SliceLocation,
			Tag.SamplesPerPixel,
			Tag.PhotometricInterpretation,
			Tag.Rows,
			Tag.Columns,
			Tag.PixelSpacing,
			Tag.BitsAllocated,
			Tag.BitsStored,
			Tag.HighBit,
			Tag.WindowCenter,
			Tag.WindowWidth,
			Tag.PlanarConfiguration,
			Tag.NumberOfFrames,
			Tag.PixelRepresentation,
			Tag.ViewPosition,
			Tag.InstitutionName,
			Tag.ProtocolName,
			Tag.PixelPaddingValue,
			Tag.RescaleIntercept,
			Tag.RescaleSlope		}, "ImageOffset", "ImageSize");

	public String toString()
	{
		return "ImageType:"
				+ ImageType
				+ "\n"
				+ // ORIGINAL\PRIMARY\AXIAL
				"InstanceCreationDate:"
				+ InstanceCreationDate
				+ "\n"
				+ // 19950126
				"InstanceCreationTime:"
				+ InstanceCreationTime
				+ "\n"
				+ // 094200.000
				"StudyDate:"
				+ StudyDate
				+ "\n"
				+ // 19950126
				"SeriesDate:" + SeriesDate + "\n" + "AcquisitionDate:" + AcquisitionDate + "\n" + "AcquisitionDatetime:" + AcquisitionDatetime + "\n" + "StudyTime:" + StudyTime
				+ "\n" + "SeriesTime:" + SeriesTime + "\n" + "AcquisitionTime:" + AcquisitionTime + "\n" + "AccessionNumber:" + AccessionNumber + "\n" + "Modality:" + Modality
				+ "\n" + "ReferringPhysicianName:" + ReferringPhysicianName + "\n" + "StationName:" + StationName + "\n" + "StudyDescription:" + StudyDescription + "\n"
				+ "SeriesDescription:" + SeriesDescription + "\n" + "PerformingPhysicianName:" + PerformingPhysicianName + "\n" + "NameOfPhysicianReadingStudy:"
				+ NameOfPhysicianReadingStudy + "\n" + "OperatorName:" + OperatorName + "\n" + "PatientName:" + PatientName + "\n" + "PatientID:" + PatientID + "\n"
				+ "PatientBirthDate:" + PatientBirthDate + "\n" + "PatientAge:" + PatientAge + "\n" + "BodyPartExamined:" + BodyPartExamined + "\n" + "PatientSex:" + PatientSex
				+ "\n" + "StudyInstanceUID:" + StudyInstanceUID + "\n" + "SeriesInstanceUID:" + SeriesInstanceUID + "\n" + "StudyID:" + StudyID + "\n" + "SeriesNumber:"
				+ SeriesNumber + "\n" + "AcquisitionNumber:" + AcquisitionNumber + "\n" + "InstanceNumber:" + InstanceNumber; // 59
		// ?
	}
}
