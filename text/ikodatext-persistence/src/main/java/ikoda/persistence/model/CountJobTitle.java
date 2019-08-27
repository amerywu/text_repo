package ikoda.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "jobTitle" }) )
public class CountJobTitle implements Serializable
{
	private static final long serialVersionUID = 14594397858365L;
	private String jobTitle;
	private Integer count;

	@CreationTimestamp
	protected Date created;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "uniqueJobId", referencedColumnName = "id", nullable = false)
	private UniqueJob uniqueJob;

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
		CountJobTitle other = (CountJobTitle) obj;
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
		if (jobTitle == null)
		{
			if (other.jobTitle != null)
			{
				return false;
			}
		}
		else if (!jobTitle.equals(other.jobTitle))
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

	public Integer getCount()
	{
		return count;
	}

	public Date getCreated()
	{
		return created;
	}

	public Long getId()
	{
		return id;
	}

	public String getJobTitle()
	{
		return jobTitle;
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
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((jobTitle == null) ? 0 : jobTitle.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		return result;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setJobTitle(String jobTitle)
	{
		this.jobTitle = jobTitle;
	}

	public void setUniqueJob(UniqueJob uniqueJob)
	{
		this.uniqueJob = uniqueJob;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

}
