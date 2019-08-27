package ikoda.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.Logger;

public abstract class AbstractSpreadsheetCreator
{

	protected static final String COMMA = ",";
	protected static final String ESCAPE = "\\\\";

	private static final String SEMICOLON = ";";
	protected static final String EMPTY = "";
	protected static final String CSV = "CSV";
	protected static final String DASH = "-";
	protected static final String COLON = ":";
	protected static final String SPACE = " ";
	protected static final String END = "\n";

	protected static final String BLANK = "BLANK";
	protected static final String COLUMN_MAP_SUFFIX = "-columnMap.txt";
	protected static final String ID_MAP_SUFFIX = "-idColumn.txt";
	protected static final String TARGET_MAP_SUFFIX = "-targetMap.txt";
	protected static final String LIBSVM = ".libsvm";
	private static final String NUMERIC_REGEX = "([^0-9.-])";
	protected Logger logger;

	protected Map<String, String> columnHeadings = new TreeMap<>();

	protected Map<String, Integer> targetMap = new HashMap<>();
	protected Map<Integer, String> targetMapInverse = new HashMap<>();

	protected Map<String, HashMap<String, String>> data = new HashMap<>();
	protected int targetCount = 1;
	protected String targetColumnName;
	protected String pkColumnName = "A_RowId";
	protected String projectPrefix = "";
	protected String pathToDir = "";
	protected String name = "UNNAMED";
	private String keyspaceName = "";
	private String keyspaceUUID = "";


	public AbstractSpreadsheetCreator()
	{

	}
	

	public AbstractSpreadsheetCreator(String inname)
	{
		name = inname;
	}

	/**
	 * Adds a cell to the dataset. The rowId specifies row assignment. columnId specifies the column name.
	 * Column indices are generated internally. 
	 * 
	 * @param inrowId
	 * @param incolumnId
	 * @param indataValue
	 * @throws IKodaUtilsException
	 */
	public void addCell(long inrowId, String incolumnId, double indataValue) throws IKodaUtilsException
	{
		addCell(String.valueOf(inrowId), incolumnId, String.valueOf(indataValue));
	}

	/**
	 * Adds a cell to the dataset. The rowId specifies row assignment. columnId specifies the column name.
	 * Column indices are generated internally. 
	 * 
	 * @param inrowId
	 * @param incolumnId
	 * @param indataValue
	 * @throws IKodaUtilsException
	 */
	public void addCell(long inrowId, String incolumnId, float indataValue) throws IKodaUtilsException
	{
		addCell(String.valueOf(inrowId), incolumnId, String.valueOf(indataValue));
	}

	/**
	 * Adds a cell to the dataset. The rowId specifies row assignment. columnId specifies the column name.
	 * Column indices are generated internally. 
	 * 
	 * @param inrowId
	 * @param incolumnId
	 * @param indataValue
	 * @throws IKodaUtilsException
	 */
	public void addCell(long inrowId, String incolumnId, int indataValue) throws IKodaUtilsException
	{
		addCell(String.valueOf(inrowId), incolumnId, String.valueOf(indataValue));
	}

	/**
	 * Adds a cell to the dataset. The rowId specifies row assignment. columnId specifies the column name.
	 * Column indices are generated internally. 
	 * @param inrowId
	 * @param incolumnId
	 * @param indataValue
	 * @throws IKodaUtilsException
	 */
	public void addCell(long inrowId, String incolumnId, long indataValue) throws IKodaUtilsException
	{
		addCell(String.valueOf(inrowId), incolumnId, String.valueOf(indataValue));
	}

	/**
	 * Adds a cell to the dataset. The rowId specifies row assignment. columnId specifies the column name.
	 * Column indices are generated internally. 
	 * @param inrowId
	 * @param incolumnId
	 * @param indataValue
	 * @throws IKodaUtilsException
	 */
	public void addCell(long inrowId, String incolumnId, String indataValue) throws IKodaUtilsException
	{
		addCell(String.valueOf(inrowId), incolumnId, indataValue);
	}

