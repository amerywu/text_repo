package ikoda.persistence.model;

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

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "databaseLocale" }) )
public class DatabaseDescriptors implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 14596397858365L;

	private String databaseLocale = "en";
	private String databaseDescriptor = "JOB_ANALYSIS_CONFIGURATION_DIPLOMA_LEVEL";

	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	public Date getCreated()
	{
		return created;
	}

	public String getDatabaseLocale()
	{
		return databaseLocale;
	}

	public Long getId()
	{
		return id;
	}

	public Date getUpdated()
	{
		return updated;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setDatabaseLocale(String databaseLocale)
	{
		this.databaseLocale = databaseLocale;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	public String getDatabaseDescriptor()
	{
		return databaseDescriptor;
	}

	public void setDatabaseDescriptor(String databaseDescriptor)
	{
		this.databaseDescriptor = databaseDescriptor;
	}

}
