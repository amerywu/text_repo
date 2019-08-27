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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ikoda.fileio.FioLog;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "specifiedDegreeName","degreeLevel" }) )
public class CountDegree implements Serializable
{

	private static final long serialVersionUID = 14596397858365L;
	public final static String DEGREELEVEL="DEGREE_LEVEL";
	public final static String CERTIFICATELEVEL="CERTIFICATE_LEVEL";
	public final static String CERTIFICATEANDDEGREELEVEL="DEGREE_OR_CERTIFICATE_LEVEL";
	
	private final static String[] validDegreeLevels={DEGREELEVEL,CERTIFICATELEVEL,CERTIFICATEANDDEGREELEVEL};

	private String specifiedDegreeName;

	@OneToMany(mappedBy = "specifiedDegree", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IkodaDegreeSpecifiedDegree> ikodaDegreeSpecifiedDegreeRelations = new ArrayList<IkodaDegreeSpecifiedDegree>();

	
	@OneToMany(mappedBy = "countDegree", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CountDegreeJob> countDegreeJobRelations = new ArrayList<CountDegreeJob>();
	
	

	
	private Integer count;

	@CreationTimestamp
	protected Date created;
	
	private String degreeLevel=DEGREELEVEL;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	@OneToMany(mappedBy = "countDegree", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ValueSalaryByDegree> valueSalaryByDegreeList = new ArrayList<ValueSalaryByDegree>();

	@OneToMany(mappedBy = "countDegree", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<ValueSalaryByDegreeAndJobTitle> valueSalaryByDegreeAndJobTitleList = new ArrayList<ValueSalaryByDegreeAndJobTitle>();

	@OneToMany(mappedBy = "countDegree", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<CountJobTitleByDegreeName> countJobTitleByDegreeNameList = new ArrayList<CountJobTitleByDegreeName>();

	@Transient
	public static boolean isValidDegreelevel(String degreeLevel)
	{
		for(int i = 0;i<validDegreeLevels.length;i++)
		{
			if(degreeLevel.equals(validDegreeLevels[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean addIkodaDegreeSpecifiedDegreeRelation(IkodaDegreeSpecifiedDegree idsdnew)
	{
		try
		{
			for (IkodaDegreeSpecifiedDegree idsdextant : ikodaDegreeSpecifiedDegreeRelations)
			{
				if (idsdnew.equals(idsdextant))
				{
					return false;
				}
			}
			return ikodaDegreeSpecifiedDegreeRelations.add(idsdnew);
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
			return false;
		}
	}

	public boolean containsRelatedIkodaDegree(String ikodaDegreeName)
	{
		try
		{
			for (IkodaDegreeSpecifiedDegree idsd : ikodaDegreeSpecifiedDegreeRelations)
			{
				if (ikodaDegreeName.toUpperCase().equals(idsd.getIkodaDegree().getIkodaDegreeName().toUpperCase()))
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
		CountDegree other = (CountDegree) obj;
		if (specifiedDegreeName == null)
		{
			if (other.specifiedDegreeName != null)
			{
				return false;
			}
		}
		else if (!specifiedDegreeName.equals(other.specifiedDegreeName))
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

	public String getDegreeLevel()
	{
		return degreeLevel;
	}

	public Long getId()
	{
		return id;
	}

	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeRelations()
	{
		return ikodaDegreeSpecifiedDegreeRelations;
	}

	public String getSpecifiedDegreeName()
	{
		return specifiedDegreeName;
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
		result = prime * result + ((specifiedDegreeName == null) ? 0 : specifiedDegreeName.hashCode());
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

	public void setDegreeLevel(String degreeLevel)
	{
		this.degreeLevel = degreeLevel;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setSpecifiedDegreeName(String specifiedDegreeName)
	{
		this.specifiedDegreeName = specifiedDegreeName;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	@Override
	public String toString()
	{
		return "CountDegree [specifiedDegreeName=" + specifiedDegreeName + ", count=" + count + "]";
	}

}
