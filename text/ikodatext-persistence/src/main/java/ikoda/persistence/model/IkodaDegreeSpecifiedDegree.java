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
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "ikodaDegreeId", "specifiedDegreeId" }) )
public class IkodaDegreeSpecifiedDegree implements Serializable
{
	private static final long serialVersionUID = 19486397858365L;
	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ikodaDegreeId", referencedColumnName = "id", nullable = false)
	private IkodaDegree ikodaDegree;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "specifiedDegreeId", referencedColumnName = "id", nullable = false)
	private CountDegree specifiedDegree;

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
		IkodaDegreeSpecifiedDegree other = (IkodaDegreeSpecifiedDegree) obj;
		if (ikodaDegree == null)
		{
			if (other.ikodaDegree != null)
			{
				return false;
			}
		}
		else if (!ikodaDegree.equals(other.ikodaDegree))
		{
			return false;
		}
		if (specifiedDegree == null)
		{
			if (other.specifiedDegree != null)
			{
				return false;
			}
		}
		else if (!specifiedDegree.equals(other.specifiedDegree))
		{
			return false;
		}
		return true;
	}

	public Date getCreated()
	{
		return created;
	}

	public Long getId()
	{
		return id;
	}

	public IkodaDegree getIkodaDegree()
	{
		return ikodaDegree;
	}

	public CountDegree getSpecifiedDegree()
	{
		return specifiedDegree;
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
		result = prime * result + ((ikodaDegree == null) ? 0 : ikodaDegree.hashCode());
		result = prime * result + ((specifiedDegree == null) ? 0 : specifiedDegree.hashCode());
		return result;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setIkodaDegree(IkodaDegree ikodaDegree)
	{
		this.ikodaDegree = ikodaDegree;
	}

	public void setSpecifiedDegree(CountDegree specifiedDegree)
	{
		this.specifiedDegree = specifiedDegree;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

}
