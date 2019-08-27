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

import ikoda.persistence.model.ValueSalaryByDegree;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "countDegreeId", "region" }) )
public class CPCSalaryByDegree implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 193736843887925L;
	private String degreeName;
	private Long countDegreeId;

	private Integer count;
	private String degreeLevel;
	private Integer averageSalary;

	private Integer medianSalary;
	private Integer typicalSalary;
	private String region;
	private int salaryBand;
	private int rank;

	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	public CPCSalaryByDegree()
	{

	}

	public CPCSalaryByDegree(ValueSalaryByDegree vsbd, String degreeLevel)
	{
		update(vsbd, degreeLevel);
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

	public Integer getMedianSalary()
	{
		return medianSalary;
	}

	public int getRank()
	{
		return rank;
	}

	public String getRegion()
	{
		return region;
	}

	public int getSalaryBand()
	{
		return salaryBand;
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

	public void setSalaryBand(int salaryBand)
	{
		this.salaryBand = salaryBand;
	}

	public void setTypicalSalary(Integer typicalSalary)
	{
		this.typicalSalary = typicalSalary;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}
	
	

	public String getDegreeLevel()
	{
		return degreeLevel;
	}

	public void setDegreeLevel(String degreeLevel)
	{
		this.degreeLevel = degreeLevel;
	}

	@Override
	public String toString()
	{
		return "CPCSalaryByDegree [degreeName=" + degreeName + ", count=" + count + ", averageSalary=" + averageSalary
				+ ", medianSalary=" + medianSalary + ", typicalSalary=" + typicalSalary + ", region=" + region
				+ ", salaryBand=" + salaryBand + ", rank=" + rank + "]";
	}

	public void update(ValueSalaryByDegree vsbd, String inDegreeLevel)
	{
		degreeName = vsbd.getDegreeName();
		count = vsbd.getCount();

		// PLog.getRLogger().debug("degree " + vsbd.getDegreeName());
		// PLog.getRLogger().debug("count " + vsbd.getCount());
		// PLog.getRLogger().debug("region " + vsbd.getRegion());
		averageSalary = vsbd.getAverageSalary();
		medianSalary = vsbd.getMedianSalary();
		region = vsbd.getRegion();
		countDegreeId = vsbd.getCountDegree().getId();
		degreeLevel=inDegreeLevel;
		// PLog.getRLogger().debug("averageSalary " + averageSalary);
		// PLog.getRLogger().debug("medianSalary " + medianSalary);

		if (null == medianSalary && null != averageSalary)
		{
			typicalSalary = averageSalary;
		}
		else if (null != medianSalary)
		{
			typicalSalary = medianSalary;
		}
		else
		{
			typicalSalary = 0;
		}
	}

}
