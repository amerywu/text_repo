package ikoda.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity

public class ValueSalaryByDegreeAndJobTitle implements Serializable
{
	private static final long serialVersionUID = 14596399058325L;
	@Column(nullable = false)
	private String degreeName;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "countDegreeId", referencedColumnName = "id", nullable = false)
	private CountDegree countDegree;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "uniqueJobId", referencedColumnName = "id", nullable = false)
	private UniqueJob uniqueJob;

	@Column(nullable = false)
	private String jobTitle;

	@Column(nullable = false)
	private Integer count;

	@Column(nullable = true)
	private Integer averageSalary;

	@Column(nullable = true)
	private Integer medianSalary;

	@Column(nullable = false)
	private Long totalSalary;

	@Column(nullable = false)
	private String region;

	@CreationTimestamp
	protected Date created;

	@Column(nullable = true)
	private String ikodaDegreeName;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

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
		ValueSalaryByDegreeAndJobTitle other = (ValueSalaryByDegreeAndJobTitle) obj;
		if (averageSalary == null)
		{
			if (other.averageSalary != null)
			{
				return false;
			}
		}
		else if (!averageSalary.equals(other.averageSalary))
		{
			return false;
		}
		if (count == null)
		{
			if (other.count != null)
			{
				return false;
			}
		}
		else if (!count.equals(other.count))
		{
			return false;
		}
		if (created == null)
		{
			if (other.created != null)
			{
				return false;
			}
		}
		else if (!created.equals(other.created))
		{
			return false;
		}
		if (degreeName == null)
		{
			if (other.degreeName != null)
			{
				return false;
			}
		}
		else if (!degreeName.equals(other.degreeName))
		{
			return false;
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
		if (totalSalary == null)
		{
			if (other.totalSalary != null)
			{
				return false;
			}
		}
		else if (!totalSalary.equals(other.totalSalary))
		{
			return false;
		}
		if (updated == null)
		{
			if (other.updated != null)
			{
				return false;
			}
		}
		else if (!updated.equals(other.updated))
		{
			return false;
		}
		return true;
	}

	public Integer getAverageSalary()
	{
		return averageSalary;
	}

	public Integer getCount()
	{
		return count;
	}

	public CountDegree getCountDegree()
	{
		return countDegree;
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

	public String getIkodaDegreeName()
	{
		return ikodaDegreeName;
	}

	public String getJobTitle()
	{
		return jobTitle;
	}

	public Integer getMedianSalary()
	{
		return medianSalary;
	}

	public String getRegion()
	{
		return region;
	}

	public Long getTotalSalary()
	{
		return totalSalary;
	}

	public Integer getTypicalSalary()
	{
		if (null != medianSalary && medianSalary.intValue() > 10000)
		{
			return medianSalary;
		}
		else
		{
			return averageSalary;
		}
	}

	public UniqueJob getUniqueJob()
	{
		return uniqueJob;
	}

	public Date getUpdated()
	{
		return updated;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((averageSalary == null) ? 0 : averageSalary.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((degreeName == null) ? 0 : degreeName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((totalSalary == null) ? 0 : totalSalary.hashCode());
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		return result;
	}

	public void setAverageSalary(Integer averageSalary)
	{
		this.averageSalary = averageSalary;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public void setCountDegree(CountDegree countDegree)
	{
		this.countDegree = countDegree;
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

	public void setIkodaDegreeName(String ikodaDegreeName)
	{
		this.ikodaDegreeName = ikodaDegreeName;
	}

	public void setJobTitle(String jobTitle)
	{
		this.jobTitle = jobTitle;
	}

	public void setMedianSalary(Integer medianSalary)
	{
		this.medianSalary = medianSalary;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public void setTotalSalary(Long totalSalary)
	{
		this.totalSalary = totalSalary;
	}

	public void setUniqueJob(UniqueJob uniqueJob)
	{
		this.uniqueJob = uniqueJob;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	@Override
	public String toString()
	{
		return "ValueSalaryByDegreeAndJobTitle [\ndegreeName=" + degreeName + "\n jobTitle=" + jobTitle + "\n count="
				+ count + "\n averageSalary=" + averageSalary + "\n medianSalary=" + medianSalary + "\ntotalSalary="
				+ totalSalary + "\n region=" + region + "\nid=" + id + "]";
	}

}
