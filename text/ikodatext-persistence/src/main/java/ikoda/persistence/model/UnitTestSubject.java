package ikoda.persistence.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "id", "attributeKey", "value" }) )
public class UnitTestSubject
{
	private String attributeKey;

	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	private String value;

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
		UnitTestSubject other = (UnitTestSubject) obj;
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

	public String getAttributeKey()
	{
		return attributeKey;
	}

	public Date getCreated()
	{
		return created;
	}

	public Long getId()
	{
		return id;
	}

	public Date getUpdated()
	{
		return updated;
	}

	public String getValue()
	{
		return value;
	}

	public void setAttributeKey(String attributeKey)
	{
		this.attributeKey = attributeKey;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
