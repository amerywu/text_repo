package ikoda.persistence.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import ikoda.persistence.application.PersistenceException;
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

public interface JobAnalysisDaoInterface
{

	public List<CountDegree> getAllCountDegrees() throws PersistenceException;
	public List<CountDegreeJob> getCountDegreeJobByJobId(Long jobId) throws PersistenceException;
	public List<UniqueJob> getAllUniqueJobs() throws PersistenceException;
	public List<CountDegreeJob> getCountDegreeJobByCpuntDegreeId(Long cdid) throws PersistenceException;
	public List<ValueSalaryByDegree> getAllValueSalaryByDegrees() throws PersistenceException;
	
	public void saveCountDegreeJob(CountDegreeJob cdj) throws PersistenceException;
	
	public CountDegree getCountDegree(String specifiedDegreeName, String degreeLevel) throws PersistenceException;

	public CountDegree getCountDegreeById(Long id) throws PersistenceException;

	public Long getCountJobListWithinIdRange(Long minId, Long maxId, int minDetailLevel) throws PersistenceException;

	public List<CountJobTitle> getCountJobTitle(int minimumCount) throws PersistenceException;

	public CountJobTitle getCountJobTitle(Long uniqueJobId) throws PersistenceException;

	public CountJobTitleByDegreeName getCountJobTitleByDegreeName(Long uniqueJobId, Long countDegreeId)
			throws PersistenceException;

	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNameByDegreeId(Long countDegreeId)
			throws PersistenceException;

	public CountJobTitleByDegreeName getCountJobTitleByDegreeNameById(Long id) throws PersistenceException;

	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNamesByDegreeName(Long countDegreeId)
			throws PersistenceException;

	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNamesByJobTitle(Long uniqueJobId)
			throws PersistenceException;

	public CountJobTitle getCountJobTitleById(Long id) throws PersistenceException;

	public List<DatabaseDescriptors> getDatabaseDescriptors() throws PersistenceException;

	public IkodaDegree getIkodaDegree(String ikodaDegreeName) throws PersistenceException;

	public IkodaDegree getIkodaDegreeById(Long id) throws PersistenceException;

	public IkodaDegreeSpecifiedDegree getIkodaDegreeSpecifiedDegreeById(Long id) throws PersistenceException;

	public List<Job> getJobsByUniqueJobId(Long ujId) throws PersistenceException;

	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel) throws PersistenceException;

	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel, String degree) throws PersistenceException;

	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel, String degree, String region)
			throws PersistenceException;

	public List<Integer> getListOfMaxSalariesFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException;

	public List<Integer> getListOfMinSalariesFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException;

	public Long getMaxJobId() throws PersistenceException;

	public List<Integer> getMaxSalaryFromJob(List<Long> ujIds, String region) throws PersistenceException;

	public List<Integer> getMinSalaryFromJob(List<Long> ujIds, String region) throws PersistenceException;

	public List<Job> getMinSalaryFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException;

	public List<Job> getRandomJobList(int jobCount) throws PersistenceException;

	public List<Job> getRandomJobListWithinIdRange(int jobCount, Long minId, Long maxId, int minDetailLevel)
			throws PersistenceException;

	SessionFactory getSessionFactoryjobs();

	public UniqueJob getUniqueJob(String uniqueJobName) throws PersistenceException;

	public UniqueJob getUniqueJobById(Long id) throws PersistenceException;

	public UnitTestSubject getUnitTestSubject(Long id) throws PersistenceException;

	public List<UnitTestSubject> getUnitTestSubjects() throws PersistenceException;

	public ValueSalaryByDegree getValueSalaryByDegree(Long countDegreeId, String region) throws PersistenceException;

	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleByDegree(Long countDegreeId, String region)
			throws PersistenceException;

	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId)
			throws PersistenceException;

	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId,
			int count) throws PersistenceException;

	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId,
			String region) throws PersistenceException;

	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleById(Long id) throws PersistenceException;

	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(Long uniqueJobId,
			Long countDegreeId, String region) throws PersistenceException;

	public ValueSalaryByDegree getValueSalaryByDegreeByDegreeId(Long countdegreeid, String region)
			throws PersistenceException;

	public ValueSalaryByDegree getValueSalaryByDegreeById(Long id) throws PersistenceException;

	public ValueSalaryByJobTitle getValueSalaryByJobTitle(Long uniqueJobId, String region) throws PersistenceException;

	public ValueSalaryByJobTitle getValueSalaryByJobTitleById(Long id) throws PersistenceException;

	public ValueSalaryByJobTitle getValueSalaryByJobTitleByUniqueJobId(Long ujId, String region)
			throws PersistenceException;

	public void saveCountDegree(CountDegree countDegree) throws PersistenceException;

	public void saveCountJobTitle(CountJobTitle countJobTitle) throws PersistenceException;

	public void saveCountJobTitleByDegreeName(CountJobTitleByDegreeName countJobTitleByDegreeName)
			throws PersistenceException;

	public void saveDatabaseDescriptors(DatabaseDescriptors dd) throws PersistenceException;

	public void saveIkodaDegreee(IkodaDegree ikodaDegree) throws PersistenceException;

	public void saveIkodaDegreeSpecifiedDegree(IkodaDegreeSpecifiedDegree ikodaDegreeSpecifiedDegree)
			throws PersistenceException;

	public void saveJob(Job job) throws PersistenceException;

	public void saveUniqueJob(UniqueJob uniqueJob) throws PersistenceException;

	void saveUnitTestSubject(UnitTestSubject newUnitTestSubject) throws PersistenceException;

	public void saveValueSalaryByDegree(ValueSalaryByDegree valueSalaryByDegree) throws PersistenceException;

	public void saveValueSalaryByDegreeAndJobTitle(ValueSalaryByDegreeAndJobTitle valueSalaryByDegreeAndJobTitle)
			throws PersistenceException;

	public void saveValueSalaryByJobTitle(ValueSalaryByJobTitle valueSalaryByJobTitle) throws PersistenceException;

	///////////////////////////////////////////////////////////////////////////////////////

	void setSessionFactoryjobs(SessionFactory sessionFactory);

	public void updateCountDegree(CountDegree countDegree) throws PersistenceException;

	public void updateCountJobTitle(CountJobTitle countJobTitle) throws PersistenceException;

	public void updateCountJobTitleByDegreeName(CountJobTitleByDegreeName countJobTitleByDegreeName)
			throws PersistenceException;

	public void updateIkodaDegree(IkodaDegree ikodaDegree) throws PersistenceException;

	public void updateIkodaDegreeSpecifiedDegree(IkodaDegreeSpecifiedDegree ikodaDegreeSpecifiedDegree)
			throws PersistenceException;

	public void updateUniqueJob(UniqueJob uniqueJob) throws PersistenceException;

	public void updateValueSalaryByDegree(ValueSalaryByDegree valueSalaryByDegree) throws PersistenceException;

	public void updateValueSalaryByDegreeAndJobTitle(ValueSalaryByDegreeAndJobTitle valueSalaryByDegreeAndJobTitle)
			throws PersistenceException;

	public void updateValueSalaryByJobTitle(ValueSalaryByJobTitle valueSalaryByJobTitle) throws PersistenceException;

	public PossibleTokenCount getPossibleTokenCount(String tokenType, String value) throws PersistenceException;
	public void updatePossibleTokenCount(PossibleTokenCount possibleTokenCount) throws PersistenceException;
	public void savePossibleTokenCount(PossibleTokenCount possibleTokenCount) throws PersistenceException;
}