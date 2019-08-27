package ikoda.persistence.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class Job implements Serializable
{

	private static final long serialVersionUID = 14596397858465L;
	
	@Column(nullable=false)
	private String jobTitle;
	@Column(nullable=false)
	private String region;
	
	private String contentSource;
	private Integer salaryStartRange = -1;
	private Integer salaryEndRange = -1;
	private Integer detailLevel;
	private Integer yearsExperienceInt=-1;
	private String fileId;
	private String location;
	
	@Column(nullable=false)
	private String degreeLevel;
	
	@Column(nullable=true)
	private Boolean byExtrapolation = false;

	@ManyToOne
	@JoinColumn(name = "uniqueJobId", referencedColumnName = "id", nullable = false)
	private UniqueJob uniqueJob;

	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> qualifications = new ArrayList<String>();

	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> workSkills = new ArrayList<String>();

	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> relatedMajors = new ArrayList<String>();

	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> areasOfStudy = new ArrayList<String>();
	
	
	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> certifications = new ArrayList<String>();

	@ElementCollection
	@Column(length = 1000)
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> skills = new ArrayList<String>();

	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "job_id") )
	private List<String> yearsExperience = new ArrayList<String>();

	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;
	
	@OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CountDegreeJob> countDegreeJobRelations = new ArrayList<CountDegreeJob>();

	public boolean containsAreaOfStudy(String s)
	{
		for (String aos : areasOfStudy)
		{
			if (aos.toUpperCase().equals(s.toUpperCase()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Job other = (Job) obj;
		if (id == null)
		{
			return super.equals(obj);
		}
		if (id == null)
		{
			if (other.id != null)
			{
				return false;
			}
		}
		else if (!id.equals(other.id))
		{
			return false;
		}

		return true;
	}

	public List<String> getAreasOfStudy()
	{
		return areasOfStudy;
	}

	public List<String> getCertifications()
	{
		return certifications;
	}

	public String getContentSource()
	{
		return contentSource;
	}

	public Date getCreated()
	{
		return created;
	}

	public String getDegreeLevel()
	{
		return degreeLevel;
	}

	public Integer getDetailLevel()
	{
		return detailLevel;
	}

	public String getFileId()
	{
		return fileId;
	}

	public Long getId()
	{
		return id;
	}

	public String getJobTitle()
	{
		return jobTitle;
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

	public Integer getSalaryEndRange()
	{
		return salaryEndRange;
	}

	public Integer getSalaryStartRange()
	{
		return salaryStartRange;
	}

	public List<String> getSkills()
	{
		return skills;
	}

	public UniqueJob getUniqueJob()
	{
		return uniqueJob;
	}

	public Date getUpdated()
	{
		return updated;
	}

	public List<String> getWorkSkills()
	{
		return workSkills;
	}

	public List<String> getYearsExperience()
	{
		return yearsExperience;
	}

	public Integer getYearsExperienceInt()
	{
		return yearsExperienceInt;
	}

	public Boolean isByExtrapolation()
	{
		return byExtrapolation;
	}

	public void setAreasOfStudy(List<String> areasOfStudy)
	{
		this.areasOfStudy = areasOfStudy;
	}

	public void setByExtrapolation(Boolean byExtrapolation)
	{
		this.byExtrapolation = byExtrapolation;
	}

	public void setCertifications(List<String> certifications)
	{
		this.certifications = certifications;
	}

	public void setContentSource(String contentSource)
	{
		this.contentSource = contentSource;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setDegreeLevel(String degreeLevel)
	{
		this.degreeLevel = degreeLevel;
	}

	public void setDetailLevel(Integer detailLevel)
	{
		this.detailLevel = detailLevel;
	}

	public void setFileId(String fileId)
	{
		this.fileId = fileId;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setJobTitle(String jobTitle)
	{
		this.jobTitle = jobTitle;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public void setQualifications(List<String> qualifications)
	{
		this.qualifications = qualifications;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public void setRelatedMajors(List<String> relatedMajors)
	{
		this.relatedMajors = relatedMajors;
	}

	public void setSalaryEndRange(Integer salaryEndRange)
	{
		this.salaryEndRange = salaryEndRange;
	}

	public void setSalaryStartRange(Integer salaryStartRange)
	{
		this.salaryStartRange = salaryStartRange;
	}

	public void setSkills(List<String> skills)
	{
		this.skills = skills;
	}

	public void setUniqueJob(UniqueJob uniqueJob)
	{
		this.uniqueJob = uniqueJob;
	}
	
	
	

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	public void setWorkSkills(List<String> workSkills)
	{
		this.workSkills = workSkills;
	}

	
	
	
	
	public void setYearsExperience(List<String> yearsExperience)
	{
		this.yearsExperience = yearsExperience;
	}

	public void setYearsExperienceInt(Integer yearsExperienceInt)
	{
		this.yearsExperienceInt = yearsExperienceInt;
	}

	@Override
	public String toString()
	{
		return "Job [jobTitle=" + jobTitle + "\n region=" + region + "\n contentSource=" + contentSource
				+ "\n salaryStartRange=" + salaryStartRange + "\n salaryEndRange=" + salaryEndRange + "\n detailLevel="
				+ detailLevel + "\n yearsExperienceInt=" + yearsExperienceInt + "\n fileId=" + fileId + "\n uniqueJob="
				+ uniqueJob + "\n qualifications=" + qualifications + "\n workSkills=" + workSkills
				+ "\n relatedMajors=" + relatedMajors + "\n areasOfStudy=" + areasOfStudy + "\n skills=" + skills
				+ "\n yearsExperience=" + yearsExperience + "\n created=" + created + "\n id=" + id + "\n updated="
				+ updated + "]";
	}

}
