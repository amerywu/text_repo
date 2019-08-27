package ikoda.persistence.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.persistence.application.PLog;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.dao.JobReportingByIkodaDegreeDaoInterface;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.reporting.CPCJob;
import ikoda.persistence.model.reporting.CPCPayByJobTitleAndDegree;
import ikoda.persistence.model.reporting.CPCSalaryByDegree;

@Service("jobReportingService")
public class JobReportingByIkodaDegreeServiceImpl implements JobReportingByIkodaDegreeService
{

	private static Map<String, Long> IkodaDegreeIdMap = new HashMap<String, Long>();

	@Autowired
	private JobReportingByIkodaDegreeDaoInterface jobReportingDao;

	public JobReportingByIkodaDegreeServiceImpl()
	{
		// TODO Auto-generated constructor stub
	}

	
	public void filterCPCJobsByAnyDegree(List<CPCPayByJobTitleAndDegree> displayList)
	{
		List<CPCPayByJobTitleAndDegree> tempList = new ArrayList<CPCPayByJobTitleAndDegree>();
		PLog.getLogger().debug("displayList "+displayList.size());
		for (CPCPayByJobTitleAndDegree cpcjbad : displayList)
		{
			PLog.getLogger().debug("job title  "+cpcjbad.getJobTitle().toUpperCase());
			for (int i = 0; i < InterfaceReportingFilters.excludeAnyDegreeList.length; i++)
			{
				if (cpcjbad.getJobTitle().toUpperCase().contains(InterfaceReportingFilters.excludeAnyDegreeList[i].toUpperCase()))
				{
					PLog.getLogger().debug("removing");
					tempList.add(cpcjbad);
				}
			}
		}
		displayList.removeAll(tempList);
		PLog.getLogger().debug("displayList "+displayList.size());
	}
	
	
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public int deleteCPCPayByJobTitleAndDegree(Long countDegreeId, Long uniqueJobId, String region,Long iKodaDegreeId, String degreeLevel)
			throws PersistenceException
	{
		return jobReportingDao.deleteCPCPayByJobTitleAndDegree(countDegreeId, uniqueJobId, region, iKodaDegreeId,degreeLevel);
	}
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public int deleteCPCSalaryByDegree(Long countDegreeId, String region, String degreeLevel) throws PersistenceException
	{
		return this.jobReportingDao.deleteCPCSalaryByDegree(countDegreeId, region,degreeLevel);
	}
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CPCSalaryByDegree> getAllCpcSalaryByDegrees () throws PersistenceException
	{
		return jobReportingDao.getAllCpcSalaryByBachelorLevelDegrees();
	}
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public CPCJob getCPCJob(Long jobId) throws PersistenceException
	{
		return jobReportingDao.getCPCJob(jobId);
	}
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<CPCSalaryByDegree> getAllCpcSalaryByDegreesForRegion (String region) throws PersistenceException
	{
		return jobReportingDao.getAllCpcSalaryByBachelorLevelDegreesForRegion(region);
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CPCJob> getCPCJob(Long ujId, Long countDegreeId) throws PersistenceException
	{
		return jobReportingDao.getCPCJob(ujId, countDegreeId);
	}
	
	
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CPCJob getCPCJob(Long jobId, Long countDegreeId, Long uniqueJobId, String region, Long ikodaDegreeId) throws PersistenceException
	{
		return jobReportingDao.getCPCJob(jobId, countDegreeId, uniqueJobId, region, ikodaDegreeId);
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<CPCJob> getCPCJob(Long ujId, Long countDegreeId, Long ikodaDegreeId, String region) throws PersistenceException
	{
		return jobReportingDao.getCPCJob(ujId, countDegreeId, ikodaDegreeId,region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CPCPayByJobTitleAndDegree getCPCPayByJobTitleAndDegree(Long countDegreeId, Long uniqueJobId, String region, Long iKodaDegreeId)
			throws PersistenceException
	{
		return this.jobReportingDao.getCPCPayByJobTitleAndDegree(countDegreeId, uniqueJobId, region,  iKodaDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByCountDegreeIdAndRegion(Long countDegreeId, String region)
	{
		return this.jobReportingDao.getCPCPayByJobTitleAndDegreeByCountDegreeIdAndRegion(countDegreeId, region);
	}
	
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByIkodaDegreeName(String ikodaDegreeName)
	{
		return this.jobReportingDao.getCPCPayByJobTitleAndBachelorLevelDegreeByIkodaDegreeName(ikodaDegreeName);
	}


	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByIkodaDegreeName(String ikodaDegreeName, String region) throws PersistenceException
	{
		return jobReportingDao.getCPCPayByJobTitleAndBachelorLevelDegreeByIkodaDegreeName(ikodaDegreeName, region);
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CPCSalaryByDegree getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(Long countDegreeId, String region)
			throws PersistenceException
	{
		return jobReportingDao.getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(countDegreeId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CPCSalaryByDegree getCPCSalaryByBachelorLevelDegreeBySpecifiedDegreeNameAndRegion(String degreeName, String region)
			throws PersistenceException
	{
		return jobReportingDao.getCPCSalaryByBachelorLevelDegreeBySpecifiedDegreeNameAndRegion(degreeName, region);
	}
	
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<CPCSalaryByDegree> getCpcSalaryByDegreesByRegion (String region) throws PersistenceException
	{
		return this.jobReportingDao.getCpcSalaryByBachelorLevelDegreesByRegion(region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public Long getIkodaDegreeIdByIkodaDegreeName(String degreeName) throws PersistenceException
	{

		if (IkodaDegreeIdMap.size() == 0)
		{
			this.getIkodaDegrees();
		}
		return IkodaDegreeIdMap.get(degreeName.toUpperCase());
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<IkodaDegree> getIkodaDegrees() throws PersistenceException
	{
		List<IkodaDegree> degrees = jobReportingDao.getIkodaDegrees();

		if (IkodaDegreeIdMap.size() == 0)
		{
			mapIkodaDegrees(degrees);
		}

		return degrees;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeByIkodaDegreeId(Long ikodaDegreeId)
			throws PersistenceException
	{
		return jobReportingDao.getIkodaDegreeSpecifiedDegreeByIkodaDegreeId(ikodaDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeBySpecifiedDegreeId(Long specifiedDegreeId)
			throws PersistenceException
	{
		return jobReportingDao.getIkodaDegreeSpecifiedDegreeBySpecifiedDegreeId(specifiedDegreeId);
	}

	@Override
	public JobReportingByIkodaDegreeDaoInterface getJobReportingDao()
	{
		return jobReportingDao;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		return this.jobReportingDao.getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(countDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(Long countDegreeId,
			int count) throws PersistenceException
	{
		return jobReportingDao.getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(countDegreeId, count);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegree> getValueSalaryByDegreeBySpecifiedDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		return this.jobReportingDao.getValueSalaryByDegreeBySpecifiedDegreeId(countDegreeId);
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private void mapIkodaDegrees(List<IkodaDegree> ikodaDegrees)
	{
		IkodaDegreeIdMap.clear();
		for (IkodaDegree ikd : ikodaDegrees)
		{
			IkodaDegreeIdMap.put(ikd.getIkodaDegreeName().toUpperCase(), ikd.getId());
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCPCJob(CPCJob cpcjob) throws PersistenceException
	{
		jobReportingDao.saveCPCJob(cpcjob);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCPCPayByJobTitleAndDegree(CPCPayByJobTitleAndDegree jbd) throws PersistenceException
	{
		jobReportingDao.saveCPCPayByJobTitleAndDegree(jbd);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCPCSalaryByDegree(CPCSalaryByDegree sbd) throws PersistenceException
	{
		jobReportingDao.saveCPCSalaryByDegree(sbd);
	}

	@Override
	public void setJobReportingDao(JobReportingByIkodaDegreeDaoInterface jobReportingDao)
	{
		this.jobReportingDao = jobReportingDao;
	}

	public void updateCPCJob(CPCJob cpcjob) throws PersistenceException
	{
		jobReportingDao.updateCPCJob(cpcjob);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateCPCPayByJobTitleAndDegree(CPCPayByJobTitleAndDegree jbd) throws PersistenceException
	{
		jobReportingDao.updateCPCPayByJobTitleAndDegree(jbd);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateCPCSalaryByDegree(CPCSalaryByDegree sbd) throws PersistenceException
	{
		jobReportingDao.updateCPCSalaryByDegree(sbd);
	}
}
