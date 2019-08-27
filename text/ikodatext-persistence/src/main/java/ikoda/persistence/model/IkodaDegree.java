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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ikoda.fileio.FioLog;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "ikodaDegreeName" }) )
public class IkodaDegree implements Serializable
{

	private static final long serialVersionUID = 14596323858365L;

	private String ikodaDegreeName;
	private String localIkodaDegreeName;

	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	@OneToMany(mappedBy = "ikodaDegree", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IkodaDegreeSpecifiedDegree> ikodaDegreeSpecifiedDegreeRelations = new ArrayList<IkodaDegreeSpecifiedDegree>();

	public boolean containsRelatedSpecifiedDegree(String specifiedDegreeName)
	{
		try
		{
			for (IkodaDegreeSpecifiedDegree idsd : ikodaDegreeSpecifiedDegreeRelations)
			{
				if (specifiedDegreeName.toUpperCase()
						.equals(idsd.getSpecifiedDegree().getSpecifiedDegreeName().toUpperCase()))
				{
					return true;
				}
			}
			return false;
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
			return false;
		}
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
		IkodaDegree other = (IkodaDegree) obj;
		if (ikodaDegreeName == null)
		{
			if (other.ikodaDegreeName != null)
			{
				return false;
			}
		}
		else if (!ikodaDegreeName.equals(other.ikodaDegreeName))
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

	public String getIkodaDegreeName()
	{
		return ikodaDegreeName;
	}

	public String getLocalIkodaDegreeName()
	{
		return localIkodaDegreeName;
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
		result = prime * result + ((ikodaDegreeName == null) ? 0 : ikodaDegreeName.hashCode());
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

	public void setIkodaDegreeName(String ikodaDegreeName)
	{
		this.ikodaDegreeName = ikodaDegreeName;
	}

	public void setLocalIkodaDegreeName(String localIkodaDegreeName)
	{
		this.localIkodaDegreeName = localIkodaDegreeName;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

}
