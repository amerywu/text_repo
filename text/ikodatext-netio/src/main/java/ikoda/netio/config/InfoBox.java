package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import ikoda.netio.NioLog;

@XmlType(propOrder =
{ "fileId", "databaseDescriptor", "blockNumber", "contentType", "region", "detailLevel", "yearsExperienceAsInt",
		"byExtrapolation", "degreeLevel", "jobTitles", "startSalaryRange", "endSalaryRange", "qualifications",
		"areasOfStudy", "certification", "skills", "workSkills", "yearsExperience", "location", "relatedMajors",
		"majorFinal", "jobFinal"})

@XmlRootElement(name = "infobox")
public class InfoBox
{

	private List<String> jobTitles = new ArrayList<String>();
	private List<String> qualifications = new ArrayList<String>();
	private List<String> areasOfStudy = new ArrayList<String>();
	private List<String> certification = new ArrayList<String>();
	private List<String> skills = new ArrayList<String>();
	private List<String> relatedMajors = new ArrayList<String>();
	private List<String> workSkills = new ArrayList<String>();
	private List<String> yearsExperience = new ArrayList<String>();

	private String databaseDescriptor;
	private String region;
	private String majorFinal;
	private String jobFinal;
	private String fileId = "";
	private Integer blockNumber = 0;
	private String contentType;
	private int startSalaryRange = -1;
	private int endSalaryRange = -1;
	private String infoBoxUID = "";
	private String degreeLevel;
	private int detailLevel = 0;
	private String location = "";
	private int yearsExperienceAsInt = -1;
	private boolean byExtrapolation = false;

	public InfoBox()
	{

	}

	public InfoBox(String inid)
	{
		fileId = inid;
	}

