package ikoda.persistenceforanalysis.singletons;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;

public class RawDataUnit
{
	
	public final static String COLUMN_HEAD_JOB_TITLE="aa_jobtitle";
	public final static String COLUMN_HEAD_ID="a_uid";
	public final static String COLUMN_HEAD_REGION="aa_region";
	public final static String COLUMN_HEAD_MAJOR="aa_major";
	public final static String COLUMN_HEAD_UUID="aa_uuid";
	public final static String COLUMN_HEAD_AGGREGATEDMAJOR="aa_label";
	public final static String COLUMN_HEAD_LOCATION="aa_location";
	
	
	
	
	private String region="";
	private String id;

	private String section="01";
	private String jobTitle="";
	private String location="";

	private String majorFinal="";
	
	private String jobDescription="";

	private List<String> qualifications = new ArrayList<>();

	private List<String> workSkills = new ArrayList<>();

	private List<String> relatedMajors = new ArrayList<>();

	private List<String> areasOfStudy = new ArrayList<>();
	
	private List<String> skills = new ArrayList<>();

	private List<String> yearsExperience = new ArrayList<>();

	public RawDataUnit()
	{
		
	}

	public List<String> getAreasOfStudy()
	{
		return areasOfStudy;
	}

	public String getId()
	{
		return id;
	}

	public String getJobDescription()
	{
		return jobDescription;
	}

	public String getJobTitle()
	{
		return jobTitle;
	}

	public String getLocation()
	{
		return location;
	}

	public String getMajorFinal()
	{
		return majorFinal;
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

	public String getUIDAsString()
	{
	    return id+section;
	}

	public List<String> getWorkSkills()
	{
		return workSkills;
	}

	public List<String> getYearsExperience()
	{
		return yearsExperience;
	}

	public void setAreasOfStudy(List<String> inareasOfStudy)
	{
		
		for (String s : inareasOfStudy)
		{
			String s1 = s.replaceAll(",", "-");
			areasOfStudy.add(s1);
		}

	}

	public void setId(String inid)
	{
		if(null!=inid)
		{
		this.id = inid;
		}
	}

	public void setJobDescription(String injobDescription)
	{
		if(null!=injobDescription)
		{
		this.jobDescription = injobDescription;
		}
	}

	public void setJobTitle(String injobTitle)
	{
		if(null!=injobTitle)
		{
		this.jobTitle = injobTitle;
		}
	}

	public void setLocation(String inlocation)
	{
		if(null!=inlocation)
		{
		this.location = inlocation;
		}
	}

	public void setMajorFinal(String majorFinal)
	{
		if(null!=majorFinal)
		{
		this.majorFinal = majorFinal;
		}
	}

	public void setQualifications(List<String> qualifications)
	{
		this.qualifications = qualifications;
	}

	public void setRegion(String inregion)
	{
		if(null!=region)
		{
			this.region = inregion;
		}
	}

	public void setRelatedMajors(List<String> relatedMajors)
	{
		this.relatedMajors = relatedMajors;
	}

	public void setSection(Integer section)
    {
        if(section <10)
        {
            this.section = "0"+String.valueOf(section);
        }
        else
        {
            this.section = String.valueOf(section);
        }
    }

	public void setSkills(List<String> inskills)
	{
		for (String s : inskills)
		{
			String s1 = s.replaceAll(",", "-");
			skills.add(s1);
		}
	}

    



	public void setWorkSkills(List<String> workSkills)
	{
		this.workSkills = workSkills;
	}
	
	public void setYearsExperience(List<String> yearsExperience)
	{
		this.yearsExperience = yearsExperience;
	}

}
