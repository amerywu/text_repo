package ikoda.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;



/**
 * 
 * 
 * Spreadsheet is a singleton. It maintains multiple dataset instances (of type CSVSpreadsheetCreator or LibsvmProcessor), each specified by its user defined name.<br>
 * <br>
 * Hence:<br>
 * <br>
 *       Spreadsheet.getInstance().initCsvSpreadsheet("DataOne", "path/to/dir");<br>
 *       Spreadsheet.getInstance().initCsvSpreadsheet("DataTwo", "path/to/dir");<br>
 * <br>
 * creates two distinct datasets that can receive distinct data into two separate sets.<br><br>

 * Data is added to a dataset by specifying its rowid, column name, and value.<br><br>
 *        addCell(rowId, "col1", key)<br>
 *        
 * The row is determined by the rowId. <br>
 * The column name will be created if it does not exist. Columns can be added on the fly. There is no limit to the number of columns that can be created.<br>
 * The cell value is stored as a String (but can be entered as a String or numeric type).<br><br>

 * Spreadsheet.getInstance().getCsvSpreadSheet("DataName").printCsvOverwrite();<br>
 *  Here any pre-existing file will be overwritten. If columns have not changed it is also possible to append to an existing file.<br>
 * @author jake
 *
 */
public class Spreadsheet
{
	private static Spreadsheet spreadsheet;
	private static final String UNDERSCORE = "_";

	public static Spreadsheet getInstance()
	{
		if (null == spreadsheet)
		{
			spreadsheet = new Spreadsheet();
		}
		return spreadsheet;
	}

	private Logger logger = null;

	private HashMap<String, Integer> fileNamesByBlockMap = new HashMap();

	private Map<String, AbstractSpreadsheetCreator> csvSpreadsheetsMap = new HashMap<String, AbstractSpreadsheetCreator>();

	
	private Spreadsheet()
	{
		logger = SSm.getAppLogger();
	}




	
	/**
	 * Returns an existing instance of CSVSpreadsheetCreator
	 * @param name
	 * @return
	 */
	public synchronized CSVSpreadsheetCreator getCsvSpreadSheet(String name)
	{
		CSVSpreadsheetCreator csvSpreadsheet = (CSVSpreadsheetCreator) csvSpreadsheetsMap.get(getFileName(name));
		if (null == csvSpreadsheet)
		{
			logger.warn("Null. No csvspreadsheet for " + getFileName(name) + ". Available spreadsheets are "
					+ csvSpreadsheetsMap.keySet());
		}
		return csvSpreadsheet;
	}

	private String getFileName(String fileName)
	{

		Integer block = fileNamesByBlockMap.get(fileName);
		if (null == block)
		{
			fileNamesByBlockMap.put(fileName, 0);

			return fileName + UNDERSCORE + 0;
		}

		return fileName + UNDERSCORE + block;
	}

	/**
	 * Returns an existing instance of LibSvmProcessor
	 * LibSvmProcessor can generate both LIBSVM and CSV format output
	 * @param name
	 * @return
	 * @throws ClassCastException
	 * @throws IKodaUtilsException
	 */
	public synchronized LibSvmProcessor getLibSvmProcessor(String name) throws ClassCastException, IKodaUtilsException
	{

		LibSvmProcessor libSvmProcessor = (LibSvmProcessor) csvSpreadsheetsMap.get(getFileName(name));
		if (null == libSvmProcessor)
		{
			logger.warn("Null. No csvspreadsheet for " + getFileName(name) + ". Available spreadsheets are "
					+ csvSpreadsheetsMap.keySet());
		}
		return libSvmProcessor;

	}

	/**
	 * Creates a new instance of CSVSpreadsheetCreator
	 * @param name
	 * @param logger
	 * @param dirPath
	 */
	public synchronized void initCsvSpreadsheet(String name, Logger logger, String dirPath)
	{
		initCsvSpreadsheet1(name, logger, dirPath);
	}

	/**
	 * Creates a new instance of CSVSpreadsheetCreator
	 * @param name
	 * @param dirPath
	 */
	public synchronized void initCsvSpreadsheet(String name, String dirPath)
	{

		initCsvSpreadsheet(name, logger, dirPath);
	}

	/**
	 * Creates a new instance of CSVSpreadsheetCreator
	 * @param name
	 * @param loggerName
	 * @param dirPath
	 */
	public synchronized void initCsvSpreadsheet(String name, String loggerName, String dirPath)
	{

		initCsvSpreadsheet(name, SSm.getLogger(loggerName), dirPath);

	}

	/**
	 * Creates a new instance of CSVSpreadsheetCreator
	 * @param name
	 * @param logger
	 * @param dirPath
	 */
	public synchronized void initCsvSpreadsheet1(String name, Logger logger, String dirPath)
	{
		String csvName = getFileName(name);
		if (null == csvSpreadsheetsMap.get(csvName))
		{

			csvSpreadsheetsMap.put(csvName, new CSVSpreadsheetCreator(name, logger, dirPath));
		}

	}

	/**
	 * Creates a new instance of LibSvmProcessor
	 * @param name
	 * @param logger
	 * @param targetColumnName
	 * @param dirPath
	 */
	public synchronized void initLibsvm2(String name, Logger logger, String targetColumnName, String dirPath)
	{
		String csvName = getFileName(name);
		if (null == csvSpreadsheetsMap.get(csvName))
		{
			logger.info("Created spreadsheet "+csvName);
			csvSpreadsheetsMap.put(csvName,
					new LibSvmProcessor(name, logger, targetColumnName, LibSvmProcessor.AUTO_ID, dirPath));
		}
		else
		{
			logger.warn("Spreadsheet already exists: " + csvName);
		}
	}