	public void addValueToInfoBox(String inkey, String valuein)
	{
		// NioLog.getLogger().info("addValueToInfoBox "+this.getInfoBoxUID());
		if (fileId.isEmpty() || fileId.length() < 2)
		{
			NioLog.getLogger().error(
					"\n\n\n\n\n\n CANNOT ADD DATA. ID IS BLANK. \nDid you use the empty constructor? That is only for xml generation\n\n\n\n\n\n");
			return;
		}
		if (null == inkey)
		{
			NioLog.getLogger().error("\n\n\n\n\n\n CANNOT ADD DATA. key is null\n\n\n\n\n\n");
			return;
		}
		if (!isValidKey(inkey))
		{
			NioLog.getLogger().error("\n\n\n\n\n\n CANNOT ADD DATA. key is invalid" + inkey + " \n\n\n\n\n\n");
			return;
		}
		// NioLog.getLogger().debug("adding " + inkey + " " + valuein);
		if (valuein.isEmpty())
		{
			NioLog.getLogger().warn("RETURNING: EMPTY VALUE " + inkey + " " + valuein);
			return;
		}

		String value = valuein.trim();

		// NioLog.getLogger().info("addValueToInfoBox "+inkey+" "+value);

		if (inkey.equals(StringConstantsInterface.INFOBOX_AREASOFSTUDY))
		{

			long count = areasOfStudy.stream()
					.filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase())).count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added AREAS OF STUDY: " + value);
				areasOfStudy.add(value);
			}
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_CERTIFICATION))
		{

			long count = certification.stream()
					.filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase())).count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added CERTIFICATION " + value);
				certification.add(value);
			}
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_QUALIFICATION))
		{
			long count = qualifications.stream()
					.filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase())).count();
			String newValue = value;
			if (value.trim().toUpperCase().equals("DEGREE IN"))
			{
				newValue = "Degree";
			}
			if (count == 0)
			{
				// NioLog.getLogger().debug("added " + newValue);
				qualifications.add(newValue);
			}
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_LOCATION))
		{
			location = value;
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_JOBTITLE))
		{
			long count = jobTitles.stream().filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase()))
					.count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added " + value);
				jobTitles.add(value);
			}
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_SKILLS))
		{

			long count = skills.stream().filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase()))
					.count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added " + value);
				detailLevel++;
				skills.add(value);
			}

			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE))
		{

			long count = yearsExperience.stream()
					.filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase())).count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added " + value);
				detailLevel++;
				yearsExperience.add(value);
			}
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_STARTSALARY))
		{

			try
			{
				startSalaryRange = new Integer(value).intValue();
			}
			catch (Exception e)
			{
				NioLog.getLogger().error(e.getMessage(), e);
			}
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_ENDSSALARY))
		{

			try
			{
				endSalaryRange = new Integer(value).intValue();
			}
			catch (Exception e)
			{
				NioLog.getLogger().error(e.getMessage(), e);
			}
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE_AS_INT))
		{

			try
			{
				int tempyrs = new Integer(value).intValue();

				if (yearsExperienceAsInt < tempyrs)
				{
					yearsExperienceAsInt = tempyrs;
				}
			}
			catch (Exception e)
			{
				NioLog.getLogger().error(e.getMessage(), e);
			}
		}

		else if (inkey.equals(StringConstantsInterface.INFOBOX_RELATED_MAJORS))
		{

			long count = relatedMajors.stream()
					.filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase())).count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added " + value);
				relatedMajors.add(value);
			}
			return;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_WORKSKILLS))
		{

			long count = workSkills.stream()
					.filter(extantValue -> extantValue.toUpperCase().equals(value.toUpperCase())).count();
			if (count == 0)
			{
				// NioLog.getLogger().debug("added " + value);
				workSkills.add(value);
			}
			return;
		}

	}

	public boolean containsAreaOfStudy(String ins)
	{
		for (String s : areasOfStudy)
		{
			if (ins.toUpperCase().equals(s.toUpperCase()))
			{
				return true;
			}

		}
		return false;
	}

	public List<String> getAreasOfStudy()
	{
		return areasOfStudy;
	}

	public Integer getBlockNumber()
	{
		return blockNumber;
	}

	public List<String> getCertification()
	{
		return certification;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getDatabaseDescriptor()
	{
		return databaseDescriptor;
	}

	public String getDegreeLevel()
	{
		return degreeLevel;
	}

	public int getDetailLevel()
	{
		return detailLevel;
	}

	public int getEndSalaryRange()
	{
		return endSalaryRange;
	}

	public String getFileId()
	{
		return fileId;
	}

	public String getInfoBoxUID()
	{
		return infoBoxUID;
	}

	public List<String> getJobTitles()
	{
		return jobTitles;
	}

	public String getLocation()
	{
		return location;
	}

	public List<String> getQualifications()
	{
		return qualifications;
	}

	public String getRegion()
	{
		return region;
	}

	public List<String> getRelatedMajors()
	{
		return relatedMajors;
	}

	public List<String> getSkills()
	{
		return skills;
	}

	public int getStartSalaryRange()
	{
		return startSalaryRange;
	}

	public List<String> getValuesByType(String inkey)
	{
		NioLog.getLogger().debug("getting " + inkey);

		if (inkey.equals(StringConstantsInterface.INFOBOX_AREASOFSTUDY))
		{
			return areasOfStudy;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_QUALIFICATION))
		{
			return qualifications;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_JOBTITLE))
		{
			return jobTitles;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_SKILLS))
		{
			return skills;
		}
		else if (inkey.equals(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE))
		{
			return yearsExperience;
		}
		else
		{
			NioLog.getLogger().error("\n\n\n\nKEY NOT RECOGNIZED\n\n");
			return null;
		}

	}

	public List<String> getWorkSkills()
	{
		return workSkills;
	}

	public List<String> getYearsExperience()
	{
		return yearsExperience;
	}

	public int getYearsExperienceAsInt()
	{
		return yearsExperienceAsInt;
	}

	public boolean isByExtrapolation()
	{
		return byExtrapolation;
	}

	boolean isValidKey(String s)
	{
		for (int i = 0; i < StringConstantsInterface.XINFOBOX_VALIDKEYS.length; i++)
		{
			if (StringConstantsInterface.XINFOBOX_VALIDKEYS[i].equals(s))
			{
				return true;
			}
		}
		return false;
	}

	@XmlElement
	public void setAreasOfStudy(List<String> areasOfStudy)
	{
		this.areasOfStudy = areasOfStudy;
	}

	@XmlElement
	public void setBlockNumber(Integer blockNumber)
	{
		this.blockNumber = blockNumber;
	}

	public void setByExtrapolation(boolean byExtrapolation)
	{
		this.byExtrapolation = byExtrapolation;
	}

	@XmlElement
	public void setCertification(List<String> certification)
	{
		this.certification = certification;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	@XmlElement
	public void setDatabaseDescriptor(String databaseDescriptor)
	{
		this.databaseDescriptor = databaseDescriptor;
	}

	@XmlElement
	public void setDegreeLevel(String degreeLevel)
	{
		this.degreeLevel = degreeLevel;
	}

	@XmlElement
	public void setDetailLevel(int detailLevel)
	{
		this.detailLevel = detailLevel;
	}

	@XmlElement
	public void setEndSalaryRange(int endSalaryRange)
	{
		this.endSalaryRange = endSalaryRange;
	}

	@XmlElement
	public void setFileId(String id)
	{
		this.fileId = id;
	}

	@XmlTransient
	public void setInfoBoxUID(String infoBoxUID)
	{

		this.infoBoxUID = infoBoxUID;
	}

	@XmlElement
	public void setJobTitles(List<String> jobTitles)
	{
		this.jobTitles = jobTitles;
	}
	@XmlElement
	public void setLocation(String location)
	{
		this.location = location;
	}

	@XmlElement
	public void setQualifications(List<String> qualifications)
	{
		this.qualifications = qualifications;
	}

	@XmlElement
	public void setRegion(String region)
	{
		this.region = region;
	}

	@XmlElement
	public void setRelatedMajors(List<String> relatedMajors)
	{
		this.relatedMajors = relatedMajors;
	}

	@XmlElement
	public void setSkills(List<String> skills)
	{
		this.skills = skills;
	}

	@XmlElement
	public void setStartSalaryRange(int startSalaryRange)
	{
		this.startSalaryRange = startSalaryRange;
	}

	@XmlElement
	public void setWorkSkills(List<String> workSkills)
	{
		this.workSkills = workSkills;
	}

	@XmlElement
	public void setYearsExperience(List<String> yearsExperience)
	{
		this.yearsExperience = yearsExperience;
	}

	@XmlElement
	public void setYearsExperienceAsInt(int yearsExperienceAsInt)
	{
		this.yearsExperienceAsInt = yearsExperienceAsInt;
	}

	public String getMajorFinal() {
		return majorFinal;
	}
	
	@XmlElement
	public void setMajorFinal(String majorFinal) {
		this.majorFinal = majorFinal;
	}

	public String getJobFinal() {
		return jobFinal;
	}
	
	@XmlElement
	public void setJobFinal(String jobFinal) {
		this.jobFinal = jobFinal;
	}

	@Override
	public String toString()
	{
		return "InfoBox [File id=" + fileId + " \njobTitles=" + jobTitles + "\nqualifications=" + qualifications
				+ "\nareasOfStudy=" + areasOfStudy + "\ncertification=" + this.certification + "\nskills=" + skills
				+ "\nrelatedMajors=" + relatedMajors + "\nyearsExperience=" + yearsExperience + "\nregion=" + region
				+ "\nfileId=" + fileId + "\nblockNumber=" + blockNumber + "\ncontentType=" + contentType
				+ "\nstartSalaryRange=" + startSalaryRange + "\nendSalaryRange=" + endSalaryRange + "\ninfoBoxUID="
				+ infoBoxUID + "\ndetailLevel=" + detailLevel + "\nyearsExperienceAsInt=" + yearsExperienceAsInt
				+ "\nlocation=" + location + "\nmajorFinal="+majorFinal+"]";
	}

}
