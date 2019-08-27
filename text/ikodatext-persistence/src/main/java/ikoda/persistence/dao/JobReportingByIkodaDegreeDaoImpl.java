package ikoda.persistence.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.persistence.application.PLog;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.reporting.CPCJob;
import ikoda.persistence.model.reporting.CPCPayByJobTitleAndDegree;
import ikoda.persistence.model.reporting.CPCSalaryByDegree;

@Repository
public class JobReportingByIkodaDegreeDaoImpl implements JobReportingByIkodaDegreeDaoInterface
{

	@Autowired
	@Qualifier(value = "sessionFactoryjobs")
	SessionFactory sessionFactoryjobs;
	
	private final static String DEGREE_LEVEL="DEGREE_LEVEL";

	public JobReportingByIkodaDegreeDaoImpl()
	{
		// TODO Auto-generated constructor stub
	}

	//////////////////////////////////////////////////////////

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteCPCPayByJobTitleAndDegree(Long countDegreeId, Long uniqueJobId, String region, Long iKodaDegreeId, String degreelevel)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{
			Query query = session.createQuery(
					"delete CPCPayByJobTitleAndDegree where uniqueJobId = (:uniqueJobId) and "
					+ "countDegreeId = (:countDegreeId) and region like (:region) and iKodaDegreeId= (:iKodaDegreeId)"
					+ "and degreelevel= (:degreelevel)");
			query.setParameter("countDegreeId", countDegreeId);
			query.setParameter("uniqueJobId", uniqueJobId);
			query.setParameter("region", region);
			query.setParameter("iKodaDegreeId",iKodaDegreeId);
			query.setParameter("degreelevel",degreelevel);
			
			int result = query.executeUpdate();
			return result;
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteCPCSalaryByDegree(Long countDegreeId, String region, String degreelevel ) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{
			Query query = session.createQuery(
					"delete CPCSalaryByDegree where countDegreeId = (:countDegreeId) and region like (:region) and degreeLevel like (:degreelevel)");
			query.setParameter("countDegreeId", countDegreeId);
			query.setParameter("region", region);
			query.setParameter("degreelevel",degreelevel);
			int result = query.executeUpdate();
			return result;
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCSalaryByDegree> getAllCpcSalaryByBachelorLevelDegrees () throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from CPCSalaryByDegree where degreeLevel like (:degreelevel)");

		q.setParameter("degreelevel",DEGREE_LEVEL);
		List<CPCSalaryByDegree> result = q.list();

		return result;
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCSalaryByDegree> getAllCpcSalaryByBachelorLevelDegreesForRegion (String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from CPCSalaryByDegree where region like (:region) and  degreeLevel like (:degreelevel)");
		q.setParameter("region", region);
		q.setParameter("degreelevel",DEGREE_LEVEL);

		List<CPCSalaryByDegree> result = q.list();

		return result;
	}


	
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CPCJob getCPCJob(Long jobId) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCJob where jobId = (:jobId)");
		q.setParameter("jobId", jobId);

		List<CPCJob> result = q.list();
		if (result == null || result.size() == 0)
		{
			return null;
		}
		if (result.size() > 1)
		{
			throw new PersistenceException("Too many results");
		}
		return result.get(0);
	}
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCJob> getCPCJob(Long ujId, Long countDegreeId) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCJob where uniqueJobId = (:ujId) and countDegreeId = (:countDegreeId)");
		q.setParameter("ujId", ujId);
		q.setParameter("countDegreeId", countDegreeId);

		List<CPCJob> result = q.list();
		
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CPCJob getCPCJob(Long jobId, Long countDegreeId, Long uniqueJobId, String region, Long ikodaDegreeId) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCJob where jobId= (:jobId) and countDegreeId = (:countDegreeId) and uniqueJobId=(:uniqueJobId) and region like (:region) and ikodaDegreeId = (:ikodaDegreeId)");
		q.setParameter("jobId", jobId);
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("uniqueJobId", uniqueJobId);
		q.setParameter("region", region);
		q.setParameter("ikodaDegreeId", ikodaDegreeId);

		List<CPCJob> result = q.list();
		if (result == null || result.size() == 0)
		{
			return null;
		}
		if (result.size() > 1)
		{
			throw new PersistenceException("Too many results");
		}
		return result.get(0);
	}
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCJob> getCPCJob(Long ujId, Long countDegreeId, Long ikodaDegreeId, String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCJob where uniqueJobId = (:ujId) and countDegreeId = (:countDegreeId) and iKodaDegreeId = (:iKodaDegreeId) and region like (:region)");
		q.setParameter("ujId", ujId);
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("region", region);
		q.setParameter("iKodaDegreeId", ikodaDegreeId);
		
		List<CPCJob> result = q.list();
		
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CPCPayByJobTitleAndDegree getCPCPayByJobTitleAndDegree(Long countDegreeId, Long uniqueJobId, String region, Long ikodaDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from CPCPayByJobTitleAndDegree where countDegreeId = (:countDegreeId) and uniqueJobId = (:uniqueJobId) and region like (:region) and ikodaDegreeId = (:ikodaDegreeId)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("uniqueJobId", uniqueJobId);
		q.setParameter("region", region);
		q.setParameter("ikodaDegreeId", ikodaDegreeId);

		List<CPCPayByJobTitleAndDegree> result = q.list();
		if (result == null || result.size() == 0)
		{
			return null;
		}
		if (result.size() > 1)
		{
			throw new PersistenceException("Too many results");
		}
		return result.get(0);
	}
	


	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByCountDegreeIdAndRegion(Long countDegreeId, String region)
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCPayByJobTitleAndDegree where region like (:region) and countDegreeId = (:countDegreeId)");
		q.setParameter("region", region);
		q.setParameter("countDegreeId", countDegreeId);

		List<CPCPayByJobTitleAndDegree> result = q.list();

		return result;
	}
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndBachelorLevelDegreeByIkodaDegreeName(String iKodaDegreeName)
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCPayByJobTitleAndDegree where iKodaDegreeName like (:iKodaDegreeName) and degreeLevel like (:degreelevel)");
		q.setParameter("iKodaDegreeName", iKodaDegreeName);
		q.setParameter("degreelevel", DEGREE_LEVEL);

