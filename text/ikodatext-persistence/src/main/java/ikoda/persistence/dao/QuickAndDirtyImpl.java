package ikoda.persistence.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ikoda.persistence.application.PLog;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.CountDegreeJob;
import ikoda.persistence.model.CountJobTitleByDegreeName;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.reporting.CPCJob;
import ikoda.persistence.model.reporting.CPCPayByJobTitleAndDegree;
import ikoda.persistence.model.reporting.CPCSalaryByDegree;

@Repository
public class QuickAndDirtyImpl
{

	@Autowired
	@Qualifier(value = "sessionFactoryjobs")
	SessionFactory sessionFactoryjobs;

	@Transactional
	public List<CountDegree> getAllCountDegrees() throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountDegree cd order by cd.count desc");

			List<CountDegree> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<CountJobTitleByDegreeName> getAllCountJobTitleByDegreeNames() throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountJobTitleByDegreeName");

			List<CountJobTitleByDegreeName> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNames(Long countDegreeId) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountJobTitleByDegreeName t1 where countDegreeId =(:countDegreeId) order by t1.count desc");
			q.setParameter("countDegreeId", countDegreeId);
			PLog.getChoresLogger().debug("calling "+q.getQueryString());
			List<CountJobTitleByDegreeName> result = q.list();
			
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	@Transactional
	public List<CountDegree> getCountDegreeByDegreeName(String name) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountDegree where specifiedDegreeName like (:name)");
			q.setParameter("name", name);

			List<CountDegree> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());

			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	


	@Transactional
	public List<CountJobTitleByDegreeName> getCountJobTitleByDegreeNameByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountJobTitleByDegreeName where countDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<CountJobTitleByDegreeName> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;

		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	@Transactional
	public List<IkodaDegreeSpecifiedDegree> getIKodaDegreeSpecifiedDegreeByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from IkodaDegreeSpecifiedDegree where specifiedDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<IkodaDegreeSpecifiedDegree> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<ValueSalaryByDegree> getValueSalaryByDegreeeByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from ValueSalaryByDegree where countDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<ValueSalaryByDegree> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	
	@Transactional
	public List<ValueSalaryByDegreeAndJobTitle> getValueSalaryByDegreeAndJobTitleByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from ValueSalaryByDegreeAndJobTitle where countDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<ValueSalaryByDegreeAndJobTitle> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<CPCJob> getCPCJobByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CPCJob where countDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<CPCJob> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<CPCPayByJobTitleAndDegree> getCPCPayByJobTitleAndDegreeByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CPCPayByJobTitleAndDegree where countDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<CPCPayByJobTitleAndDegree> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	
	@Transactional
	public List<CPCSalaryByDegree> getCPCSalaryByDegreeByCDID(Long cdid) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CPCSalaryByDegree where countDegreeId = (:cdid)");
			q.setParameter("cdid", cdid);

			List<CPCSalaryByDegree> result = q.list();
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return result;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public void updateCountDegree(CountDegree cd) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();
			PLog.getChoresLogger().debug("Updating: "+cd);
			session.merge(cd);
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public void updateJob(Job obj) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();
			PLog.getChoresLogger().debug("Updating: "+obj);
			session.merge(obj);
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	
	@Transactional
	public void updateCountJobTitleByDegreeName(CountJobTitleByDegreeName obj) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();
			PLog.getChoresLogger().debug("Updating: "+obj);
			session.merge(obj);
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public void saveCountDegreeJob(CountDegreeJob obj) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();
			PLog.getChoresLogger().debug("Updating: "+obj);
			session.saveOrUpdate(obj);
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public boolean countDegreeJobExists(CountDegreeJob obj) throws PersistenceException
	{
		try
		{
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery("from CountDegreeJob  where countDegreeId = (:cdid) and uniqueJobId = (:ujid) and jobId =(:jobid)");
			q.setParameter("cdid", obj.getCountDegree().getId());
			q.setParameter("ujid", obj.getUniqueJob().getId());
			q.setParameter("jobid", obj.getJob().getId());

			List<CountDegreeJob> result = q.list();
			if(null==result || result.size()==0)
			{
				return false;
			}
			PLog.getChoresLogger().debug("Results List Size: "+result.size());
			return true;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	

	@Transactional
	public List<Job> getListFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException
	{
		try
		{
			
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"From Job j where region like (:region) and uniquejobid in (:ujIds) and j.salaryStartRange > 1 and (:degreeName) in elements(j.areasOfStudy) order by rand()");
			q.setParameterList("ujIds", ujIds);
			q.setParameter("region", region);
			q.setParameter("degreeName", degreeName);

			List<Job> result = q.list();

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
	
	@Transactional
	public List<Job> getListFromJob1(List<Long> ujIds, String region)
			throws PersistenceException
	{
		try
		{
			
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"From Job j where region like (:region) and uniquejobid in (:ujIds) order by rand()");
			q.setParameterList("ujIds", ujIds);
			q.setParameter("region", region);


			List<Job> result = q.list();

			PLog.getChoresLogger().debug("got result of size: " + result.size());

			//PLog.getChoresLogger().debug(result);

			return result;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<Job> getListFromJob2(List<Long> ujIds)
			throws PersistenceException
	{
		try
		{
			
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"From Job j where  uniquejobid in (:ujIds) and byextrapolation = false order by rand()");
			q.setParameterList("ujIds", ujIds);



			List<Job> result = q.list();

			PLog.getChoresLogger().debug("got result of size: " + result.size());

			//PLog.getChoresLogger().debug(result);

			return result;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<Job> getListFromJobByUjid(Long ujid)
			throws PersistenceException
	{
		try
		{
			
			Session session = this.sessionFactoryjobs.getCurrentSession();

			Query q = session.createQuery(
					"From Job j where  uniqueJobId = (:ujid) and byExtrapolation = false order by rand()");
			q.setParameter("ujid", ujid);



			List<Job> result = q.list();

			PLog.getChoresLogger().debug("got result of size: " + result.size());

			//PLog.getChoresLogger().debug(result);

			return result;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	
	@Transactional
	public List<Object[]> getDegreeFromAreasOfStudy(String degreeName)
			throws PersistenceException
	{
		try
		{
			
			Session session = this.sessionFactoryjobs.getCurrentSession();

			SQLQuery query = session.createSQLQuery("select emp_id, emp_name, emp_salary from job_areasofstudy where areasofstudy like '"+degreeName+"'");
			List<Object[]> result = query.list();

			

			PLog.getChoresLogger().debug("got result of size: " + result.size());

			//PLog.getChoresLogger().debug(result);

			return result;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}
	//////////////////////////////////////////////////////////

}
