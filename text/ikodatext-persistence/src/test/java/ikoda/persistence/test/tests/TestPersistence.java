package ikoda.persistence.test.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ikoda.netio.config.MajorMappingConfig;
import ikoda.netio.config.MajorMappingConfigEntry;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.service.JobAnalysisService;
import ikoda.persistence.service.JobReportingByIkodaDegreeService;
import ikoda.persistence.test.testrunner.PTestSS;

@RunWith(SpringJUnit4ClassRunner.class)

@ContextHierarchy(
{ @ContextConfiguration(locations =
		{ "classpath:beanspersistence.xml" }) })

public class TestPersistence
{

	private static String jobTitle;

	private static String degreeName;

	@Autowired
	private JobAnalysisService jobAnalysisService;

	@Autowired
	@Qualifier(value = "sessionFactoryjobs")
	SessionFactory sessionFactoryjobs;
	@Autowired
	private JobReportingByIkodaDegreeService jobReportingByIkodaDegreeServiceInterface;

	private boolean createMajorMapXml()
	{
		try
		{
			MajorMappingConfig mmc = new MajorMappingConfig();
			MajorMappingConfigEntry entry = new MajorMappingConfigEntry();

			ArrayList<String> list1 = new ArrayList<String>();
			String im1 = "Psychology";
			list1.add("Psychology");
			list1.add("Counseling");
			list1.add("Social Science");
			list1.add("Social Sciences");

			entry.setIkodamajor(im1);
			entry.setRelatedMajors(list1);

			MajorMappingConfigEntry entry2 = new MajorMappingConfigEntry();
			ArrayList<String> list2 = new ArrayList<String>();
			String im2 = "Sociology";
			list2.add("Social Work");
			list2.add("Social Science");
			list2.add("Social Sciences");

			entry2.setIkodamajor(im2);
			entry2.setRelatedMajors(list2);

			mmc.getMajorMapEntry().add(entry);
			mmc.getMajorMapEntry().add(entry2);

			JAXBContext jaxbContext = JAXBContext.newInstance(MajorMappingConfig.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(mmc, new File("dummy_mmconfig.xml"));
			jaxbMarshaller.marshal(mmc, System.out);
			return true;
		}
		catch (Exception e)
		{
			PTestSS.getLogger().error(e.getMessage(), e);
			;
			return false;
		}
	}

	/*
	 * @Test public void testGetSalary() {
	 * PTestSS.getLogger().info("testGetSalary"); StringBuffer sb = new
	 * StringBuffer(); try { String degreeName="Engineering"; CountDegree cd =
	 * jobAnalysisService.getCountDegree(degreeName);
	 * 
	 * PLog.getLogger().info("Looking at degree id: " +
	 * cd.getSpecifiedDegreeName());
	 * 
	 * List<CountJobTitleByDegreeName> jobsByDegree = jobAnalysisService
	 * .getCountJobTitleByDegreeNameByDegreeId(cd.getId());
	 * PLog.getLogger().debug("Count of job titles for this degree  " +
	 * jobsByDegree.size()); List<Long> ujIdsForDegree = new ArrayList<Long>();
	 * for (CountJobTitleByDegreeName cjtd : jobsByDegree) {
	 * ujIdsForDegree.add(cjtd.getUniqueJob().getId()); } List<Job>
	 * jobs=getMinSalaryFromJob(ujIdsForDegree, "Canada", degreeName);
	 * 
	 * sb.append("\n\n------------------------\nLooking at "+degreeName);
	 * sb.append("\n"); for(Job job:jobs) { sb.append("\nJob: ");
	 * sb.append(job.getJobTitle()); sb.append("  |  $");
	 * sb.append(job.getSalaryStartRange()); sb.append("  |  ");
	 * sb.append(job.getRegion()); sb.append("  |  ");
	 * sb.append(job.getAreasOfStudy());
	 * assertTrue(job.getAreasOfStudy().toString().contains(degreeName)); }
	 * PTestSS.getLogger().debug(sb.toString());
	 * 
	 * 
	 * assertTrue(jobs.size()>0); } catch(Exception e) {
	 * 
	 * PTestSS.getLogger().debug(sb.toString());
	 * PTestSS.getLogger().error(e.getMessage()); fail(e.getMessage());
	 * 
	 * }
	 * 
	 * }
	 */

	private List<Job> getMinSalaryFromJob(List<Long> ujIds, String region, String degreeName)
			throws PersistenceException
	{
		PTestSS.getLogger().info("getMinSalaryFromJob");
		try
		{
			return jobAnalysisService.getMinSalaryFromJob(ujIds, region, degreeName);

		}
		catch (Exception e)
		{
			PTestSS.getLogger().error(e.getMessage(), e);
			fail(e.getMessage());
			return null;

		}
	}

	@Test
	public void testCreateMajorMap()
	{
		assertTrue(createMajorMapXml());
	}

	@Test
	public void testJob() throws Exception
	{
		try
		{
			PTestSS.getLogger().debug("testJob");
			Job job = new Job();

			List<String> testList = new ArrayList<String>();
			testList.add("aaa");
			testList.add("bbb");
			testList.add("ccc");

			job.setAreasOfStudy(testList);
			job.setJobTitle("sss" + System.currentTimeMillis());
			job.setQualifications(testList);
			job.setSkills(testList);
			job.setYearsExperience(testList);
			job.setRelatedMajors(testList);
			job.setRegion("rrr");
			job.setContentSource("tyu");
			jobAnalysisService.saveJob(job);
			assertTrue(null != job.getId());
		}
		catch (Exception e)
		{
			PTestSS.getLogger().error(e.getMessage(), e);

			fail(e.getMessage());
		}
	}

	/*
	 * @Test public void testCalculateMediansForDegree() { try {
	 * PTestSS.getLogger().info("testCalculateMediansForDegree");
	 * 
	 * CountDegree cd = jobAnalysisService.getCountDegree("Engineering");
	 * 
	 * PLog.getLogger().info("Looking at degree id: " +
	 * cd.getSpecifiedDegreeName());
	 * 
	 * List<CountJobTitleByDegreeName> jobsByDegree = jobAnalysisService
	 * .getCountJobTitleByDegreeNameByDegreeId(cd.getId());
	 * PLog.getLogger().debug("Count of job titles for this degree  " +
	 * jobsByDegree.size()); List<Long> ujIdsForDegree = new ArrayList<Long>();
	 * for (CountJobTitleByDegreeName cjtd : jobsByDegree) {
	 * ujIdsForDegree.add(cjtd.getUniqueJob().getId()); }
	 * 
	 * for (int i = 0; i < RegionInterface.regions.length; i++) { List<Integer>
	 * salaryMinList = jobAnalysisService.getMinSalaryFromJob(ujIdsForDegree,
	 * RegionInterface.regions[i]); List<Integer> salaryMaxList =
	 * jobAnalysisService.getMaxSalaryFromJob(ujIdsForDegree,
	 * RegionInterface.regions[i]);
	 * 
	 * 
	 * 
	 * PLog.getLogger().debug("Region: " + RegionInterface.regions[i]);
	 * PLog.getLogger().debug("MinList: " + salaryMinList);
	 * PLog.getLogger().debug("MaxList: " + salaryMaxList);
	 * 
	 * if(salaryMinList.size()==0||salaryMaxList.size()==0) { continue; }
	 * 
	 * int medianIndexMin = salaryMinList.size() / 2; int medianIndexMax =
	 * salaryMaxList.size() / 2;
	 * 
	 * PLog.getLogger().debug("medianIndexMin: " + medianIndexMin);
	 * PLog.getLogger().debug("medianIndexMax: " + medianIndexMax);
	 * 
	 * Integer medianMin = salaryMinList.get(medianIndexMin); Integer medianMax
	 * = salaryMaxList.get(medianIndexMax);
	 * 
	 * PLog.getLogger().debug("medianMin: " + medianMin);
	 * PLog.getLogger().debug("medianMax: " + medianMax);
	 * 
	 * Integer median = (medianMin + medianMax) / 2;
	 * 
	 * PLog.getLogger().debug("\n\n\nmedian: " + median);
	 * 
	 * ValueSalaryByDegree vsd =
	 * jobAnalysisService.getValueSalaryByDegreeByDegreeId(cd.getId(),
	 * RegionInterface.regions[i]); if(null==vsd) { PLog.getLogger().info(
	 * "No data for "+RegionInterface.regions[i]); } PLog.getLogger().debug(
	 * "ValueSalaryByDegree is " + vsd.getDegreeName());
	 * vsd.setMedianSalary(median);
	 * 
	 * 
	 * ///jobAnalysisService.updateValueSalaryByDegree(vsd);
	 * PLog.getLogger().info("\n\n\n Median set to " + vsd.getMedianSalary() +
	 * " for " + vsd.getDegreeName() + " in region " +
	 * vsd.getRegion()+"\n\n\n"); assertTrue(1==1); }
	 * 
	 * 
	 * } catch (Exception e) { PLog.getLogger().error(e.getMessage(), e);
	 * fail(e.getMessage()); } }
	 */
	@Test
	public void testtIkodaDegree()
	{

		try
		{
			StringBuffer sb = new StringBuffer();
			PTestSS.getLogger().info("testtIkodaDegree");
			List<IkodaDegree> list = jobReportingByIkodaDegreeServiceInterface.getIkodaDegrees();
			PTestSS.getLogger().debug("ikodadegree count" + list.size());
			assertTrue(list.size() > 0);

			Long sociologyId = jobReportingByIkodaDegreeServiceInterface.getIkodaDegreeIdByIkodaDegreeName("sociology");
			assertTrue(sociologyId > 0);
			sb.append("\n\n");
			sb.append("sociologyId " + sociologyId);

			List<IkodaDegreeSpecifiedDegree> result = jobReportingByIkodaDegreeServiceInterface
					.getIkodaDegreeSpecifiedDegreeByIkodaDegreeId(sociologyId);
			for (IkodaDegreeSpecifiedDegree idsd : result)
			{

				sb.append("\n \n Specified degree connected with Sociology:  ");
				sb.append(idsd.getSpecifiedDegree().getSpecifiedDegreeName());

				List<ValueSalaryByDegree> vsbdList = jobReportingByIkodaDegreeServiceInterface
						.getValueSalaryByDegreeBySpecifiedDegreeId(idsd.getSpecifiedDegree().getId());
				sb.append("\n");
				sb.append("\n");
				for (ValueSalaryByDegree vsbd : vsbdList)
				{

					sb.append("\n");
					sb.append(vsbd.getDegreeName() + " | avg:  " + vsbd.getAverageSalary() + " | median:  "
							+ vsbd.getMedianSalary());
				}

				sb.append("\n");
				sb.append("\n");

				List<ValueSalaryByDegreeAndJobTitle> vsbdjtList = jobReportingByIkodaDegreeServiceInterface
						.getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(idsd.getSpecifiedDegree().getId());
				for (ValueSalaryByDegreeAndJobTitle vsbdjt : vsbdjtList)
				{
					sb.append("\n");
					sb.append(vsbdjt.getDegreeName() + "Job Title:  " + vsbdjt.getJobTitle() + " | avg:  "
							+ vsbdjt.getAverageSalary() + " | median:  " + vsbdjt.getMedianSalary());
				}

			}

			PTestSS.getLogger().info(sb.toString());

			assertTrue(result.size() > 0);

		}
		catch (Exception e)
		{
			PTestSS.getLogger().error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

}