		List<CPCPayByJobTitleAndDegree> result = q.list();

		return result;
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndBachelorLevelDegreeByIkodaDegreeName(String iKodaDegreeName, String region) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();
	
			Query q = session.createQuery("from CPCPayByJobTitleAndDegree where iKodaDegreeName like (:iKodaDegreeName) and region like (:region) and degreeLevel like (:degreelevel)");
			q.setParameter("iKodaDegreeName", iKodaDegreeName);
			q.setParameter("region", region);
			q.setParameter("degreelevel", DEGREE_LEVEL);
	
			List<CPCPayByJobTitleAndDegree> result = q.list();
			PLog.getRLogger().debug(result);
	
			return result;
		}
		catch(Exception e)
		{
			throw new PersistenceException(e.getMessage(),e);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CPCSalaryByDegree getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(Long countDegreeId, String region)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from CPCSalaryByDegree  where countDegreeId = (:countDegreeId) and region like (:region)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("region", region);

		List<CPCSalaryByDegree> result = q.list();
		if (null == result || result.size() == 0)
		{
			return null;
		}
		if (result.size() > 1)
		{
			throw new PersistenceException("Too many results.");
		}

		return result.get(0);
	}
	
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	
	public CPCSalaryByDegree getCPCSalaryByBachelorLevelDegreeBySpecifiedDegreeNameAndRegion(String degreeName, String region)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from CPCSalaryByDegree  where degreeName like (:degreeName) and region like (:region) and degreeLevel like (:degreelevel)");
		q.setParameter("degreeName", degreeName);
		q.setParameter("region", region);
		q.setParameter("degreelevel", DEGREE_LEVEL);

		List<CPCSalaryByDegree> result = q.list();
		if (null == result || result.size() == 0)
		{
			return null;
		}
		if (result.size() > 1)
		{
			throw new PersistenceException("Too many results.");
		}

		return result.get(0);
	}
	
	
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<CPCSalaryByDegree> getCpcSalaryByBachelorLevelDegreesByRegion (String region) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from CPCSalaryByDegree where region like (:region) and degreeLevel like (:degreelevel)");
		q.setParameter("region", region);
		q.setParameter("degreelevel", DEGREE_LEVEL);
		List<CPCSalaryByDegree> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<IkodaDegree> getIkodaDegrees() throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from IkodaDegree");

		List<IkodaDegree> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeByIkodaDegreeId(Long ikodaDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from IkodaDegreeSpecifiedDegree where ikodadegreeid = (:ikodaDegreeId)");
		q.setParameter("ikodaDegreeId", ikodaDegreeId);

		List<IkodaDegreeSpecifiedDegree> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<IkodaDegreeSpecifiedDegree> getIkodaDegreeSpecifiedDegreeBySpecifiedDegreeId(Long specifiedDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from IkodaDegreeSpecifiedDegree where specifieddegreeid = (:specifiedDegreeId)");
		q.setParameter("specifiedDegreeId", specifiedDegreeId);

		List<IkodaDegreeSpecifiedDegree> result = q.list();

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.persistence.dao.JobReportingByIkodaDegreeDaoInterface#
	 * getSessionFactoryjobs()
	 */
	@Override
	public SessionFactory getSessionFactoryjobs()
	{
		return sessionFactoryjobs;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByDegreeAndJobTitle where countdegreeid = (:countDegreeId)");
		q.setParameter("countDegreeId", countDegreeId);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(Long countDegreeId,
			int count) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery(
				"from ValueSalaryByDegreeAndJobTitle vjdt where vjdt.countDegree.id = (:countDegreeId) and vjdt.count >=(:count)");
		q.setParameter("countDegreeId", countDegreeId);
		q.setParameter("count", count);

		List<ValueSalaryByDegreeAndJobTitle> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ValueSalaryByDegree> getValueSalaryByDegreeBySpecifiedDegreeId(Long countDegreeId)
			throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();

		Query q = session.createQuery("from ValueSalaryByDegree where countdegreeid = (:countDegreeId)");
		q.setParameter("countDegreeId", countDegreeId);

		List<ValueSalaryByDegree> result = q.list();

		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCPCJob(CPCJob cpcjob) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getRLogger().debug("Saving new business " + newBusiness);
			session.save(cpcjob);
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCPCPayByJobTitleAndDegree(CPCPayByJobTitleAndDegree jbd) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getRLogger().debug("Saving new business " + newBusiness);
			session.save(jbd);
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveCPCSalaryByDegree(CPCSalaryByDegree sbd) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getRLogger().debug("Saving new business " + newBusiness);
			session.save(sbd);
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.persistence.dao.JobReportingByIkodaDegreeDaoInterface#
	 * setSessionFactoryjobs(org.hibernate.SessionFactory)
	 */
	@Override
	public void setSessionFactoryjobs(SessionFactory sessionFactoryjobs)
	{
		this.sessionFactoryjobs = sessionFactoryjobs;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateCPCJob(CPCJob cpcjob) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getRLogger().debug("Saving new business " + newBusiness);
			session.update(cpcjob);
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateCPCPayByJobTitleAndDegree(CPCPayByJobTitleAndDegree jbd) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getRLogger().debug("Saving new business " + newBusiness);
			session.update(jbd);
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateCPCSalaryByDegree(CPCSalaryByDegree sbd) throws PersistenceException
	{
		Session session = this.sessionFactoryjobs.getCurrentSession();
		try
		{

			// SSm.getRLogger().debug("Saving new business " + newBusiness);
			session.update(sbd);
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

}
