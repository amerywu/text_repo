package ikoda.persistence.service;

public interface InterfaceReportingFilters
{

	public final static String ORDER_BY_DEGREE = "ORDER_BY_DEGREE";
	public final static String ORDER_BY_SALARY = "ORDER_BY_SALARY";
	
	
	
	public final static int MAXIMUM_JOBS_DISPLAYED=30;
	
	public final static String STUDENT = "STUDENT";
	public final static String SOCIALWORKER = "SOCIAL WORKER";
	public final static String RESEARCHSCIENTIST = "SCIENTIST";
	public final static String FELLOW = "FELLOW";
	public final static String DIRECTOR = "DIRECTOR";
	public final static String ORACLE = "ORACLE";
	public final static String PRESIDENT = "PRESIDENT";
	public final static String NURSE = "NURSE";
	public final static String ENGINEER = "ENGINEER";
	public final static String AUDITOR = "AUDITOR";
	public final static String ACCOUNTANT = "ACCOUNTANT";
	public final static String ARCHITECT = "ARCHITECT";
	public final static String ANALYST = "ANALYST";
	public final static String GENERAL = "GENERAL";
	public final static String OLOGIST = "OLOGIST";
	public final static String AGENCYREP = "AGENCY REPRESENTATIVE";
	public final static String DATABASE = "DATABASE";
	public final static String DEVELOPER = "DEVELOPER";
	public final static String MANAGER = "MANAGER";
	public final static String PROFESSOR = "PROFESSOR";
	public final static String BANKER = "BANKER";
	public final static String REVIEW = "REVIEW";
	public final static String DESCRIPTION = "DESCRIPTION";
	public final static String TITLE = "TITLE";
	public final static String DETAILS = "DETAILS";
	public final static String MICHAEL = "MICHAEL";
	public final static String MOBILE = "MOBILE";
	public final static String THERAPIST = "THERAPIST";
	public final static String PHARMACIST = "PHARMACIST";
	public final static String PHYSICIAN = "PHYSICIAN";

	////////////////////////////
	public final static String JOBTITLE = "JOB TITLE";
	public final static String EMAIL = "BY EMAIL";
	public final static String ALERT = "ALERT";
	public final static String SEARCH = "SEARCH JOBS";
	public final static String SEARCH1 = "SEARCH FOR MORE";
	public final static String SEARCH2 = "SEARCH FOR JOBS";
	public final static String CHECKOUT = "CHECK OUT";
	public final static String SIGNIN = "SIGN IN";
	public final static String SIGNIN1= "LOG IN";
	public final static String FILE1= "CHOOSE FILE";
	public final static String FILE2= "UPLOAD";	
	public final static String APPLY1= "HELP YOU APPLY";	
	public final static String CAREERS= "CAREERS";	
	public final static String HIRING= "HIRING";	
	public final static String KEYWORD= "KEYWORD";	
	public final static String APPLY = "APPLY FOR JOBS";

	
	
	

	
	/////////////////////////
	
	
	public final static String RELATED = "RELATED";
	public final static String ACCFIN = "ACCOUNTING FINANCE";

	public final static String RELEVANT = "RELEVANT";

	public final static String ANYFIELD = "ANY ";

	public final static String COMPSCIENG = "COMPUTER SCIENCE ENGINEERING";
	
	public final static String ENGED = "ENGLISH EDUCATION";
	
	public final static String ED = "EDUCATION";

	public final static String ENGPHYS = "ENGINEERING PHYSICS";

	public final static String BUSCOM = "BUSINESS COMMERCE";
	public final static String ANY_DEGREE = "Any Subject";
	///////////////////////////////
	
	
	
	public final static String[] excludeAnyDegreeList =
	{ STUDENT, DIRECTOR, PRESIDENT, NURSE, ENGINEER, AUDITOR, ACCOUNTANT, ANALYST, GENERAL, 
			OLOGIST, AGENCYREP, SOCIALWORKER, RESEARCHSCIENTIST,DATABASE,
			DEVELOPER,MANAGER,PROFESSOR,REVIEW ,TITLE,MOBILE,ORACLE,THERAPIST,ARCHITECT,FELLOW,PHARMACIST,PHYSICIAN,BANKER};
	
	
	public final static String[] excludeFromSkills = { DESCRIPTION,EMAIL,JOBTITLE,ALERT,
			SEARCH,SEARCH1,CHECKOUT,  SIGNIN, SIGNIN1, FILE1, FILE2,APPLY1,HIRING,CAREERS,
			KEYWORD,SEARCH2,APPLY};
	

	public  final static String[] jobTitleBlackList =
		{ REVIEW, DESCRIPTION, TITLE,DETAILS,MICHAEL,MOBILE,STUDENT};
	
	
	

	

	public final static String[] ignoreDegreeNameList =
	{ RELEVANT, RELATED, ANYFIELD, COMPSCIENG, ENGPHYS, BUSCOM, ENGED,ED , ACCFIN};
	
}
