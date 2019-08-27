package ikoda.persistence.model.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import ikoda.persistence.application.PLog;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames =
{ "countDegreeId", "region", "uniqueJobId", "jobId", "iKodaDegreeId" }) )
public class CPCJob implements Serializable
{

	
	private static final long serialVersionUID = 983057294731297401L;
	private String jobTitle;
	private String region;
	private String contentSource;
	private Integer salaryStartRange = -1;
	private Integer salaryEndRange = -1;
	private Integer detailLevel;
	private Integer yearsExperienceInt =-1;
	private String location;
	private Long jobId;
	private Long countDegreeId;
	private String degreeName;
	private String iKodaDegreeName;
	private Long iKodaDegreeId;
	Long uniqueJobId;
	
	@Column(length = 1000)
	private String skill00;
	
	@Column(length = 1000)
	private String skill01;
	
	@Column(length = 1000)
	private String skill02;
	@Column(length = 1000)
	private String skill03;
	@Column(length = 1000)
	private String skill04;
	@Column(length = 1000)
	private String skill05;
	@Column(length = 1000)
	private String skill06;
	@Column(length = 1000)
	private String skill07;
	@Column(length = 1000)
	private String skill08;
	@Column(length = 1000)
	private String skill09;
	@Column(length = 1000)
	private String skill10;
	@Column(length = 1000)
	private String skill11;
	@Column(length = 1000)
	private String skill12;
	@Column(length = 1000)
	private String skill13;
	@Column(length = 1000)
	private String skill14;
	@Column(length = 1000)
	private String skill15;
	@Column(length = 1000)
	private String skill16;
	@Column(length = 1000)
	private String skill17;
	@Column(length = 1000)
	private String skill18;
	@Column(length = 1000)
	private String skill19;
	@Column(length = 1000)
	private String skill20;
	@Column(length = 1000)
	private String skill21;
	@Column(length = 1000)
	private String skill22;
	@Column(length = 1000)
	private String skill23;
	@Column(length = 1000)
	private String skill24;
	@Column(length = 1000)
	private String skill25;

	@Column(length = 1000)
	private String skill26;

	@Column(length = 1000)
	private String skill27;

	@Column(length = 1000)
	private String skill28;

	@Column(length = 1000)
	private String skill29;
	
	@Column(length = 1000)
	private String skill30;


	

	
	@ElementCollection
	@CollectionTable(joinColumns = @JoinColumn(name = "reporting_job_id") )
	private List<String> reporting_workSkills = new ArrayList<String>();


	@CreationTimestamp
	protected Date created;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@UpdateTimestamp
	protected Date updated;

	public CPCJob()
	{
		
	}