	/**
	 * Adds a cell to the dataset. The rowId specifies row assignment. columnId specifies the column name.
	 * Column indices are generated internally. 
	 * @param inrowId
	 * @param incolumnId
	 * @param indataValue
	 * @throws IKodaUtilsException
	 */
	public synchronized void addCell(String inrowId, String incolumnId, String indataValue) throws IKodaUtilsException
	{
		try
		{

			if (null == inrowId || null == incolumnId || null == indataValue || inrowId.isEmpty()
					|| incolumnId.isEmpty())
			{
				logger.warn("NULL||EMPTY: rowid:" + inrowId + " col:" + incolumnId + " data:" + indataValue, new Exception());

				return;
			}
			String columnId = cleanString(incolumnId);
			if (!columnId.substring(0, 1).matches("^[A-Za-z0-9]"))
			{
				columnId = "ZZ_" + columnId;
			}
			String dataValue = cleanString(indataValue);
			String rowId = cleanString(inrowId);

			HashMap<String, String> row = data.get(rowId);
			if (null == row)
			{
				row = new HashMap<>();
				row.put(pkColumnName, cleanString(rowId));
				data.put(rowId, row);

			}

			String value = row.get(columnId);
			if (null == value)
			{
				row.put(columnId, dataValue);
			}

			if (null == columnHeadings.get(columnId))
			{
				columnHeadings.put(columnId, columnId);
			}

			if (null != targetColumnName && columnId.equals(targetColumnName))
			{
				if (null == targetMap.get(dataValue))
				{
					logger.debug("targetColumnName: "+targetColumnName);
					if (dataValue.isEmpty() || dataValue.trim().isEmpty())
					{
						dataValue = BLANK;
						row.put(columnId, BLANK);
					}
					targetMap.put(dataValue, Integer.valueOf(targetCount));
					targetCount++;
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getClass(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}
	
	protected String cleanFullPath(String path, String fileName)
	{
		if(path.trim().endsWith(File.separator))
		{
			return path.trim() + fileName;
		}
		else
		{
			return path.trim() + File.separator+fileName;
		}
	}

	protected boolean arrayContains(String[] array, String str)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equalsIgnoreCase(str))
			{
				return true;
			}
		}
		return false;
	}

	protected String cleanString(String s)
	{
		String out = s;
		if (s.contains(COMMA))
		{
			out = s.replace(COMMA, "-");
		}
		if (s.contains(SEMICOLON))
		{
			out = s.replace(SEMICOLON, "-");
		}
		if (s.contains(ESCAPE))
		{
			out = s.replace(ESCAPE, "|");
		}
		return out.trim();
	}

	/**
	 * Clears the data in the columnMap, targetMap and all rows of the dataset.
	 * Metadata, such as keyspaceName and targetName remain. References to this instance 
	 * in Spreadsheet also remain
	 */
	public synchronized void clearAll()
	{
		clearData();
		columnHeadings.clear();
		targetMap.clear();
	}

	/**
	 * Clears the data in  all rows of the dataset.
	 * ColumMap and targetMap data remain. Metadata, such as keyspaceName and targetName remain. References to this instance 
	 * in Spreadsheet also remain
	 */
	public synchronized void clearData()
	{
		data.clear();
	}

	/**
	 * @return
	 */
	public int columnCount()
	{
		return columnHeadings.size();
	}

	private String columnHeadsForCommonsCSV(CSVRecord record)
	{
		Map<String, String> entries = record.toMap();
		Iterator<String> itr = entries.keySet().iterator();
		StringBuilder sb = new StringBuilder();
		while (itr.hasNext())
		{
			String colName = itr.next();
			sb.append(colName);
			if (itr.hasNext())
			{
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * If a print block method saved data to disk (typically in order to reduce memory load),
	 * then this command will merge all blocks into a single data set.
	 * @param fileName
	 * @throws IKodaUtilsException
	 */
	public abstract void finalizeAndJoinBlocks(String fileName) throws IKodaUtilsException;

	protected String generateColumnNames() throws IKodaUtilsException
	{
		return generateColumnNames(new ArrayList<String>());
	}

	
	protected String generateColumnNames(List<String> columnsToPrint) throws IKodaUtilsException
	{
		try
		{
			Map<String,String>colHeadsToPrint=selectColumnHeadsToPrint(columnsToPrint);
			

			if (null != targetColumnName)
			{
				colHeadsToPrint.put(targetColumnName, targetColumnName);
			}

			StringBuilder columnNames = new StringBuilder();
			Iterator<String> itr = colHeadsToPrint.keySet().iterator();

			while (itr.hasNext())
			{

				String s = itr.next();

				columnNames.append(cleanString(s));
				columnNames.append(COMMA);
			}

			columnNames.append(END);
			return columnNames.toString();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}
	
	
	/**
	 * @return
	 */
	public Map<String, String> getColumnHeadings()
	{
		return columnHeadings;
	}
	
	/**
	 * @return
	 */
	public String[] getColumnNames()
	{
		return columnHeadings.keySet().toArray(new String[columnHeadings.keySet().size()]);
	}

	/**
	 * @return
	 */
	public Map<String, HashMap<String, String>> getData()
	{
		return data;
	}

	/**
	 * @return
	 */
	public String getKeyspaceName()
	{
		if(keyspaceName.isEmpty())
		{
			logger.warn("WARN: Keyspace Name is empty. Using processor name instead: "+name);
			return name;
		}
		return keyspaceName;
	}

	/**
	 * @return
	 */
	public String getKeyspaceUUID()
	{
		return keyspaceUUID;
	}
	
	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public final String getPathToDirectory()
	{
		return pathToDir;
	}

	/**
	 * @return
	 */
	public String getPkColumnName()
	{
		return pkColumnName;
	}

	/**
	 * Project prefix is a convenience user defined name that is used to prefix all saved output files.
	 * @return
	 */
	public String getProjectPrefix()
	{
		return projectPrefix;
	}

	/**
	 * The column name that defines the label column for a LIBSVM file
	 * @return
	 */
	public String getTargetColumnName()
	{
		return targetColumnName;
	}

	/**
	 * @param targetName
	 * @return
	 */
	public Integer getTargetIdx(String targetName)
	{
		return targetMap.get(targetName);
	}

	/**
	 * @return
	 */
	public Map<String, Integer> getTargetMap()
	{
		return targetMap;
	}

	/**
	 * @param targetIdx
	 * @return
	 */
	public String getTargetName(Integer targetIdx)
	{
		if (targetMap.size() != targetMapInverse.size())
		{
			targetMapInverse.clear();
			targetMapInverse = targetMap.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		}
		return targetMapInverse.get(targetIdx);
	}

	protected String getTargetValue() throws IKodaUtilsException
	{
		try
		{

			if (null == targetMap.get(targetColumnName))
			{

				return "-";
			}

			return targetMap.get(targetColumnName).toString();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * @param columnId
	 * @return
	 * @throws IKodaUtilsException
	 */
	public boolean hasColumnName(String columnId) throws IKodaUtilsException
	{
		try
		{
			if (null == columnHeadings.get(columnId.trim()))
			{
				return false;
			}
			return true;
		}
		catch (Exception e)
		{
			logger.error(e.getClass(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	/**
	 * Convenience method to show top n rows as String
	 * @param rowCount
	 * @return
	 */
	public String head(int rowCount)
	{

		Iterator<String> ditr = data.keySet().iterator();
		int count = 0;
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(END);
		sb.append(pathToDir);
		sb.append(END);
		sb.append("Row Count: ");
		sb.append(rowCount());
		sb.append(END);
		sb.append("Col Count: ");
		sb.append(columnCount());
		sb.append(END);
		sb.append(END);

		while (ditr.hasNext())
		{

			String rowKey = ditr.next();
			sb.append(rowKey);
			sb.append(COMMA);
			HashMap<String, String> hmRow = data.get(rowKey);
			Iterator<String> hmRowItr = hmRow.keySet().iterator();
			while (hmRowItr.hasNext())
			{
				String key = hmRowItr.next();
				sb.append(key);
				sb.append(COMMA);
				sb.append(hmRow.get(key));
				sb.append(SPACE + SPACE);
			}
			sb.append(END);
			count++;
			if (count >= rowCount)
			{
				break;
			}

		}
		return sb.toString();
	}

	/**
	 * Returns all metadata about the dataset
	 * @return
	 */
	public String info()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n name: "+name);
		sb.append("\n targetColumnName: "+targetColumnName);
		sb.append("\n pkColumnName: "+pkColumnName);
		sb.append("\n projectPrefix: "+projectPrefix);
		sb.append("\n pathToDir: "+pathToDir);
		sb.append("\n keyspaceName: "+keyspaceName);
		sb.append("\n keyspaceUUID: "+keyspaceUUID);
		sb.append("\n columnHeadings count: "+columnHeadings.size());
		sb.append("\n targetMap count: "+targetMap.size());
		sb.append("\n rows count: "+data.size());
		return sb.toString();
				
	}

	protected boolean isLabel(String key)
	{
		// logger.debug(key);
		if (key.trim().equalsIgnoreCase(targetColumnName))
		{

			return true;

		}
		return false;
	}

	/**
	 * 
	 * @throws IKodaUtilsException
	 */
	public abstract void printCsvBlock() throws IKodaUtilsException;

	/**
	 * @param fileName
	 * @throws IKodaUtilsException
	 */
	public abstract void printCsvBlock(String fileName) throws IKodaUtilsException;

	/**
	 * Removes column with specified column name
	 * @param columnId
	 * @throws IKodaUtilsException
	 */
	public void removeColumn(String columnId) throws IKodaUtilsException
	{
		try
		{
			removeColumnHeader(columnId);

			Iterator<String> itrRows = data.keySet().iterator();
			while (itrRows.hasNext())
			{
				String key = itrRows.next();

				HashMap<String, String> row = data.get(key);
				Iterator<String> itrCells = row.keySet().iterator();
				String colId = itrCells.next();
				if (colId.equals(columnId))
				{
					itrCells.remove();
				}
			}

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);

		}
	}

	private void removeColumnHeader(String columnId) throws IKodaUtilsException
	{
		try
		{

			Iterator<String> itrCh = columnHeadings.keySet().iterator();
			while (itrCh.hasNext())
			{
				String key = itrCh.next();
				if (key.equals(columnId))
				{
					itrCh.remove();
				}
			}
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	/**
	 * Removes all rows which contain value  in column named columnId
	 * @param columnId
	 * @param value
	 * @throws IKodaUtilsException
	 */
	public void removeRowByCellValue(String columnId, String value) throws IKodaUtilsException
	{
		try
		{

			Iterator<String> itrRows = data.keySet().iterator();
			while (itrRows.hasNext())
			{
				String key = itrRows.next();

				HashMap<String, String> row = data.get(key);
				Iterator<String> itrCells = row.keySet().iterator();
				String colId = itrCells.next();
				if (colId.equals(columnId))
				{
					String cellValue = row.get(colId);
					if (cellValue.equals(value))
					{
						itrRows.remove();
					}

				}

			}

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);

		}
	}
	
	/**
	 * Converts all columns to lower case. Where there are upper and lower case columns that have values in the same row,
	 * the lower case value remains, the uppercase is removed.
	 * @throws IKodaUtilsException
	 */
	public void columnsToLowerCase() throws IKodaUtilsException
	{
		try
		{
			
			Map<String, HashMap<String, String>> newdata = new HashMap<>();
			Iterator<String> itrRows = data.keySet().iterator();
			while (itrRows.hasNext())
			{
				String key = itrRows.next();

				HashMap<String, String> row = data.get(key);
				HashMap<String, String> newrow=new HashMap<>();
				Iterator<String> itrCells = row.keySet().iterator();
				String value = "";
				while (itrCells.hasNext())
				{
					String colId = itrCells.next();


						value = row.get(colId);

						itrCells.remove();

						newrow.put(colId.toLowerCase(), value);
						renameColumnHeader(colId, colId.toLowerCase());
				}
				newdata.put(key, newrow);
			}
			data= newdata;
			
		}
		catch(Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(),e);
		}
	}

	/**
	 * @param oldName
	 * @param newName
	 * @throws IKodaUtilsException
	 */
	public void renameColumn(String oldName, String newName) throws IKodaUtilsException
	{
		try
		{

			Iterator<String> itrRows = data.keySet().iterator();
			while (itrRows.hasNext())
			{
				String key = itrRows.next();

				HashMap<String, String> row = data.get(key);
				Iterator<String> itrCells = row.keySet().iterator();
				String value = "";
				boolean found = false;
				while (itrCells.hasNext())
				{
					String colId = itrCells.next();
					if (colId.equals(oldName))
					{

						value = row.get(colId);

						itrCells.remove();
						found = true;
					}
				}
				if (found)
				{
					row.put(newName, value);
				}
			}

			renameColumnHeader(oldName, newName);

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);

		}
	}

	private void renameColumnHeader(String oldName, String newName) throws IKodaUtilsException
	{
		try
		{

			Iterator<String> itrCh = columnHeadings.keySet().iterator();
			while (itrCh.hasNext())
			{
				String key = itrCh.next();
				if (key.equals(oldName))
				{
					itrCh.remove();
				}
			}
			columnHeadings.put(newName, newName);

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	/**
	 * @return
	 */
	public int rowCount()
	{
		return data.size();
	}

	protected Map<String,String> selectColumnHeadsToPrint(List<String> columnsToPrint)
	{
		if(columnsToPrint.isEmpty())
		{
			return columnHeadings;
		}
		Map<String,String>colHeadsToPrint=new HashMap<>();
		Iterator<String> itr1=columnHeadings.keySet().iterator();
		while(itr1.hasNext())
		{
			String colName=itr1.next();
			if(columnsToPrint.contains(colName))
			{
				colHeadsToPrint.put(colName, colName);
			}
		}
		return colHeadsToPrint;
	}

	/**
	 * @param keyspaceName
	 */
	public void setKeyspaceName(String keyspaceName)
	{
		this.keyspaceName = keyspaceName.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "");
	}

	/**
	 * @param keyspaceUUID
	 */
	public void setKeyspaceUUID(String keyspaceUUID)
	{
		this.keyspaceUUID = keyspaceUUID;
	}

	/**
	 * @param f_rowId
	 */
	public void setPkColumnName(String f_rowId)
	{
		this.pkColumnName = f_rowId;
	}

	/**
	 * Project prefix is a convenience user defined name that is used to prefix all saved output files.
	 * @param projectPrefix
	 */
	public synchronized void setProjectPrefix(String projectPrefix)
	{
		logger.info("Project prefix set to: " + projectPrefix);
		if (!projectPrefix.endsWith("-"))
		{
			this.projectPrefix = projectPrefix + "-";
		}
		else
		{
			this.projectPrefix = projectPrefix;
		}
	}
	
	
	
	/**
	 * The column name that defines the label column for a LIBSVM file
	 * @param targetColumnName
	 */
	public synchronized void setTargetColumnName(String targetColumnName)
	{
		logger.info("Target column's name set to: " + targetColumnName);
		this.targetColumnName = targetColumnName;
	}

	protected String  timeStampDataName(String dataName, Date date)
		  {

		    SimpleDateFormat format = new SimpleDateFormat("y-M-d-HH-mm-ss");
		    String dataNameTs = dataName.replaceAll("_", "-");
		    logger.debug("dataName " + dataName);


		    if (dataNameTs.contains("-targetMap"))
		    {
		      dataNameTs = dataName.replace("-targetMap", "");
		      dataNameTs = dataNameTs+"-"+format.format(date)+"-targetMap";
		    }
		    else if (dataNameTs.contains("-columnMap"))
		    {
		      dataNameTs = dataName.replace("-columnMap", "");
		      dataNameTs = dataNameTs+"-"+format.format(date)+"-columnMap";
		    }
		    else
		    {
		      dataNameTs = dataNameTs+"-"+format.format(date);
		    }
		    return dataNameTs;
		  }

	protected String truncatePathSuffix(String savePath)
	{

		if (savePath.toUpperCase().endsWith(".TXT") || savePath.toUpperCase().endsWith(".CSV"))
		{
			return savePath.substring(0, savePath.length() - 4);
		}
		else if (savePath.toUpperCase().trim().endsWith(LIBSVM.toUpperCase()))
		{
			return savePath.substring(0, savePath.length() - 7);
		}

		return savePath;

	}
	
	
	/**
	 * Validates data integrity
	 * @return
	 * @throws IKodaUtilsException
	 */
	public boolean validate() throws IKodaUtilsException
	{
		try
		{
			Set<String> colSet= new HashSet<>();
			Iterator<String> dataItr= data.keySet().iterator();
			while(dataItr.hasNext())
			{
				String key = dataItr.next();
				HashMap<String,String> row=data.get(key);
				Iterator<String> rowItr = row.keySet().iterator();
				while(rowItr.hasNext())
				{
					String colName=rowItr.next();
					colSet.add(colName);
				}
			}
			
			logger.info("Column Set: "+colSet.size());
			logger.info("Column Heads: "+columnHeadings.size());
			if(colSet.size()==columnHeadings.size())
			{
				logger.info("Data Validation Succeeded.");
				return true;
			}
			else
			{
				if(colSet.size()>columnHeadings.size())
				{
					colSet.removeAll(columnHeadings.keySet());
					logger.warn("COLUMNS IN COLSET ONLY: "+colSet.toString());
				}
				else
				{
					Set<String> colHeads=columnHeadings.keySet();
					colHeads.removeAll(colSet);
					logger.warn("COLUMNS IN COLUMN HEADS ONLY: "+colHeads.toString());
					
				}
				return false;
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			return false;
		}
	}


}