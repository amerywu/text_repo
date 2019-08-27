package ikoda.persistence.service;

import java.util.Random;

public class QueueObject
{

	private Long uniqueJobId;
	private Long jobId;
	private String region;
	private String degreeName;
	private Long countDegreeId;
	private Long ikodaDegreeId;
	private String iKodaDegreeName;
	
	int positionInQueue;

	public Long getCountDegreeId()
	{
		return countDegreeId;
	}

	public String getDegreeName()
	{
		return degreeName;
	}

	public Long getIkodaDegreeId()
	{
		return ikodaDegreeId;
	}

	public String getiKodaDegreeName()
	{
		return iKodaDegreeName;
	}

	public Long getJobId()
	{
		return jobId;
	}

	public int getPositionInQueue()
	{
		return positionInQueue;
	}

	public String getRegion()
	{
		return region;
	}

	public Long getUniqueJobId()
	{
		return uniqueJobId;
	}

	public void randomizeQueuePosition()
	{
		Random rand = new Random();

		int n = rand.nextInt(1000000) + 1;
		positionInQueue = n;
	}

	public void setCountDegreeId(Long countDegreeId)
	{
		this.countDegreeId = countDegreeId;
	}

	public void setDegreeName(String degreeName)
	{
		this.degreeName = degreeName;
	}

	public void setIkodaDegreeId(Long ikodaDegreeId)
	{
		this.ikodaDegreeId = ikodaDegreeId;
	}

	public void setiKodaDegreeName(String iKodaDegreeName)
	{
		this.iKodaDegreeName = iKodaDegreeName;
	}

	public void setJobId(Long jobId)
	{
		this.jobId = jobId;
	}

	public void setPositionInQueue(int positionInQueue)
	{
		this.positionInQueue = positionInQueue;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public void setUniqueJobId(Long uniqueJobId)
	{
		this.uniqueJobId = uniqueJobId;
	}
	
	

}
