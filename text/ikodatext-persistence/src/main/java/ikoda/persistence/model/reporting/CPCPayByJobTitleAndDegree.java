package ikoda.persistence.model.reporting;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "countDegreeId", "region", "uniqueJobId", "ikodaDegreeId" }) )
public class CPCPayByJobTitleAndDegree implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4183666057908762414L;

	private final static String[] ANYDEGREE_BLACKLIST =
	{ "MOBILE", "REVIEW", "OGIST", "ORACLE", "SYSTEMS ADMINISTRATOR", "- PRI", "SENIOR MANAGER", "JOB DESCRIPTION",
			"DEVELOPER", "ENGINEER", "THERAPIST", "PHARMACIST", "PROFESSOR", "LECTURER", "MANAGER", "DATABASE" };

	private final static String[] ALLDEGREE_SBLACKLIST =
	{ "REVIEWS - SAINT", "DESCRIPTION" };

	private String degreeName;

	private Long countDegreeId;
	
	private String degreeLevel;

	private String jobTitle;

	private Long uniqueJobId;

	private Integer count;

	private Integer averageSalary;

	private Integer medianSalary;

	private Integer typicalSalary;
	
	private String iKodaDegreeName;

	private String region;

	private int rank = 5;
	
	private Long ikodaDegreeId;
	


	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	public CPCPayByJobTitleAndDegree()
	{

	}

	public CPCPayByJobTitleAndDegree(ValueSalaryByDegreeAndJobTitle vsdjt, Long iKodaDegreeId, String iKodaDegreeName, String inDegreeLevel)
	{
		update(vsdjt,iKodaDegreeId, iKodaDegreeName, inDegreeLevel);
	}

	public boolean blacklistedAllDegrees()
	{
		for (int i = 0; i < ALLDEGREE_SBLACKLIST.length; i++)
		{
			// SSm.getRLogger().debug(this.jobTitle.toUpperCase()+"
			// "+ANYDEGREE_BLACKLIST[i]);
			if (this.jobTitle.toUpperCase().contains(ALLDEGREE_SBLACKLIST[i]))
			{

				return true;
			}
		}
		return false;
	}

	public boolean blacklistedAnyDegree()
	{
		for (int i = 0; i < ANYDEGREE_BLACKLIST.length; i++)
		{
			// SSm.getRLogger().debug(this.jobTitle.toUpperCase()+"
			// "+ANYDEGREE_BLACKLIST[i]);
			if (this.jobTitle.toUpperCase().contains(ANYDEGREE_BLACKLIST[i]))
			{

				return true;
			}
		}
		return false;
	}

	public Integer getAverageSalary()
	{
		return averageSalary;
	}

	public Integer getCount()
	{
		return count;
	}

	public Long getCountDegreeId()
	{
		return countDegreeId;
	}

	public Date getCreated()
	{
		return created;
	}

	public String getDegreeName()
	{
		return degreeName;
	}

	public Long getId()
	{
		return id;
	}

	public Long getIkodaDegreeId()
	{
		return ikodaDegreeId;
	}

	public String getiKodaDegreeName()
	{
		return iKodaDegreeName;
	}

	public String getDegreeLevel()
	{
		return degreeLevel;
	}

	public void setDegreeLevel(String degreeLevel)
	{
		this.degreeLevel = degreeLevel;
	}

	public String getJobTitle()
	{
		return jobTitle;
	}

	public Integer getMedianSalary()
	{
		if (null == medianSalary)
		{
			return null;
		}
		int returnValue = (medianSalary.intValue() / 1000) * 1000;
		return new Integer(returnValue);
	}

	public int getRank()
	{
		return rank;
	}

	public String getRegion()
	{
		return region;
	}

	public Integer getTypicalSalary()
	{
		if (null == typicalSalary)
		{
			return null;
		}
		int returnValue = (typicalSalary.intValue() / 1000) * 1000;
		return new Integer(returnValue);
	}

	public Long getUniqueJobId()
	{
		return uniqueJobId;
	}

	public Date getUpdated()
	{
		return updated;
	}

	public void setAverageSalary(Integer averageSalary)
	{
		this.averageSalary = averageSalary;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public void setCountDegreeId(Long countDegreeId)
	{
		this.countDegreeId = countDegreeId;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setDegreeName(String degreeName)
	{
		this.degreeName = degreeName;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setIkodaDegreeId(Long ikodaDegreeId)
	{
		this.ikodaDegreeId = ikodaDegreeId;
	}

	public void setiKodaDegreeName(String iKodaDegreeName)
	{
		this.iKodaDegreeName = iKodaDegreeName;
	}

	public void setJobTitle(String jobTitle)
	{
		this.jobTitle = jobTitle;
	}

	public void setMedianSalary(Integer medianSalary)
	{
		this.medianSalary = medianSalary;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public void setTypicalSalary(Integer typicalSalary)
	{
		this.typicalSalary = typicalSalary;
	}

	public void setUniqueJobId(Long uniqueJobId)
	{
		this.uniqueJobId = uniqueJobId;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	@Override
	public String toString()
	{
		return "CPCPayByJobTitleAndDegree [degreeName=" + degreeName + ", jobTitle=" + jobTitle + ", uniqueJobId="
				+ uniqueJobId + ", count=" + count + ", averageSalary=" + averageSalary + ", medianSalary="
				+ medianSalary + ", typicalSalary=" + typicalSalary + ", region=" + region + ", ikodaDegreeName=" + "]";
	}

	public void update(ValueSalaryByDegreeAndJobTitle vsdjt, Long iKodaDegreeId, String iKodaDegreeName, String inDegreeLevel)
	{
		this.setAverageSalary(vsdjt.getAverageSalary());
		this.setCount(vsdjt.getCount());
		this.setDegreeName(vsdjt.getDegreeName());
		this.setJobTitle(vsdjt.getJobTitle());
		this.setMedianSalary(vsdjt.getMedianSalary());
		this.setRegion(vsdjt.getRegion());
		this.setTypicalSalary(vsdjt.getTypicalSalary());
		this.setUniqueJobId(vsdjt.getUniqueJob().getId());
		this.setCountDegreeId(vsdjt.getCountDegree().getId());
		this.setIkodaDegreeId(iKodaDegreeId);
		this.setiKodaDegreeName(iKodaDegreeName);
		this.setDegreeLevel(inDegreeLevel);
	}

}
