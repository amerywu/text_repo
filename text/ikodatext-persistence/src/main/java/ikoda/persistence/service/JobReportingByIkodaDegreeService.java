package ikoda.persistence.service;

import java.util.List;

import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.dao.JobReportingByIkodaDegreeDaoInterface;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.reporting.CPCJob;
import ikoda.persistence.model.reporting.CPCPayByJobTitleAndDegree;
import ikoda.persistence.model.reporting.CPCSalaryByDegree;

public interface JobReportingByIkodaDegreeService
{

	public int deleteCPCPayByJobTitleAndDegree(Long countDegreeId, Long uniqueJobId, String region, Long iKodaDegreeId,String degreeLevel)
			throws PersistenceException;
	public int deleteCPCSalaryByDegree(Long countDegreeId, String region,String degreeLevel) throws PersistenceException;
	public void filterCPCJobsByAnyDegree(List<CPCPayByJobTitleAndDegree> displayList);
	public List<CPCSalaryByDegree> getAllCpcSalaryByDegrees() throws PersistenceException;
	public List<CPCSalaryByDegree> getAllCpcSalaryByDegreesForRegion (String region) throws PersistenceException;
	public CPCJob getCPCJob(Long jobId) throws PersistenceException;

	public List<CPCJob> getCPCJob(Long ujId, Long countDegreeId) throws PersistenceException;
	public List<CPCJob> getCPCJob(Long ujId, Long countDegreeId, Long ikodaDegreeId, String region) throws PersistenceException;

	public CPCJob getCPCJob(Long jobId, Long countDegreeId, Long uniqueJobId, String region, Long ikodaDegreeId) throws PersistenceException;
	public CPCPayByJobTitleAndDegree getCPCPayByJobTitleAndDegree(Long countDegreeId, Long uniqueJobId, String region,Long iKodaDegreeId)
			throws PersistenceException;

	
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByCountDegreeIdAndRegion(Long countDegreeId,
			String region);
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByIkodaDegreeName(String ikodaDegreeName);
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByIkodaDegreeName(String ikodaDegreeName, String region) throws PersistenceException;;
			
    public CPCSalaryByDegree getCPCSalaryByBachelorLevelDegreeBySpecifiedDegreeNameAndRegion(String degreeName, String region)
			throws PersistenceException;;

	public CPCSalaryByDegree getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(Long countDegreeId, String region) throws PersistenceException;

	public List<CPCSalaryByDegree> getCpcSalaryByDegreesByRegion(String region) throws PersistenceException;

	public Long getIkodaDegreeIdByIkodaDegreeName(String degreeName) throws PersistenceException;

	public List<IkodaDegree> getIkodaDegrees() throws PersistenceException;

	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeByIkodaDegreeId(Long ikodaDegreeId)
			throws PersistenceException;

	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeBySpecifiedDegreeId(Long specifiedDegreeId)
			throws PersistenceException;

	public JobReportingByIkodaDegreeDaoInterface getJobReportingDao();

	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(Long countDegreeId)
			throws PersistenceException;

	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(Long countDegreeId,
			int count) throws PersistenceException;

	public List<ValueSalaryByDegree> getValueSalaryByDegreeBySpecifiedDegreeId(Long countDegreeId)
			throws PersistenceException;

	public void saveCPCJob(CPCJob cpcjob) throws PersistenceException;

	public void saveCPCPayByJobTitleAndDegree(CPCPayByJobTitleAndDegree jbd) throws PersistenceException;

	public void saveCPCSalaryByDegree(CPCSalaryByDegree sbd) throws PersistenceException;

	public void setJobReportingDao(JobReportingByIkodaDegreeDaoInterface jobReportingDao);

	public void updateCPCJob(CPCJob cpcjob) throws PersistenceException;

	public void updateCPCPayByJobTitleAndDegree(CPCPayByJobTitleAndDegree jbd) throws PersistenceException;

	public void updateCPCSalaryByDegree(CPCSalaryByDegree sbd) throws PersistenceException;

}