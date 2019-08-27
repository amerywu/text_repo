package ikoda.persistence.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.persistence.application.PLog;
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

@Repository
public class JobAnalysisDaoImpl implements JobAnalysisDaoInterface
{

	@Autowired
	@Qualifier(value = "sessionFactoryjobs")
	SessionFactory sessionFactoryjobs;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountDegree> getAllCountDegrees() throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountDegree cd where cd.count > 2");

		List<CountDegree> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<UniqueJob> getAllUniqueJobs() throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			List<UniqueJob> result = session.createQuery("from UniqueJob").list();

			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegree> getAllValueSalaryByDegrees() throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByDegree vsbd where vsbd.count > 6");

		List<ValueSalaryByDegree> result = q.list();

		return result;
	}

	//////////////////////////////////////////////////////////
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CountDegree getCountDegree(String specifiedDegreeName, String degreeLevel) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		PLog.getLogger().debug("looking for " + specifiedDegreeName);
		Query q = session.createQuery("from CountDegree cd where cd.specifiedDegreeName like (:specifiedDegreeName) and cd.degreeLevel like (:degreeLevel)");
		q.setParameter("specifiedDegreeName", specifiedDegreeName);
		q.setParameter("degreeLevel", degreeLevel);

		List<CountDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		PLog.getLogger().debug("got " + result.get(0).getSpecifiedDegreeName());
		return result.get(0);
	}
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public PossibleTokenCount getPossibleTokenCount(String tokenType, String value) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		PLog.getLogger().debug("looking for " + value);
		Query q = session.createQuery("from PossibleTokenCount ptc where ptc.tokenType like (:tokenType) and ptc.value like (:value)");
		q.setParameter("tokenType", tokenType);
		q.setParameter("value", value);

		List<PossibleTokenCount> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}

		return result.get(0);
	}


	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CountDegree getCountDegreeById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountDegree where id=(:id)");
		q.setParameter("id", id);

		List<CountDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}
	
	
	
	

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Long getCountJobListWithinIdRange(Long minId, Long maxId, int minDetailLevel) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		Query query = session.createQuery(
				"select count(*) from Job where id > (:minId) and id < (:maxId) and detailLevel >= (:minDetailLevel)");

		query.setParameter("minId", minId);
		query.setParameter("maxId", maxId);
		query.setParameter("minDetailLevel", minDetailLevel);

		PLog.getLogger().debug(query.getQueryString());

		Long result = (Long) query.uniqueResult();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountJobTitle> getCountJobTitle(int minimumCount) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountJobTitle cjt where cjt.count > (:minimumCount)");
			q.setParameter("minimumCount", minimumCount);
			List<CountJobTitle> result = q.list();
			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CountJobTitle getCountJobTitle(Long uniqueJobId) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountJobTitle where uniqueJobId = (:uniqueJobId)");
		q.setParameter("uniqueJobId", uniqueJobId);

		List<CountJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountDegreeJob> getCountDegreeJobByJobId(Long jobId) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountDegreeJob where jobId = (:jobId)");
		q.setParameter("jobId", jobId);

		List<CountDegreeJob> result = q.list();
		
		return result;
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountDegreeJob> getCountDegreeJobByCpuntDegreeId(Long cdid) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountDegreeJob where countDegreeId = (:cdid)");
		q.setParameter("cdid", cdid);

		List<CountDegreeJob> result = q.list();
		
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CountJobTitleByDegreeName getCountJobTitleByDegreeName(Long uniqueJobId, Long countDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from CountJobTitleByDegreeName where uniqueJobId = (:uniqueJobId) and countDegreeId = (:countDegreeId)");
		q.setParameter("uniqueJobId", uniqueJobId);
		q.setParameter("countDegreeId", countDegreeId);

		List<CountJobTitleByDegreeName> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNameByDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountJobTitleByDegreeName cjtbd where countDegreeId = (:countDegreeId) and cjtbd.count>7");

			q.setParameter("countDegreeId", countDegreeId);

			List<CountJobTitleByDegreeName> result = q.list();
			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException();

		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CountJobTitleByDegreeName getCountJobTitleByDegreeNameById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountJobTitleByDegreeName where id=(:id) and countSinceMedianUpdate > 2");
		q.setParameter("id", id);

		List<CountJobTitleByDegreeName> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNamesByDegreeName(Long countDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountJobTitleByDegreeName where countDegreeId = (:countDegreeId)");

		q.setParameter("countDegreeId", countDegreeId);

		List<CountJobTitleByDegreeName> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNamesByJobTitle(Long uniqueJobId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountJobTitleByDegreeName where uniqueJobId = (:uniqueJobId)");

		q.setParameter("uniqueJobId", uniqueJobId);

		List<CountJobTitleByDegreeName> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CountJobTitle getCountJobTitleById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CountJobTitle where id=(:id)");
		q.setParameter("id", id);

		List<CountJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<DatabaseDescriptors> getDatabaseDescriptors() throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from DatabaseDescriptors");

		List<DatabaseDescriptors> result = q.list();

		return result;
	}

	//////////////////////////////////////////////////////////
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public IkodaDegree getIkodaDegree(String ikodaDegreeName) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from IkodaDegree where ikodaDegreeName like (:ikodaDegreeName)");
		q.setParameter("ikodaDegreeName", ikodaDegreeName);

		List<IkodaDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public IkodaDegree getIkodaDegreeById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from IkodaDegree where id=(:id)");
		q.setParameter("id", id);

		List<IkodaDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public IkodaDegreeSpecifiedDegree getIkodaDegreeSpecifiedDegreeById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from IkodaDegreeSpecifiedDegree where id=(:id)");
		q.setParameter("id", id);

		List<IkodaDegreeSpecifiedDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getJobsByUniqueJobId(Long ujId) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from Job where uniquejobid = (:ujId)");
			q.setParameter("ujId", ujId);

			List<Job> result = q.list();

			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from Job where uniquejobid = (:ujId) and detailLevel >= (:detailLevel)");
			q.setParameter("ujId", ujId);
			q.setParameter("detailLevel", detailLevel);

			List<Job> result = q.list();

			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel, String degree) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"from Job j JOIN j.areasOfStudy  where j.uniqueJob.id = (:ujId) and j.detailLevel >= (:detailLevel) and (:degree) in elements(j.areasOfStudy)");
			q.setParameter("ujId", ujId);
			q.setParameter("detailLevel", detailLevel);
			q.setParameter("degree", degree);

			List<Job> result = q.list();
			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getJobsByUniqueJobId(Long ujId, int detailLevel, String degree, String region)
			throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"from Job j JOIN j.areasOfStudy  where j.uniqueJob.id = (:ujId) and j.region like (:region) and j.detailLevel >= (:detailLevel) and (:degree) in elements(j.areasOfStudy)");
			q.setParameter("ujId", ujId);
			q.setParameter("detailLevel", detailLevel);
			q.setParameter("degree", degree);
			q.setParameter("region", region);

			List<Job> result = q.list();
			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Integer> getListOfMaxSalariesFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"Select  j.salaryEndRange From Job j where region like (:region) and uniquejobid in (:ujIds) and j.salaryStartRange > 1 and (:degreeName) in elements(j.areasOfStudy) order by rand()");
			q.setParameterList("ujIds", ujIds);
			q.setParameter("region", region);
			q.setParameter("degreeName", degreeName);
			List<Integer> result = q.setFirstResult(0).setMaxResults(100).list();
			PLog.getChoresLogger().debug("got result of size: " + result.size());

			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Integer> getListOfMinSalariesFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"Select  j.salaryStartRange From Job j where region like (:region) and uniquejobid in (:ujIds) and j.salaryStartRange > 1 and (:degreeName) in elements(j.areasOfStudy) order by rand()");
			q.setParameterList("ujIds", ujIds);
			q.setParameter("region", region);
			q.setParameter("degreeName", degreeName);

			List<Integer> result = q.setFirstResult(0).setMaxResults(100).list();

			PLog.getChoresLogger().debug("got result of size: " + result.size());

			PLog.getChoresLogger().debug(result);

			return result;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Long getMaxJobId() throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("select max(job.id) from Job job");
			Long result = (Long) q.uniqueResult();
			// PLog.getLogger().debug(obj.getClass());
			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Integer> getMaxSalaryFromJob(List<Long> ujIds, String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"Select j.salaryEndRange from Job j where region like (:region) and uniquejobid in (:ujIds) and j.salaryEndRange > 10000 order by j.salaryEndRange DESC");
		q.setParameterList("ujIds", ujIds);
		q.setParameter("region", region);
		List<Integer> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Integer> getMinSalaryFromJob(List<Long> ujIds, String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"Select j.salaryStartRange from Job j where region like (:region) and uniquejobid in (:ujIds) and j.salaryStartRange > 10000 order by j.salaryStartRange DESC");
		q.setParameterList("ujIds", ujIds);
		q.setParameter("region", region);
		List<Integer> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getMinSalaryFromJob(List<Long> ujIds, String region, String degreeName) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"From Job j where region like (:region) and uniquejobid in (:ujIds) and j.salaryStartRange > 10000 and (:degreeName) in elements(j.areasOfStudy) order by j.salaryStartRange DESC");
			q.setParameterList("ujIds", ujIds);
			q.setParameter("region", region);
			q.setParameter("degreeName", degreeName);
			List<Job> result = q.list();
			return result;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getRandomJobList(int jobCount) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		// String sql = "SELECT * FROM EMPLOYEE WHERE id = :employee_id";
		String sql = "select * from job order by random() limit 100";
		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(Job.class);
		// query.setParameter("employee_id", 10);
		List<Job> result = query.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Job> getRandomJobListWithinIdRange(int jobCount, Long minId, Long maxId, int minDetailLevel)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		String sql = "select * from job where id > :minId and id < :maxId and detailLevel>= :minDetailLevel order by random() limit :jobCount";
		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(Job.class);
		query.setParameter("minId", minId);
		query.setParameter("maxId", maxId);
		query.setParameter("minDetailLevel", minDetailLevel);
		query.setParameter("jobCount", jobCount);
		List<Job> result = query.list();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.persistence.dao.JobAnalysisDaoInterface#getSessionFactory()
	 */
	@Override
	public SessionFactory getSessionFactoryjobs()
	{
		return sessionFactoryjobs;
	}

	//////////////////////////////////////////////////////////
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public UniqueJob getUniqueJob(String uniqueJobName) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from UniqueJob where uniqueJob like (:uniqueJobName)");
		q.setParameter("uniqueJobName", uniqueJobName);

		List<UniqueJob> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public UniqueJob getUniqueJobById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from UniqueJob where id=(:id)");
		q.setParameter("id", id);

		List<UniqueJob> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ikoda.persistence.dao.JobAnalysisDaoInterface#getUnitTestSubject(java.
	 * lang.Long)
	 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public UnitTestSubject getUnitTestSubject(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		List<UnitTestSubject> result = session.createQuery("from UnitTestSubject where id = " + id).list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<UnitTestSubject> getUnitTestSubjects() throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		List<UnitTestSubject> result = session.createQuery("from UnitTestSubject").list();

		return result;
	}

	//////////////////////////////////////////////////////////
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByDegree getValueSalaryByDegree(Long countDegreeId, String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from ValueSalaryByDegree where countDegreeId = (:countDegreeId) and region like (:region)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("region", region);

		List<ValueSalaryByDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleByDegree(Long countDegreeId, String region)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from ValueSalaryByDegreeAndJobTitle where  countDegreeId =(:countDegreeId) and region like (:region)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("region", region);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId)
			throws PersistenceException
	{

		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByDegreeAndJobTitle where countDegreeId =(:countDegreeId)");
		q.setParameter("countDegreeId", countDegreeId);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId,
			int count) throws PersistenceException
	{

		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from ValueSalaryByDegreeAndJobTitle vsdjt where vsdjt.countDegree.id = (:countDegreeId) and vsdjt.count >= (:count)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("count", count);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByDegreeId(Long countDegreeId,
			String region) throws PersistenceException
	{

		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from ValueSalaryByDegreeAndJobTitle where countDegreeId =(:countDegreeId) and region like (:region)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("region", region);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByDegreeAndJobTitle where id=(:id)");
		q.setParameter("id", id);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	//////////////////////////////////////////////////////////
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByDegreeAndJobTitle getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(Long uniqueJobId,
			Long countDegreeId, String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from ValueSalaryByDegreeAndJobTitle where uniqueJobId = (:uniqueJobId) and countDegreeId =(:countDegreeId) and region like (:region)");
		q.setParameter("uniqueJobId", uniqueJobId);
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("region", region);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByDegree getValueSalaryByDegreeByDegreeId(Long countdegreeid, String region)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session
				.createQuery("from ValueSalaryByDegree where countdegreeid=(:countdegreeid) and region like (:region)");
		q.setParameter("countdegreeid", countdegreeid);
		q.setParameter("region", region);

		List<ValueSalaryByDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			PLog.getLogger().warn("no degree with id " + countdegreeid);
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByDegree getValueSalaryByDegreeById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByDegree where id=(:id)");
		q.setParameter("id", id);

		List<ValueSalaryByDegree> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	//////////////////////////////////////////////////////////
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByJobTitle getValueSalaryByJobTitle(Long uniqueJobId, String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session
				.createQuery("from ValueSalaryByJobTitle where uniqueJobId = (:uniqueJobId) and region like (:region)");
		q.setParameter("uniqueJobId", uniqueJobId);
		q.setParameter("region", region);

		List<ValueSalaryByJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByJobTitle getValueSalaryByJobTitleById(Long id) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByJobTitle where id=(:id)");
		q.setParameter("id", id);

		List<ValueSalaryByJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ValueSalaryByJobTitle getValueSalaryByJobTitleByUniqueJobId(Long ujId, String region)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByJobTitle where uniquejobid=(:ujId) and region like (:region)");
		q.setParameter("ujId", ujId);
		q.setParameter("region", region);

		List<ValueSalaryByJobTitle> result = q.list();
		if (result.size() > 1)
		{
			throw new PersistenceException("Retrieved more than one result");
		}
		if (result.size() == 0)
		{
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCountDegree(CountDegree countDegree) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(countDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCountJobTitle(CountJobTitle countJobTitle) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(countJobTitle);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCountJobTitleByDegreeName(CountJobTitleByDegreeName countJobTitleByDegreeName)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(countJobTitleByDegreeName);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveDatabaseDescriptors(DatabaseDescriptors dd) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(dd);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveIkodaDegreee(IkodaDegree ikodaDegree) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(ikodaDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveIkodaDegreeSpecifiedDegree(IkodaDegreeSpecifiedDegree ikodaDegreeSpecifiedDegree)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(ikodaDegreeSpecifiedDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveJob(Job job) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(job);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveUniqueJob(UniqueJob uniqueJob) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(uniqueJob);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ikoda.persistence.dao.JobAnalysisDaoInterface#saveUnitTestSubject(ikoda.
	 * persistence.model.UnitTestSubject)
	 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveUnitTestSubject(UnitTestSubject newUnitTestSubject) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(newUnitTestSubject);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveValueSalaryByDegree(ValueSalaryByDegree valueSalaryByDegree) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(valueSalaryByDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveValueSalaryByDegreeAndJobTitle(ValueSalaryByDegreeAndJobTitle valueSalaryByDegreeAndJobTitle)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(valueSalaryByDegreeAndJobTitle);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCountDegreeJob(CountDegreeJob cdj)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(cdj);
			PLog.getLogger().debug("saved "+cdj);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}
	

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveValueSalaryByJobTitle(ValueSalaryByJobTitle valueSalaryByJobTitle) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(valueSalaryByJobTitle);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.persistence.dao.JobAnalysisDaoInterface#setSessionFactory(org.
	 * hibernate.SessionFactory)
	 */
	@Override
	public void setSessionFactoryjobs(SessionFactory sessionFactory)
	{
		this.sessionFactoryjobs = sessionFactory;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateCountDegree(CountDegree countDegree) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(countDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updatePossibleTokenCount(PossibleTokenCount possibleTokenCount) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(possibleTokenCount);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void savePossibleTokenCount(PossibleTokenCount possibleTokenCount) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.save(possibleTokenCount);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateCountJobTitle(CountJobTitle countJobTitle) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(countJobTitle);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	//////////////////////////////////////////////////////////

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateCountJobTitleByDegreeName(CountJobTitleByDegreeName countJobTitleByDegreeName)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(countJobTitleByDegreeName);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateIkodaDegree(IkodaDegree ikodaDegree) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(ikodaDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateIkodaDegreeSpecifiedDegree(IkodaDegreeSpecifiedDegree ikodaDegreeSpecifiedDegree)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(ikodaDegreeSpecifiedDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateUniqueJob(UniqueJob uniqueJob) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(uniqueJob);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateValueSalaryByDegree(ValueSalaryByDegree valueSalaryByDegree) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(valueSalaryByDegree);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateValueSalaryByDegreeAndJobTitle(ValueSalaryByDegreeAndJobTitle valueSalaryByDegreeAndJobTitle)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(valueSalaryByDegreeAndJobTitle);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateValueSalaryByJobTitle(ValueSalaryByJobTitle valueSalaryByJobTitle) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getLogger().debug("Saving new business " + newBusiness);
			session.update(valueSalaryByJobTitle);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	//////////////////////////////////////////////////////////

}