	public String getContentSource()
	{
		return contentSource;
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

	public Integer getDetailLevel()
	{
		return detailLevel;
	}

	public Long getId()
	{
		return id;
	}

	public Long getiKodaDegreeId()
	{
		return iKodaDegreeId;
	}

	public String getiKodaDegreeName()
	{
		return iKodaDegreeName;
	}

	public Long getJobId()
	{
		return jobId;
	}

	public String getJobTitle()
	{
		return jobTitle;
	}

	public String getLocation()
	{
		return location;
	}

	public String getRegion()
	{
		return region;
	}



	public List<String> getReporting_workSkills()
	{
		return reporting_workSkills;
	}

	public Integer getSalaryEndRange()
	{
		return salaryEndRange;
	}

	public Integer getSalaryStartRange()
	{
		return salaryStartRange;
	}

	public String getSkill00()
	{
		return skill00;
	}

	public String getSkill01()
	{
		return skill01;
	}

	public String getSkill02()
	{
		return skill02;
	}

	public String getSkill03()
	{
		return skill03;
	}

	public String getSkill04()
	{
		return skill04;
	}

	public String getSkill05()
	{
		return skill05;
	}

	public String getSkill06()
	{
		return skill06;
	}

	public String getSkill07()
	{
		return skill07;
	}

	public String getSkill08()
	{
		return skill08;
	}

	public String getSkill09()
	{
		return skill09;
	}

	public String getSkill10()
	{
		return skill10;
	}

	public String getSkill11()
	{
		return skill11;
	}

	public String getSkill12()
	{
		return skill12;
	}

	public String getSkill13()
	{
		return skill13;
	}

	public String getSkill14()
	{
		return skill14;
	}

	public String getSkill15()
	{
		return skill15;
	}
	
	public String getSkill16()
	{
		return skill16;
	}



	public String getSkill17()
	{
		return skill17;
	}

	public String getSkill18()
	{
		return skill18;
	}

	public String getSkill19()
	{
		return skill19;
	}



	public String getSkill20()
	{
		return skill20;
	}

	public String getSkill21()
	{
		return skill21;
	}

	public String getSkill22()
	{
		return skill22;
	}

	public String getSkill23()
	{
		return skill23;
	}

	public String getSkill24()
	{
		return skill24;
	}

	public String getSkill25()
	{
		return skill25;
	}

	public String getSkill26()
	{
		return skill26;
	}

	public String getSkill27()
	{
		return skill27;
	}

	public String getSkill28()
	{
		return skill28;
	}

	public String getSkill29()
	{
		return skill29;
	}

	public String getSkill30()
	{
		return skill30;
	}

	public List<String> getSkills()
	{
		List <String>skills=new ArrayList<String>();
		skills.add(skill00);
		skills.add(skill01);
		skills.add(skill02);
		skills.add(skill03);
		skills.add(skill04);
		skills.add(skill05);
		skills.add(skill06);
		skills.add(skill07);
		skills.add(skill08);
		skills.add(skill09);
		skills.add(skill10);
		skills.add(skill11);
		skills.add(skill12);
		skills.add(skill13);
		skills.add(skill14);
		skills.add(skill15);
		skills.add(skill16);
		skills.add(skill17);
		skills.add(skill18);
		skills.add(skill19);
		skills.add(skill20);
		skills.add(skill21);
		skills.add(skill22);
		skills.add(skill23);
		skills.add(skill24);
		skills.add(skill25);
		skills.add(skill26);
		skills.add(skill27);
		skills.add(skill28);
		skills.add(skill29);
		skills.add(skill30);

		PLog.getRLogger().debug("skill00: "+skill00);

		

		return skills;
		
	}

	public Long getUniqueJobId()
	{
		return uniqueJobId;
	}

	public Date getUpdated()
	{
		return updated;
	}

	public Integer getYearsExperienceInt()
	{
		return yearsExperienceInt;
	}

	public void ignoreSkill(String s)
	{

		//PLog.getRLogger().debug(skills);

		
			if(null!=skill00&&skill00.toUpperCase().equals(s.toUpperCase()))
			{
				skill00="";
				return;
			}
			else if(null!=skill01&&skill01.toUpperCase().equals(s.toUpperCase()))
			{
				skill01="";
				return;
			}
			else if(null!=skill02&&skill02.toUpperCase().equals(s.toUpperCase()))
			{
				skill02="";
				return;
			}
			else if(null!=skill03&&skill03.toUpperCase().equals(s.toUpperCase()))
			{
				skill03="";
				return;
			}
			else if(null!=skill04&&skill04.toUpperCase().equals(s.toUpperCase()))
			{
				skill04="";
				return;
			}
			else if(null!=skill05&&skill05.toUpperCase().equals(s.toUpperCase()))
			{
				skill05="";
				return;
			}
			else if(null!=skill06&&skill06.toUpperCase().equals(s.toUpperCase()))
			{
				skill06="";
				return;
			}
			else if(null!=skill07&&skill07.toUpperCase().equals(s.toUpperCase()))
			{
				skill07="";
				return;
			}
			else if(null!=skill08&&skill08.toUpperCase().equals(s.toUpperCase()))
			{
				skill08="";
				return;
			}
			else if(null!=skill09&&skill09.toUpperCase().equals(s.toUpperCase()))
			{
				skill09="";
				return;
			}
			else if(null!=skill10&&skill10.toUpperCase().equals(s.toUpperCase()))
			{
				skill10="";
				return;
			}
			else if(null!=skill11&&skill11.toUpperCase().equals(s.toUpperCase()))
			{
				skill11="";
				return;
			}
			else if(null!=skill12&&skill12.toUpperCase().equals(s.toUpperCase()))
			{
				skill12="";
				return;
			}
			else if(null!=skill13&&skill13.toUpperCase().equals(s.toUpperCase()))
			{
				skill13="";
				return;
			}
			else if(null!=skill14&&skill14.toUpperCase().equals(s.toUpperCase()))
			{
				skill14="";
				return;
			}
			else if(null!=skill15&&skill15.toUpperCase().equals(s.toUpperCase()))
			{
				skill15="";
				return;
			}
			else if(null!=skill16&&skill16.toUpperCase().equals(s.toUpperCase()))
			{
				skill16="";
				return;
			}
			else if(null!=skill17&&skill17.toUpperCase().equals(s.toUpperCase()))
			{
				skill17="";
				return;
			}
			else if(null!=skill18&&skill18.toUpperCase().equals(s.toUpperCase()))
			{
				skill18="";
				return;
			}
			else if(null!=skill19&&skill19.toUpperCase().equals(s.toUpperCase()))
			{
				skill19="";
				return;
			}
			else if(null!=skill20&&skill20.toUpperCase().equals(s.toUpperCase()))
			{
				skill20="";
				return;
			}
			else if(null!=skill21&&skill21.toUpperCase().equals(s.toUpperCase()))
			{
				skill21="";
				return;
			}
			else if(null!=skill22&&skill22.toUpperCase().equals(s.toUpperCase()))
			{
				skill22="";
				return;
			}
			else if(null!=skill23&&skill23.toUpperCase().equals(s.toUpperCase()))
			{
				skill23="";
				return;
			}
			else if(null!=skill24&&skill24.toUpperCase().equals(s.toUpperCase()))
			{
				skill24="";
				return;
			}
			else if(null!=skill25&&skill25.toUpperCase().equals(s.toUpperCase()))
			{
				skill25="";
				return;
			}
			else if(null!=skill26&&skill26.toUpperCase().equals(s.toUpperCase()))
			{
				skill26="";
				return;
			}
			else if(null!=skill27&&skill27.toUpperCase().equals(s.toUpperCase()))
			{
				skill27="";
				return;
			}
			else if(null!=skill28&&skill28.toUpperCase().equals(s.toUpperCase()))
			{
				skill28="";
				return;
			}
			else if(null!=skill29&&skill29.toUpperCase().equals(s.toUpperCase()))
			{
				skill29="";
				return;
			}
			else if(null!=skill30&&skill30.toUpperCase().equals(s.toUpperCase()))
			{
				skill30="";
				return;
			}
	}

	public void setContentSource(String contentSource)
	{
		this.contentSource = contentSource;
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

	public void setDetailLevel(Integer detailLevel)
	{
		this.detailLevel = detailLevel;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setiKodaDegreeId(Long iKodaDegreeId)
	{
		this.iKodaDegreeId = iKodaDegreeId;
	}

	public void setiKodaDegreeName(String iKodaDegreeName)
	{
		this.iKodaDegreeName = iKodaDegreeName;
	}

	public void setJobId(Long jobId)
	{
		this.jobId = jobId;
	}

	public void setJobTitle(String jobTitle)
	{
		this.jobTitle = jobTitle;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public void setReporting_workSkills(List<String> reporting_workSkills)
	{
		this.reporting_workSkills = reporting_workSkills;
	}

	public void setSalaryEndRange(Integer salaryEndRange)
	{
		this.salaryEndRange = salaryEndRange;
	}

	public void setSalaryStartRange(Integer salaryStartRange)
	{
		this.salaryStartRange = salaryStartRange;
	}

	public void setSkill00(String skill00)
	{
		this.skill00 = skill00;
	}

	public void setSkill01(String skill01)
	{
		this.skill01 = skill01;
	}

	public void setSkill02(String skill02)
	{
		this.skill02 = skill02;
	}

	public void setSkill03(String skill03)
	{
		this.skill03 = skill03;
	}

	public void setSkill04(String skill04)
	{
		this.skill04 = skill04;
	}

	public void setSkill05(String skill05)
	{
		this.skill05 = skill05;
	}

	public void setSkill06(String skill06)
	{
		this.skill06 = skill06;
	}

	public void setSkill07(String skill07)
	{
		this.skill07 = skill07;
	}

	public void setSkill08(String skill08)
	{
		this.skill08 = skill08;
	}

	public void setSkill09(String skill09)
	{
		this.skill09 = skill09;
	}

	public void setSkill10(String skill10)
	{
		this.skill10 = skill10;
	}

	public void setSkill11(String skill11)
	{
		this.skill11 = skill11;
	}

	public void setSkill12(String skill12)
	{
		this.skill12 = skill12;
	}

	public void setSkill13(String skill13)
	{
		this.skill13 = skill13;
	}

	public void setSkill14(String skill14)
	{
		this.skill14 = skill14;
	}

	public void setSkill15(String skill15)
	{
		this.skill15 = skill15;
	}

	public void setSkill16(String skill16)
	{
		this.skill16 = skill16;
	}

	public void setSkill17(String skill17)
	{
		this.skill17 = skill17;
	}

	public void setSkill18(String skill18)
	{
		this.skill18 = skill18;
	}

	public void setSkill19(String skill19)
	{
		this.skill19 = skill19;
	}

	public void setSkill20(String skill20)
	{
		this.skill20 = skill20;
	}

	public void setSkill21(String skill21)
	{
		this.skill21 = skill21;
	}

	public void setSkill22(String skill22)
	{
		this.skill22 = skill22;
	}

	public void setSkill23(String skill23)
	{
		this.skill23 = skill23;
	}

	public void setSkill24(String skill24)
	{
		this.skill24 = skill24;
	}

	public void setSkill25(String skill25)
	{
		this.skill25 = skill25;
	}

	public void setSkill26(String skill26)
	{
		this.skill26 = skill26;
	}

	public void setSkill27(String skill27)
	{
		this.skill27 = skill27;
	}

	public void setSkill28(String skill28)
	{
		this.skill28 = skill28;
	}

	public void setSkill29(String skill29)
	{
		this.skill29 = skill29;
	}

	public void setSkill30(String skill30)
	{
		this.skill30 = skill30;
	}

	
	public void setSkills(List<String> skills)
	{
		int i =0;
		//PLog.getRLogger().debug(skills);
		for(String s:skills)
		{
		
			if(i==0)
			{
				skill00=s;
			}
			else if(i==1)
			{
				skill01=s;
			}
			else if(i==2)
			{
				skill02=s;
			}
			else if(i==3)
			{
				skill03=s;
			}
			else if(i==4)
			{
				skill04=s;
			}
			else if(i==5)
			{
				skill05=s;
			}
			else if(i==6)
			{
				skill06=s;
			}
			else if(i==7)
			{
				skill07=s;
			}
			else if(i==8)
			{
				skill08=s;
			}
			else if(i==9)
			{
				skill09=s;
			}
			else if(i==10)
			{
				skill10=s;
			}
			else if(i==11)
			{
				skill11=s;
			}
			else if(i==12)
			{
				skill12=s;
			}
			else if(i==13)
			{
				skill13=s;
			}
			else if(i==14)
			{
				skill14=s;
			}
			else if(i==15)
			{
				skill15=s;
			}
			else if(i==16)
			{
				skill16=s;
			}
			else if(i==17)
			{
				skill17=s;
			}
			else if(i==18)
			{
				skill18=s;
			}
			else if(i==19)
			{
				skill19=s;
			}
			else if(i==20)
			{
				skill20=s;
			}
			else if(i==21)
			{
				skill21=s;
			}
			else if(i==22)
			{
				skill22=s;
			}
			else if(i==23)
			{
				skill23=s;
			}
			else if(i==24)
			{
				skill24=s;
			}
			else if(i==25)
			{
				skill25=s;
			}
			else if(i==26)
			{
				skill26=s;
			}
			else if(i==27)
			{
				skill27=s;
			}
			else if(i==28)
			{
				skill28=s;
			}
			else if(i==29)
			{
				skill29=s;
			}
			else if(i==30)
			{
				skill30=s;
			}

			i++;
			if(i==30)
			{
				break;
			}
		}
		//PLog.getRLogger().debug(skill01+"   |||   "+skill02);

	}
	
	
	
	
	
	public void setUniqueJobId(Long uniqueJobId)
	{
		this.uniqueJobId = uniqueJobId;
	}

	public void setUpdated(Date updated)
	{
		this.updated = updated;
	}

	public void setYearsExperienceInt(Integer inyearsExperienceInt)
	{
		if(null==inyearsExperienceInt)
		{
			return;
		}
		this.yearsExperienceInt = inyearsExperienceInt;
	}

}