	/**
	 * Creates a new instance of LibSvmProcessor
	 * @param name
	 * @param targetColumnName
	 * @param dirPath
	 */
	public synchronized void initLibsvm2(String name, String targetColumnName, String dirPath)
	{
		initLibsvm2(name, logger.getName(), targetColumnName, dirPath);
	}

	/**
	 * Creates a new instance of LibSvmProcessor
	 * @param name
	 * @param loggerName
	 * @param targetColumnName
	 * @param dirPath
	 */
	public synchronized void initLibsvm2(String name, String loggerName, String targetColumnName, String dirPath)
	{

		initLibsvm2(name, SSm.getLogger(loggerName), targetColumnName, dirPath);
	}

	private Logger initLogger(String loggerName)
	{
		return SSm.getLogger(loggerName);
	}

	private void registerNewBlock(String fileName)
	{
		Integer block = fileNamesByBlockMap.get(fileName);
		if (null == block)
		{
			block = -1;
		}

		int newBlock = block + 1;
		fileNamesByBlockMap.put(fileName, Integer.valueOf(newBlock));
	}

	/**
	 * Removes references to a LibSvmProcessor or CSVSpreadsheetCreator instance from Spreadsheet
	 * @param name
	 */
	public void removeSpreadsheet(String name)
	{
		CSVSpreadsheetCreator success = (CSVSpreadsheetCreator) csvSpreadsheetsMap.remove(getFileName(name));
		if (null == success)
		{
			logger.warn("Could not remove " + name + ". The spreadsheet is not registered");
		}
	}

	/**
	 * Resets a CSVSpreadsheetCreator by clearing all data, but maintaining metadata such
	 * as keyspaceName, targetName etc.
	 * @param fileName
	 * @param columnsToIgnore
	 */
	public synchronized void resetSpreadsheet(final String fileName, String[] columnsToIgnore)
	{
		try
		{

			String currentFile = getFileName(fileName);
			logger.info("\n\nNEW SPREADSHEET replacing" + currentFile + "\n\n");

			getCsvSpreadSheet(fileName).finalizeAndJoinBlocks(currentFile + ".csv");

			saveExtantBlockAndClearData(fileName);

			String targetColumn = getCsvSpreadSheet(fileName).getTargetColumnName();
			String projectPrefix = getCsvSpreadSheet(fileName).getProjectPrefix();
			String path = getCsvSpreadSheet(fileName).getPathToDirectory();
			String keyspaceName = getCsvSpreadSheet(fileName).getKeyspaceName();
			String keyspaceUuid = getCsvSpreadSheet(fileName).getKeyspaceUUID();
			registerNewBlock(fileName);

			logger.info("Creating " + getFileName(fileName) + "\n");
			initCsvSpreadsheet(fileName, logger, path);
			getCsvSpreadSheet(fileName).setProjectPrefix(projectPrefix);
			getCsvSpreadSheet(fileName).setTargetColumnName(targetColumn);
			getCsvSpreadSheet(fileName).setKeyspaceName(keyspaceName);
			getCsvSpreadSheet(fileName).setKeyspaceUUID(keyspaceUuid);

			getCsvSpreadSheet(fileName).clearAll();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Resets a LibSvmProcessor by clearing all data, but maintaining metadata such
	 * as keyspaceName, targetName etc.
	 * @param fileName
	 * @param columnsToIgnore
	 */
	public synchronized void resetSpreadsheetLibsvm(final String fileName, String[] columnsToIgnore)
	{
		try
		{

			String currentFile = getFileName(fileName);
			logger.info("\n\nNEW SPREADSHEET replacing" + currentFile + "\n\n");

			getLibSvmProcessor(fileName).printLibSvmBlock(columnsToIgnore);

			String targetColumn = getLibSvmProcessor(fileName).getTargetColumnName();
			String projectPrefix = getLibSvmProcessor(fileName).getProjectPrefix();
			String path = getLibSvmProcessor(fileName).getPathToDirectory();
			String keyspaceName = getLibSvmProcessor(fileName).getKeyspaceName();
			String keyspaceUuid = getLibSvmProcessor(fileName).getKeyspaceUUID();
			String localurl =  getLibSvmProcessor(fileName).getLocalUrl();
			String localports =  getLibSvmProcessor(fileName).getLocalPorts();
			registerNewBlock(fileName);

			logger.info("Creating " + getFileName(fileName) + "\n");
			initLibsvm2(fileName, logger, targetColumn, path);
			getLibSvmProcessor(fileName).setProjectPrefix(projectPrefix);
			getLibSvmProcessor(fileName).setTargetColumnName(targetColumn);
			getLibSvmProcessor(fileName).setKeyspaceName(keyspaceName);
			getLibSvmProcessor(fileName).setKeyspaceUUID(keyspaceUuid);
			getLibSvmProcessor(fileName).setLocalUrl(localurl);
			getLibSvmProcessor(fileName).setLocalPorts(localports);
			getLibSvmProcessor(fileName).clearAll();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	

	private void saveExtantBlockAndClearData(String fileName) throws IKodaUtilsException
	{

		getCsvSpreadSheet(fileName).printCsvBlock("PART_" + fileName);
		getCsvSpreadSheet(fileName).clearData();
	}

	/**
	 * @param s
	 */
	public void setSpreadsheetLogger(String s)
	{
		logger = initLogger(s);
	}
}
