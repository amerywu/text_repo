package ikoda.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;



public class LibSvmProcessor extends CSVSpreadsheetCreator
{

	final static String AUTO_ID = "auto_id";
	private Integer rowIdColIndex = 0;

	private LibSvmProcessor(Map<String, HashMap<String, String>> indata, Map<String, Integer> intargetMap,Map<String, String> incolumnHeadings,Logger inLogger, String inPathToDir)
	{
		super(inLogger,inPathToDir);
		data=indata;
		targetMap=intargetMap;
		columnHeadings=incolumnHeadings;
		targetCount=targetMap.size();
		name = String.valueOf(System.currentTimeMillis());

	}
	
	protected LibSvmProcessor(String inname, Logger inLogger, String targetColumnName, String uidColumn, String inPath)
	{
		super(inname, inLogger, inPath);
		super.setTargetColumnName(targetColumnName);
		super.setPkColumnName(uidColumn);
	}

	private void addAllValues(Map<Integer, Map<Integer, Map<Integer, Double>>> rowsMap,
			Map<Integer, String> columnIndexMap) throws IKodaUtilsException
	{
		try
		{
			Iterator<Integer> itr = rowsMap.keySet().iterator();
			logger.info("addAllValues ");
			while (itr.hasNext())
			{
				Integer uid = itr.next();
				Map<Integer, Map<Integer, Double>> rowMap = rowsMap.get(uid);
				Iterator<Integer> rowMapItr = rowMap.keySet().iterator();
				Integer label = rowMapItr.next();
				Map<Integer, Double> valuesMap = rowMap.get(label);
				Iterator<Integer> valuesItr = valuesMap.keySet().iterator();
				addCell(uid, targetColumnName, findColumnName(label));
				while (valuesItr.hasNext())
				{
					Integer columnIndex = valuesItr.next();
					Double value = valuesMap.get(columnIndex);
					String columnString = columnIndexMap.get(columnIndex);
					// System.out.print("-");
					addCell(uid, columnString, value);
				}
			}
			logger.info("addAllValues done");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}
	
	
	
	private String findColumnName(Integer idx)
	{
		Iterator<Entry<String, Integer>> itr = targetMap.entrySet().iterator();
		while (itr.hasNext())
		{
			Entry<String, Integer> e = itr.next();
			if (e.getValue().equals(idx))
			{
				return e.getKey();
			}
		}
		return "";
	}

	private String getTargetName(String line, int spaceIndex)
	{
		try
		{
			String strTargetIdx = line.substring(0, spaceIndex);
			Double targetIdx = Double.valueOf(strTargetIdx.trim());
			return this.getTargetName(targetIdx.intValue());
		}
		catch (Exception e)
		{
			logger.warn(e.getMessage(), e);
			return "NA";
		}
	}

	private void loadColumnMap(String strpath, String fileCoreName, Map<Integer, String> columnIndexMap)
			throws IKodaUtilsException, IOException
	{
		try
		{
			Path path = Paths.get(strpath, fileCoreName);

			Charset charset = Charset.forName("ISO-8859-1");

			List<String> lines = Files.readAllLines(path, charset);

			char comma = ',';
			char closeBracket = ')';
			char openbracket = '(';

			for (String s : lines)
			{

				int commaIndex = s.indexOf(comma);
				int lastBracket = s.indexOf(closeBracket);
				int firstBracket = s.indexOf(openbracket);
				String columnString = s.substring(commaIndex + 1, lastBracket);
				Integer columnNumeric = Integer.valueOf(s.substring(firstBracket + 1, commaIndex));
				columnIndexMap.put(columnNumeric, columnString);

				if (columnString.equals(pkColumnName))
				{
					rowIdColIndex = columnNumeric;
				}
			}

			logger.info("loadColumnMap " + columnIndexMap.size());
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	
	/**
	 * Loads a LIBSVM file from Hadoop (Currently assumes coalesced to 1 partition)
	 * @param fileCoreName
	 * @throws IKodaUtilsException
	 */
	public void loadLibsvm(String fileCoreName) throws IKodaUtilsException
	{
		try
		{
			logger.info("LOADING " + pathToDir + File.separator + fileCoreName);
			logger.info("UID COL " + pkColumnName + " targetColumnName " + targetColumnName);
			Map<Integer, String> columnIndexMap = new HashMap<>();
			loadTargetMap(truncatePathSuffix(pathToDir + File.separator + fileCoreName + TARGET_MAP_SUFFIX), "part-00000");
			loadColumnMap(truncatePathSuffix(pathToDir + File.separator + fileCoreName + COLUMN_MAP_SUFFIX), "part-00000", columnIndexMap);
			loadLibsvm1(pathToDir + File.separator + fileCoreName, "part-00000", columnIndexMap);
			logger.info("\n\nLoaded " + head(1) + "\n\n");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	
	/**
	 * Loads a LIBSVM file from Hadoop (Currently assumes coalesced to 1 partition)
	 * @param fileCoreName
	 * @param autoGenerateIDColumn
	 * @throws IKodaUtilsException
	 */
	public void loadLibsvm(String fileCoreName, boolean autoGenerateIDColumn) throws IKodaUtilsException
	{
		try
		{

			if (autoGenerateIDColumn)
			{
				pkColumnName = AUTO_ID;
			}
			loadLibsvm(fileCoreName);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}
	
	

	private void loadLibsvm1(String strpath, String fileCoreName, Map<Integer, String> columnIndexMap)
			throws IKodaUtilsException
	{
		try
		{

			////// protected Map<String, HashMap<String, String>> data = new HashMap<>();

			if (null == targetColumnName)
			{
				logger.warn("\n\ntargetColumnName is null. Aborting now\n\n");
			}
			logger.info("Using " + pkColumnName + " as uid. This can be changed programmatically through the API.");

			TicToc tt = new TicToc();
			logger.info(tt.tic("loadLibsvm1", 100));
			// logger.info("targetMap start load "+targetMap);
			Path path = Paths.get(strpath, fileCoreName);

			Charset charset = Charset.forName("ISO-8859-1");

			List<String> lines = Files.readAllLines(path, charset);

			char space = ' ';
			String colon = ":";

			Map<String, String> columnHeadingsTemp = new TreeMap<>();
			columnHeadingsTemp.put(targetColumnName, targetColumnName);
			int count = 0;
			for (String s : lines)
			{

				HashMap<String, String> hmRow = new HashMap<>();

				// System.out.print(".");
				int spaceIndex = s.indexOf(space);
				// Double temp = Double.parseDouble(s.substring(0, spaceIndex));
				// Integer label = temp.intValue();
				// logger.debug("label "+s.substring(0, spaceIndex));

				String values = s.substring(spaceIndex, s.length()).trim();
				String[] valuesArray = values.split(" ");

				// Map<Integer, Map<Integer, Double>> oneRowValuesMap = new HashMap();
				// oneRowValuesMap.put(label, new HashMap<Integer, Double>());
				// logger.debug("valuecount "+valuesArray.length);

				for (int i = 0; i < valuesArray.length; i++)
				{

					String[] valueColumnPair = valuesArray[i].split(colon);
					Integer column = Integer.valueOf(valueColumnPair[0]);
					// logger.debug(columnIndexMap.get(column)+" "+valueColumnPair[1]);

					String colName = columnIndexMap.get(column);

					hmRow.put(colName, valueColumnPair[1]);
					columnHeadingsTemp.put(colName, colName);
				}

				String uidF = hmRow.get(pkColumnName);
				// logger.debug("got uid"+uidF);

				if (null != uidF)
				{

					hmRow.put(targetColumnName, getTargetName(s, spaceIndex));
					data.put(uidF, hmRow);
				}
				else
				{
					if (!pkColumnName.equals(AUTO_ID))
					{

						logger.warn("No rowId. Ommitting");
						continue;

					}
					else
					{
						hmRow.put(targetColumnName, getTargetName(s, spaceIndex));
						// logger.debug("putting "+ hmRow);
						String uidt = String.valueOf(count);
						count++;
						// logger.debug(uidt);
						data.put(uidt, hmRow);
					}
				}
			}
			columnHeadings = columnHeadingsTemp;
			logger.info(tt.toc("loadLibsvm1"));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}

	}

	private void loadLibsvmOld(String strpath, String fileCoreName, Map<Integer, String> columnIndexMap)
			throws IKodaUtilsException
	{
		try
		{
			TicToc tt = new TicToc();
			tt.tic("loadLibsvm");

			Path path = Paths.get(strpath, fileCoreName);
			logger.info("Loading data from " + strpath);

			Charset charset = Charset.forName("ISO-8859-1");
			logger.info("reading all lines ");
			List<String> lines = Files.readAllLines(path, charset);
			logger.info("Retrieved with line count " + lines.size());
			char space = ' ';
			String dspace = "  ";
			String colon = ":";
			Map<Integer, Map<Integer, Map<Integer, Double>>> rowsMap = new HashMap();
			logger.info("Retrieved with line count " + lines.size());

			for (String sorg : lines)
			{
				// System.out.print(".");
				String s =sorg.replace(dspace, " ");
				logger.debug(s);
				int spaceIndex = s.indexOf(space);
				Double temp = Double.parseDouble(s.substring(0, spaceIndex));
				Integer label = temp.intValue();
				String values = s.substring(spaceIndex, s.length()).trim();
				String[] valuesArray = values.split(" ");

				Map<Integer, Map<Integer, Double>> oneRowValuesMap = new HashMap();
				oneRowValuesMap.put(label, new HashMap<Integer, Double>());

				for (int i = 0; i < valuesArray.length; i++)
				{
					String[] valueColumnPair = valuesArray[i].split(colon);
					logger.debug(valueColumnPair[0]+":"+valueColumnPair[1]);
					Integer column = Integer.valueOf(valueColumnPair[0]);
					Double value = Double.valueOf(valueColumnPair[1]);
					oneRowValuesMap.get(label).put(column, value);
				}

				Map<Integer, Double> hm = oneRowValuesMap.get(label);

				Integer uid = null;
				if (null != hm.get(rowIdColIndex))
				{
					uid = hm.get(rowIdColIndex).intValue();
					rowsMap.put(uid, oneRowValuesMap);
				}
				else
				{
					if (!pkColumnName.equals(AUTO_ID))
					{
						logger.warn("No rowId. Ommitting");
						continue;
					}
					else
					{
						rowsMap.put(new Long(IDGenerator.getInstance().nextIDInMem()).intValue(), oneRowValuesMap);
					}
				}
			}

			logger.info(tt.toc("loadLibsvm"));
			logger.info("Calling addAllValues.");
			addAllValues(rowsMap, columnIndexMap);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}

	}

	/**
	 * Loads a LIBSVM file from local disk
	 * @param fileCoreName
	 * @throws IKodaUtilsException
	 */
	public void loadLibsvmPJ(String fileCoreName) throws IKodaUtilsException
	{
		loadLibsvmPJ(pathToDir, fileCoreName);
	}

	/**
	 * Loads a LIBSVM file from local disk
	 * @param path
	 * @param fileCoreName
	 * @throws IKodaUtilsException
	 */
	public void loadLibsvmPJ(String path, String fileCoreName) throws IKodaUtilsException
	{
		try
		{
			logger.info("LOADING " + path + File.separator + fileCoreName);
			logger.info("UID COL " + pkColumnName + " targetColumnName " + targetColumnName);
			Map<Integer, String> columnIndexMap = new HashMap();
			loadTargetMap(path, truncatePathSuffix(fileCoreName) + TARGET_MAP_SUFFIX);
			loadColumnMap(path, truncatePathSuffix(fileCoreName) + COLUMN_MAP_SUFFIX, columnIndexMap);
			loadLibsvmOld(path, truncatePathSuffix(fileCoreName) + LIBSVM, columnIndexMap);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e.getCause());
		}
	}

	private void loadTargetMap(String strpath, String fileCoreName) throws IKodaUtilsException, IOException
	{
		try
		{
			logger.debug(strpath);
			logger.debug(fileCoreName);
			Path path = Paths.get(strpath, fileCoreName);

			Charset charset = Charset.forName("ISO-8859-1");

			List<String> lines = Files.readAllLines(path, charset);

			char comma = ',';
			char closeBracket = ')';

			for (String s : lines)
			{
				int commaIndex = s.indexOf(comma);
				int lastBracket = s.indexOf(closeBracket);
				String targetString = s.substring(1, commaIndex);
				Double targetNumeric = Double.valueOf(s.substring(commaIndex + 1, lastBracket));
				targetMap.put(targetString, Integer.valueOf(targetNumeric.intValue()));
			}

			logger.info("loadedTargetMap " + targetMap.size());
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}

	}

	/**
	 * Merges new columns into the dataset
	 * @param toMerge
	 */
	public void mergeColumns(LibSvmProcessor toMerge)
	{
		try
		{
			columnHeadings.putAll(toMerge.columnHeadings);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Merges another instance of LibSvmProcessor into this one 
	 * @param toMerge
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String mergeIntoLibsvm(LibSvmProcessor toMerge) throws IKodaUtilsException
	{
		try
		{
			TicToc tt = new TicToc();
			logger.info(tt.tic("mergeIntoLibsvm NEW"));

			if (!toMerge.targetColumnName.equals(this.targetColumnName))
			{
				logger.warn("ABORTING " + this.targetColumnName
						+ " does not match target column name of incoming dataset " + toMerge.targetColumnName);
			}
			if (!toMerge.pkColumnName.equals(this.pkColumnName))
			{
				logger.warn("ABORTING " + this.pkColumnName + " does not match pk column name of incoming dataset "
						+ toMerge.pkColumnName);
			}
			mergeColumns(toMerge);

			Iterator<String> itrDataToMerge = toMerge.data.keySet().iterator();
			while (itrDataToMerge.hasNext())
			{
				String rowIdToMerge = itrDataToMerge.next();
				HashMap<String, String> hmRowExtant = data.get(rowIdToMerge);
				if (null == data.get(rowIdToMerge))
				{
					if (mergeLabel(rowIdToMerge, toMerge))
					{
						data.put(rowIdToMerge, toMerge.data.get(rowIdToMerge));
					}
				}
				else
				{
					if (mergeLabel(rowIdToMerge, toMerge))
					{

						Iterator<String> itr = toMerge.data.get(rowIdToMerge).keySet().iterator();
						while (itr.hasNext())
						{
							String key = itr.next();
							String value = toMerge.data.get(rowIdToMerge).get(key);

							hmRowExtant.put(key, value);

						}

					}
				}
			}
			logger.info(tt.toc("mergeIntoLibsvm NEW"));
			return "";
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	/**
	 * Merges another instance of LibSvmProcessor into this one. Validates data integrity. This is slower.
	 * @param toMerge
	 * @throws IKodaUtilsException
	 */
	public void mergeIntoLibsvmValidating(LibSvmProcessor toMerge) throws IKodaUtilsException
	{
		try
		{
			logger.info("mergeIntoLibsvmValidating");
			TicToc tt = new TicToc();

			if (!toMerge.targetColumnName.equals(this.targetColumnName))
			{
				logger.warn("ABORTING " + this.targetColumnName
						+ " does not match target column name of incoming dataset " + toMerge.targetColumnName);
			}
			if (!toMerge.pkColumnName.equals(this.pkColumnName))
			{
				logger.warn("ABORTING " + this.pkColumnName + " does not match pk column name of incoming dataset "
						+ toMerge.pkColumnName);
			}

			// logger.info("starting target map "+targetMap);

			logger.info(tt.tic("mergeIntoLibsvmValidating"));

			logger.info("Merging " + toMerge.rowCount() + " rows into data ");
			Map<String, HashMap<String, String>> dataToMerge = toMerge.data;
			Iterator<String> itrDataToMerge = dataToMerge.keySet().iterator();

			while (itrDataToMerge.hasNext())
			{
				String uid = itrDataToMerge.next();
				Map<String, String> rowValues = dataToMerge.get(uid);
				Iterator<String> itrRowValues = rowValues.keySet().iterator();
				while (itrRowValues.hasNext())
				{
					String columnName = itrRowValues.next();
					String value = rowValues.get(columnName);

					addCell(uid, columnName, value);
				}
			}
			logger.info(tt.toc("mergeIntoLibsvmValidating"));

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private boolean mergeLabel(String rowIdToMerge, LibSvmProcessor toMerge)
	{
		try
		{
			String labelNameToMerge = toMerge.data.get(rowIdToMerge).get(targetColumnName);

			if (null == labelNameToMerge)
			{
				logger.warn("Could not find target column in toMerge data: " + targetColumnName);
				return false;

			}

			Integer labelIdxToMerge = toMerge.getTargetIdx(labelNameToMerge);

			Integer labelIdxExtant = this.getTargetIdx(labelNameToMerge);

			if (null == labelIdxExtant)
			{
				logger.info("New Label " + labelNameToMerge);
				targetMap.put(labelNameToMerge, Integer.valueOf(targetCount));
			}
			else
			{
				toMerge.data.get(rowIdToMerge).put(targetColumnName, String.valueOf(labelNameToMerge));
			}
			return true;
		}
		catch (Exception e)
		{
			logger.warn(e.getMessage(), e);
			return false;
		}
	}

	private String sparkStreamColumnMap(Integer port, Map<String, Integer> columnIndicesMap, String serverurl,
			int serverport, String[] columnsToIgnore, Date date, String dataName) throws IKodaUtilsException
	{

		StreamToSpark sts1 = null;
		try
		{
			sts1 = new StreamToSpark(Integer.valueOf(port));
			List<String> colList = generateLibSvmColumnFile(columnIndicesMap, columnsToIgnore);
			String colNameMap = timeStampDataName(dataName+"-columnMap", date);
			String response = sparkNewDataNofitication(serverurl, serverport, String.valueOf(sts1.getPort()), localUrl,
					colNameMap);
			
			if (response.equals("SUCCESS"))
			{
				///////////////////////////////
				logger.info("Streaming column map");
				sts1.sendLine(StreamingConstants.IKODA_DATA_NAME + "=" + colNameMap);
				sts1.sendLine(StreamingConstants.IKODA_CASSANDRA_KEYSPACE + "=" + getKeyspaceName());
				for (String line : colList)
				{
					sts1.sendLine(line);
				}

				sts1.sendLine(StreamingConstants.IKODA_END_STREAM);
				sparkWaitOnStream(sts1);
				streamToSparkShutdown(sts1);
			}
			else
			{
				
				throw new IKodaUtilsException("\n\nFAILED to contact spark\n\n");
			}
			
			return response;
			
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
		finally
		{
			streamToSparkShutdown(sts1);
		}
	}
	
	private String sparkStreamDataAsLibSvm(Integer port, Map<String, Integer> columnIndicesMap, String serverurl,
			int serverport, Date date, String dataName) throws IKodaUtilsException
	{
		StreamToSpark sts1 = null;
		try
		{

			logger.info("Streaming data");
			logger.info("Row count in memory: " + data.size());
			sts1 = new StreamToSpark(Integer.valueOf(port));

			List<String> rows = new ArrayList<String>();

			String dataNamets = timeStampDataName(dataName, date);
			String response = sparkNewDataNofitication(serverurl, serverport, String.valueOf(sts1.getPort()), localUrl,
					dataNamets);
			if (response.equals("SUCCESS"))
			{
				List<String> idList = new ArrayList<>();
				Iterator<String> itrRowPKs = data.keySet().iterator();
				int count = 0;
				/** Iterate all rows */
				while (itrRowPKs.hasNext())
				{
					String id = itrRowPKs.next();
					LabelValuesTuple lvt = processLibSvmRow(id, columnIndicesMap, true);

					if (null == lvt)
					{
						logger.warn("Label is empty, dropping row");
						continue;
					}
					idList.add(id + END);
					count++;
					rows.add(lvt.getTarget() + SPACE + lvt.getValue());

				} // end all rows
				logger.info("Generated " + count + " rows.");

				sts1.sendLine(StreamingConstants.IKODA_DATA_NAME + "=" + dataName);
				sts1.sendLine(StreamingConstants.IKODA_CASSANDRA_KEYSPACE + "=" + getKeyspaceName());
				for (String line2 : rows)
				{

					sts1.sendLine(line2);
				}
				sts1.sendLine(StreamingConstants.IKODA_END_STREAM);

				sparkWaitOnStream(sts1);
			}
			else
			{
				throw new IKodaUtilsException("\n\nFAILED to contact spark\n\n");
			}
			return response;

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
		finally
		{
			streamToSparkShutdown(sts1);
		}
	}

	
	/**
	 * Streams data to Spark ikodaML app as LIBSVM format data plus a column map and a target map
	 * @param serverurl
	 * @param serverport
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String sparkStreamRunLibsvm(String serverurl, int serverport) throws IKodaUtilsException
	{
		logger.debug("sparkStreamRunLibsvm");

		return sparkStreamRunLibsvm(serverurl, serverport, new String[0]);
	}

	/**
	 * 
	 * Streams data to Spark ikodaML app as LIBSVM format data plus a column map and a target map
	 * @param serverurl
	 * @param serverport
	 * @param columnsToIgnore
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String sparkStreamRunLibsvm(String serverurl, int serverport, String[] columnsToIgnore)
			throws IKodaUtilsException
	{

		if (!sparkValidateStreamPorts(1))
		{
			throw new IKodaUtilsException(
					"Connection address validation failed for serverurl " + serverurl + " & serverport " + serverport);
		}
		logger.info("\n\nsparkStreamSetUp "+info());
		return sparkStreamSetUp(serverurl, serverport, columnsToIgnore);

		

	}

	private String sparkStreamSetUp(String serverurl, int serverport, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		try
		{
			String[] ports = localPorts.split(",");
			String response="";
			
			Date date=Calendar.getInstance().getTime();

			logger.debug("All sts initialized");
			logger.info("Streaming "+info());
			Map<String, Integer> columnIndicesMap = generateLibSvmColumnMap(columnsToIgnore);

			String dataName=getCassandraName();
			response=sparkStreamColumnMap(Integer.valueOf(ports[0]), columnIndicesMap, serverurl, serverport, columnsToIgnore,date,dataName);
			logger.info("Streamed Column Map on " + ports[0]);
			response=sparkStreamTargetMap(Integer.valueOf(ports[0]), serverurl, serverport,date,dataName);
			logger.info("Streamed Target Map on " + ports[0]);
			response=sparkStreamDataAsLibSvm(Integer.valueOf(ports[0]), columnIndicesMap, serverurl, serverport,date,dataName);
			logger.info("Streamed Data on " + ports[0]);
			logger.debug("Streaming complete\n");
			return response;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			ProcessStatus.incrementStatus("Failed to connect to spark");
			// sts.shutdown();
			return "FAILED";
		}

	}

	private String sparkStreamTargetMap(Integer port, String serverurl, int serverport, Date date, String dataName) throws IKodaUtilsException
	{
		StreamToSpark sts1 = null;
		try
		{
			sts1 = new StreamToSpark(Integer.valueOf(port));
			List<String> targetList = generateLibSvmTargetMap();

			logger.info("Streaming target map");
			String[] ports = localPorts.split(",");
			String targetMapName = timeStampDataName(dataName+"-targetMap", date);
			String response = sparkNewDataNofitication(serverurl, serverport, String.valueOf(sts1.getPort()), localUrl,
					targetMapName);
			if (response.equals("SUCCESS"))
			{
				sts1.sendLine(StreamingConstants.IKODA_DATA_NAME + "=" + targetMapName);
				sts1.sendLine(StreamingConstants.IKODA_CASSANDRA_KEYSPACE + "=" + getKeyspaceName());
				for (String line1 : targetList)
				{
					logger.debug("sending "+line1);
					sts1.sendLine(line1);
				}
				sts1.sendLine(StreamingConstants.IKODA_END_STREAM);

				sparkWaitOnStream(sts1);
			}
			else
			{
				throw new IKodaUtilsException("\n\nFAILED to contact spark\n\n");
			}
			return response;

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
		finally
		{
			streamToSparkShutdown(sts1);
		}

	}

	private void streamToSparkShutdown(StreamToSpark sts)
	{
		if (null != sts)
		{
			sts.shutdown();
		}
	}

	/**
	 * @param rowCount
	 * @return
	 * @throws IKodaUtilsException
	 */
	public LibSvmProcessor subsetLibsvmNoReplacement(int rowCount) throws IKodaUtilsException
	{
		try
		{
			logger.info("Subsetting from "+info());
			if(data.isEmpty())
			{
				return null;
			}
			HashMap<String,String> newColMap=new HashMap<>();
			Iterator<String> itr = data.keySet().iterator(); 
			int count =0;
			Map<String, HashMap<String, String>> subdata = new HashMap<>();
			
			
			while(itr.hasNext())
			{
				count++;
				
				if(count >= rowCount)
				{
					break;
				}
				String key=itr.next();
				HashMap<String, String> row=data.get(key);
				
				Iterator<String> itrRow= row.keySet().iterator();
				while(itrRow.hasNext())
				{
					String colName=itrRow.next();
					newColMap.put(colName, colName);
				}
				
				subdata.put(key,row );
				itr.remove();
			}
			
			
			LibSvmProcessor subsetLibsvm = new LibSvmProcessor(subdata, targetMap, newColMap, logger, pathToDir);
			subsetLibsvm.setPkColumnName(this.getPkColumnName());
			subsetLibsvm.setProjectPrefix(this.getProjectPrefix());
			subsetLibsvm.setKeyspaceName(this.getKeyspaceName());
			subsetLibsvm.setKeyspaceUUID(this.getKeyspaceUUID());
			subsetLibsvm.setLocalPorts(this.getLocalPorts());
			subsetLibsvm.setLocalUrl(this.getLocalUrl());
			subsetLibsvm.setTargetColumnName(this.getTargetColumnName());
			logger.info("Subset:  "+subsetLibsvm.info());
			return subsetLibsvm;
	
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(),e);
		}
	}

}
