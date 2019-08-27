package ikoda.persistence.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "uniqueJob" }) )
public class UniqueJob implements Serializable
{
	private static final long serialVersionUID = 14596397358365L;
	private String uniqueJob;

	private String friendlyName;

	@OneToMany(mappedBy = "uniqueJob", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<Job> JobRelations = new ArrayList<Job>();

	@OneToMany(mappedBy = "uniqueJob", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ValueSalaryByDegreeAndJobTitle> valueSalaryByDegreeAndJobTitleList = new ArrayList<ValueSalaryByDegreeAndJobTitle>();

	@OneToMany(mappedBy = "uniqueJob", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ValueSalaryByJobTitle> valueSalaryByJobTitleList = new ArrayList<ValueSalaryByJobTitle>();

	@OneToOne(mappedBy = "uniqueJob", cascade = CascadeType.ALL)
	private CountJobTitle countJobTitle;

	@OneToMany(mappedBy = "uniqueJob", cascade = CascadeType.ALL)
	private List<CountJobTitleByDegreeName> countJobTitleByDegreeNameList = new ArrayList<CountJobTitleByDegreeName>();

	
	@OneToMany(mappedBy = "uniqueJob", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CountDegreeJob> countDegreeJobRelations = new ArrayList<CountDegreeJob>();
	
	@CreationTimestamp
	protected Date created;

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
		UniqueJob other = (UniqueJob) obj;
		if (uniqueJob == null)
		{
			if (other.uniqueJob != null)
			{
				return false;
			}
		}
		else if (!uniqueJob.equals(other.uniqueJob))
		{
			return false;
		}
		return true;
	}

	public CountJobTitle getCountJobTitle()
	{
		return countJobTitle;
	}

	public Date getCreated()
	{
		return created;
	}

	public String getFriendlyName()
	{
		return friendlyName;
	}

	public Long getId()
	{
		return id;
	}

	public String getUniqueJob()
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
		result = prime * result + ((uniqueJob == null) ? 0 : uniqueJob.hashCode());
		return result;
	}

	public void setCountJobTitle(CountJobTitle countJobTitle)
	{
		this.countJobTitle = countJobTitle;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setFriendlyName(String friendlyName)
	{
		this.friendlyName = friendlyName;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setUniqueJob(String uniqueJob)
	{
		this.uniqueJob = uniqueJob;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

}
