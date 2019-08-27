package ikoda.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames ={ "jobId", "countDegreeId","uniqueJobId" }) )
public class CountDegreeJob implements Serializable
{
	private static final long serialVersionUID = 19486397858365L;
	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "jobId", referencedColumnName = "id", nullable = false)
	private Job job;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uniqueJobId", referencedColumnName = "id", nullable = false)
	private UniqueJob uniqueJob;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "countDegreeId", referencedColumnName = "id", nullable = false)
	private CountDegree countDegree;
	


	

	public Date getCreated()
	{
		return created;
	}

	public Long getId()
	{
		return id;
	}

	
	public CountDegree getCountDegree()
	{
		return countDegree;
	}

	public Date getUpdated()
	{
		return updated;
	}



	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setId(Long id)
	{
		this.id = id;
	}



	public void setCountDegree(CountDegree countDegree)
	{
		this.countDegree = countDegree;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	public Job getJob()
	{
		return job;
	}

	public void setJob(Job job)
	{
		this.job = job;
	}

	public UniqueJob getUniqueJob()
	{
		return uniqueJob;
	}

	public void setUniqueJob(UniqueJob uniqueJob)
	{
		this.uniqueJob = uniqueJob;
	}



	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((countDegree == null) ? 0 : countDegree.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((job == null) ? 0 : job.hashCode());
		result = prime * result + ((uniqueJob == null) ? 0 : uniqueJob.hashCode());
		return result;
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
		CountDegreeJob other = (CountDegreeJob) obj;
		if (countDegree == null)
		{
			if (other.countDegree != null)
			{
				return false;
			}
		}
		else if (!countDegree.equals(other.countDegree))
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
		if (job == null)
		{
			if (other.job != null)
			{
				return false;
			}
		}
		else if (!job.equals(other.job))
		{
			return false;
		}
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

	@Override
	public String toString()
	{
		return "CountDegreeJob [job=" + job.getJobTitle() + ", uniqueJob=" + uniqueJob.getFriendlyName() + ", countDegree=" + countDegree.getSpecifiedDegreeName() + "]";
	}


	
}
