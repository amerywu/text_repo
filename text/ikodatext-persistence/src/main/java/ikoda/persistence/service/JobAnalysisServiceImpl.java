package ikoda.persistence.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.persistence.application.PLog;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.dao.JobAnalysisDaoInterface;
import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.CountDegreeJob;
import ikoda.persistence.model.CountJobTitle;
import ikoda.persistence.model.CountJobTitleByDegreeName;
import ikoda.persistence.model.DatabaseDescriptors;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.PossibleTokenCount;
import ikoda.persistence.model.UniqueJob;
import ikoda.persistence.model.UnitTestSubject;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.ValueSalaryByJobTitle;
import ikoda.utils.SSm;

@Service("jobAnalysisService")
public class JobAnalysisServiceImpl implements JobAnalysisService
{

	@Autowired
	private JobAnalysisDaoInterface jobAnalysisDao;

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Long> getAllCountDegreeIds() throws PersistenceException
	{
		List<CountDegree> countDegreeList = jobAnalysisDao.getAllCountDegrees();
		List<Long> ids = new ArrayList<Long>();
		for (CountDegree cd : countDegreeList)
		{
			ids.add(new Long(cd.getId().longValue()));
		}
		return ids;
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountDegreeJob> getCountDegreeJobByJobId(Long jobId) throws PersistenceException
	{
		return jobAnalysisDao.getCountDegreeJobByJobId(jobId);
	}
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountDegreeJob> getCountDegreeJobByCpuntDegreeId(Long cdid) throws PersistenceException
	{
		return jobAnalysisDao.getCountDegreeJobByCpuntDegreeId(cdid);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountDegree> getAllCountDegrees() throws PersistenceException
	{
		return jobAnalysisDao.getAllCountDegrees();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegree> getAllValueSalaryByDegrees() throws PersistenceException
	{
		return jobAnalysisDao.getAllValueSalaryByDegrees();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Long> getAlUniqueJobIds()
	{
		try
		{
			List<Long> uniqueJobIds = new ArrayList<Long>();
			PLog.getChoresLogger().debug("getAlUniqueJobIds");
			List<CountJobTitle> cjtList =jobAnalysisDao.getCountJobTitle(4);
			PLog.getChoresLogger().debug("got cjtList of size "+cjtList.size());
			for (CountJobTitle cjt : cjtList)
			{
				uniqueJobIds.add(new Long(cjt.getUniqueJob().getId().longValue()));
			}
			return uniqueJobIds;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return new ArrayList<Long>();
		}
	}
	
	

	

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CountDegree getCountDegree(String specifiedDegreeName, String degreeLevel) throws PersistenceException
	{
		if(!CountDegree.isValidDegreelevel(degreeLevel))
		{
			throw new PersistenceException("Invalid DegreeLevel: "+degreeLevel);
		}
		return jobAnalysisDao.getCountDegree(specifiedDegreeName,  degreeLevel);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updatePossibleTokenCount(PossibleTokenCount possibleTokenCount) throws PersistenceException
	{
		jobAnalysisDao.updatePossibleTokenCount(possibleTokenCount);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void savePossibleTokenCount(PossibleTokenCount possibleTokenCount) throws PersistenceException
	{
		jobAnalysisDao.savePossibleTokenCount(possibleTokenCount);
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public PossibleTokenCount getPossibleTokenCount(String tokenType, String value) throws PersistenceException
	{
		return jobAnalysisDao.getPossibleTokenCount(tokenType, value);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CountDegree getCountDegreeById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getCountDegreeById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public Long getCountJobListWithinIdRange(Long minId, Long maxId, int minDetailLevel) throws PersistenceException
	{
		return jobAnalysisDao.getCountJobListWithinIdRange(minId, maxId, minDetailLevel);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CountJobTitle getCountJobTitle(Long uniqueJobId) throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitle(uniqueJobId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CountJobTitleByDegreeName getCountJobTitleByDegreeName(Long uniqueJobId, Long countDegreeId)
			throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitleByDegreeName(uniqueJobId, countDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNameByDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitleByDegreeNameByDegreeId(countDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNameByDegreeIdWithCeiling(Long countDegreeId)
			throws PersistenceException
	{
		List<CountJobTitleByDegreeName> list1 = jobAnalysisDao.getCountJobTitleByDegreeNameByDegreeId(countDegreeId);
		List<CountJobTitleByDegreeName> templist = new ArrayList();
		templist.addAll(list1);
		if (templist.size() > 750)
		{
			PLog.getLogger().info("list size is big " + templist.size());
			int cut = templist.size() - 750;
			PLog.getLogger().info("Trimming list by  " + cut);
			List<CountJobTitleByDegreeName> toRemove = new ArrayList<CountJobTitleByDegreeName>();
			for (int i = 0; i < cut; i++)
			{
				int index = ThreadLocalRandom.current().nextInt(templist.size());
				PLog.getLogger().info("index " + index);
				toRemove.add(templist.get(index));
			}
			PLog.getLogger().info("Trimming list by toRemove " + toRemove.size());
			templist.removeAll(toRemove);
			PLog.getLogger().info("List is " + templist.size());
		}
		return templist;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CountJobTitleByDegreeName getCountJobTitleByDegreeNameById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitleByDegreeNameById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNamesByDegreeName(Long countDegreeId)
			throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitleByDegreeNamesByDegreeName(countDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNamesByJobTitle(Long uniqueJobId)
			throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitleByDegreeNamesByJobTitle(uniqueJobId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public CountJobTitle getCountJobTitleById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getCountJobTitleById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<DatabaseDescriptors> getDatabaseDescriptors() throws PersistenceException
	{
		return jobAnalysisDao.getDatabaseDescriptors();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public IkodaDegree getIkodaDegree(String ikodaDegreeName) throws PersistenceException
	{
		return jobAnalysisDao.getIkodaDegree(ikodaDegreeName);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public IkodaDegree getIkodaDegreeById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getIkodaDegreeById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public IkodaDegreeSpecifiedDegree getIkodaDegreeSpecifiedDegreeById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getIkodaDegreeSpecifiedDegreeById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ikoda.persistence.service.JobAnalysisServiceInterface#getJobAnalysisDao()
	 */
	@Override
	public JobAnalysisDaoInterface getJobAnalysisDao()
	{
		return jobAnalysisDao;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Job> getJobsByUniqueJobId(Long ujId) throws PersistenceException
	{
		return jobAnalysisDao.getJobsByUniqueJobId(ujId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel) throws PersistenceException
	{
		return jobAnalysisDao.getJobsByUniqueJobId(ujId, detailLevel);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel, String degree) throws PersistenceException
	{
		return jobAnalysisDao.getJobsByUniqueJobId(ujId, detailLevel, degree);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel, String degree, String region)
			throws PersistenceException
	{
		return jobAnalysisDao.getJobsByUniqueJobId(ujId, detailLevel, degree, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Integer> getListOfMaxSalariesFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException
	{
		return jobAnalysisDao.getListOfMaxSalariesFromJob(ujIds, region, degreeName);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Integer> getListOfMinSalariesFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException
	{
		return jobAnalysisDao.getListOfMinSalariesFromJob(ujIds, region, degreeName);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public Long getMaxJobId() throws PersistenceException
	{
		return jobAnalysisDao.getMaxJobId();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Integer> getMaxSalaryFromJob(List<Long> ujIds, String region) throws PersistenceException
	{
		return jobAnalysisDao.getMaxSalaryFromJob(ujIds, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Integer> getMinSalaryFromJob(List<Long> ujIds, String region) throws PersistenceException
	{
		return jobAnalysisDao.getMinSalaryFromJob(ujIds, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Job> getMinSalaryFromJob(List<Long> ujIds, String region, String degreeName) throws PersistenceException
	{
		return jobAnalysisDao.getMinSalaryFromJob(ujIds, region, degreeName);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<Job> getRandomJobListWithinIdRange(int jobCount, Long minId, Long maxId, int minDetailLevel)
			throws PersistenceException
	{
		return jobAnalysisDao.getRandomJobListWithinIdRange(jobCount, minId, maxId, minDetailLevel);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public UniqueJob getUniqueJob(String uniqueJobName) throws PersistenceException
	{
		return jobAnalysisDao.getUniqueJob(uniqueJobName);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public UniqueJob getUniqueJobById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getUniqueJobById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ikoda.persistence.service.JobAnalysisServiceInterface#getUnitTestSubject(
	 * java.lang.Long)
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public UnitTestSubject getUnitTestSubject(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getUnitTestSubject(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<UnitTestSubject> getUnitTestSubjects() throws PersistenceException
	{
		return jobAnalysisDao.getUnitTestSubjects();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByDegree getValueSalaryByDegree(Long countDegreeId, String region) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegree(countDegreeId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleByDegree(Long countDegreeId, String region)
			throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeAndJobTitleByDegree(countDegreeId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeAndJobTitleByDegreeId(countDegreeId);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId,
			int count) throws PersistenceException
	{

		return jobAnalysisDao.getValueSalaryByDegreeAndJobTitleByDegreeId(countDegreeId, count);

	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId,
			String region) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeAndJobTitleByDegreeId(countDegreeId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeAndJobTitleById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(Long uniqueJobId,
			Long countDegreeId, String region) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(uniqueJobId, countDegreeId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByDegree getValueSalaryByDegreeByDegreeId(Long countdegreeid, String region)
			throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeByDegreeId(countdegreeid, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByDegree getValueSalaryByDegreeById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByDegreeById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByJobTitle getValueSalaryByJobTitle(Long uniqueJobId, String region) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByJobTitle(uniqueJobId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByJobTitle getValueSalaryByJobTitleById(Long id) throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByJobTitleById(id);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public ValueSalaryByJobTitle getValueSalaryByJobTitleByUniqueJobId(Long ujId, String region)
			throws PersistenceException
	{
		return jobAnalysisDao.getValueSalaryByJobTitleByUniqueJobId(ujId, region);
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public boolean isCorrectLocale(String locale) throws PersistenceException
	{

		List<DatabaseDescriptors> list = jobAnalysisDao.getDatabaseDescriptors();
		for (DatabaseDescriptors dd : list)
		{
			if (dd.getDatabaseLocale().equals(locale))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public boolean isCorrectDatabaseDescriptor(String descriptor) throws PersistenceException
	{

		List<DatabaseDescriptors> list = jobAnalysisDao.getDatabaseDescriptors();
		for (DatabaseDescriptors dd : list)
		{
			PLog.getLogger().info("DB_DESCRIPTOR: "+dd.getDatabaseDescriptor());
			PLog.getLogger().info("CONFIG_DESCRIPTOR: "+descriptor);
			if (dd.getDatabaseDescriptor().equals(descriptor))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCountDegree(CountDegree countDegree) throws PersistenceException
	{
		jobAnalysisDao.saveCountDegree(countDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCountJobTitle(CountJobTitle countJobTitle) throws PersistenceException
	{
		jobAnalysisDao.saveCountJobTitle(countJobTitle);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCountJobTitleByDegreeName(CountJobTitleByDegreeName countJobTitleByDegreeName)
			throws PersistenceException
	{
		jobAnalysisDao.saveCountJobTitleByDegreeName(countJobTitleByDegreeName);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveDatabaseDescriptors(DatabaseDescriptors dd) throws PersistenceException
	{
		jobAnalysisDao.saveDatabaseDescriptors(dd);
	}

	///////////////////////////////////////////////////////////////////////////////////////

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveIkodaDegreee(IkodaDegree ikodaDegree) throws PersistenceException
	{
		jobAnalysisDao.saveIkodaDegreee(ikodaDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveIkodaDegreeSpecifiedDegree(IkodaDegreeSpecifiedDegree ikodaDegreeSpecifiedDegree)
			throws PersistenceException
	{
		jobAnalysisDao.saveIkodaDegreeSpecifiedDegree(ikodaDegreeSpecifiedDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveJob(Job job) throws PersistenceException
	{
		jobAnalysisDao.saveJob(job);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveUniqueJob(UniqueJob uniqueJob) throws PersistenceException
	{
		jobAnalysisDao.saveUniqueJob(uniqueJob);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveUnitTestSubject(UnitTestSubject tus) throws PersistenceException
	{
		jobAnalysisDao.saveUnitTestSubject(tus);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveValueSalaryByDegree(ValueSalaryByDegree valueSalaryByDegree) throws PersistenceException
	{
		jobAnalysisDao.saveValueSalaryByDegree(valueSalaryByDegree);
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveCountDegreeJob(CountDegreeJob cdj) throws PersistenceException
	{
		jobAnalysisDao.saveCountDegreeJob(cdj);
	}
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveValueSalaryByDegreeAndJobTitle(ValueSalaryByDegreeAndJobTitle valueSalaryByDegreeAndJobTitle)
			throws PersistenceException
	{
		jobAnalysisDao.saveValueSalaryByDegreeAndJobTitle(valueSalaryByDegreeAndJobTitle);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveValueSalaryByJobTitle(ValueSalaryByJobTitle valueSalaryByJobTitle) throws PersistenceException
	{
		jobAnalysisDao.saveValueSalaryByJobTitle(valueSalaryByJobTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ikoda.persistence.service.JobAnalysisServiceInterface#setJobAnalysisDao(
	 * ikoda.persistence.dao.JobAnalysisDaoInterface)
	 */
	@Override
	public void setJobAnalysisDao(JobAnalysisDaoInterface jobAnalysisDao)
	{
		this.jobAnalysisDao = jobAnalysisDao;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateCountDegree(CountDegree countDegree) throws PersistenceException
	{
		jobAnalysisDao.updateCountDegree(countDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateCountJobTitle(CountJobTitle countJobTitle) throws PersistenceException
	{
		jobAnalysisDao.updateCountJobTitle(countJobTitle);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateCountJobTitleByDegreeName(CountJobTitleByDegreeName countJobTitleByDegreeName)
			throws PersistenceException
	{
		jobAnalysisDao.updateCountJobTitleByDegreeName(countJobTitleByDegreeName);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateIkodaDegree(IkodaDegree ikodaDegree) throws PersistenceException
	{
		jobAnalysisDao.updateIkodaDegree(ikodaDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateIkodaDegreeSpecifiedDegree(IkodaDegreeSpecifiedDegree ikodaDegreeSpecifiedDegree)
			throws PersistenceException
	{
		jobAnalysisDao.updateIkodaDegreeSpecifiedDegree(ikodaDegreeSpecifiedDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateUniqueJob(UniqueJob uniqueJob) throws PersistenceException
	{
		jobAnalysisDao.updateUniqueJob(uniqueJob);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateValueSalaryByDegree(ValueSalaryByDegree valueSalaryByDegree) throws PersistenceException
	{
		jobAnalysisDao.updateValueSalaryByDegree(valueSalaryByDegree);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateValueSalaryByDegreeAndJobTitle(ValueSalaryByDegreeAndJobTitle valueSalaryByDegreeAndJobTitle)
			throws PersistenceException
	{
		jobAnalysisDao.updateValueSalaryByDegreeAndJobTitle(valueSalaryByDegreeAndJobTitle);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void updateValueSalaryByJobTitle(ValueSalaryByJobTitle valueSalaryByJobTitle) throws PersistenceException
	{
		jobAnalysisDao.updateValueSalaryByJobTitle(valueSalaryByJobTitle);
	}

}
