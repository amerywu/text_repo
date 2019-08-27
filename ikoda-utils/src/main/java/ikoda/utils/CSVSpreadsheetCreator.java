package ikoda.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.Logger;

public class CSVSpreadsheetCreator extends AbstractSpreadsheetCreator
{

	private boolean printByBlockCalled = false;

	protected String localPorts;
	protected String localUrl;

	/**
	 * @param inLogger
	 * @param inPathToDir
	 */
	public CSVSpreadsheetCreator(Logger inLogger, String inPathToDir)
	{
		super("UnnamedSpreadsheet");
		columnHeadings.put(pkColumnName, pkColumnName);
		targetMap.put(BLANK, 0);
		pathToDir = inPathToDir;
		logger = inLogger;
	}

	/**
	 * @param inname
	 * @param inLogger
	 * @param inPathToDir
	 */
	public CSVSpreadsheetCreator(String inname, Logger inLogger, String inPathToDir)
	{
		super(inname);
		columnHeadings.put(pkColumnName, pkColumnName);
		targetMap.put(BLANK, 0);
		pathToDir = inPathToDir;

		logger = inLogger;
	}

	/**
	 * 
	 * Opens a CSV file and changes a column name
	 * @param path
	 * @param uidCol
	 * @param columnName
	 * @param replaceFrom
	 * @param replaceWith
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void changeCsvField(String path, String uidCol, String columnName, String replaceFrom,
			String replaceWith, String[] columnsToIgnore) throws IKodaUtilsException
	{
		try
		{
			FileReader in = null;
			try
			{
				if (!path.toUpperCase().contains(".CSV"))
				{
					logger.warn(path + "is not a csv file.");
					return;
				}
				in = new FileReader(path);
				Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
				for (CSVRecord record : records)
				{
					Map<String, String> entries = record.toMap();
					String uid = entries.get(uidCol);
					Iterator<String> itr = entries.keySet().iterator();
					String key = null;

					while (itr.hasNext())
					{
						key = itr.next();
						if (null == entries.get(key) || entries.get(key).isEmpty())
						{
							continue;
						}
						if (key.equals(columnName) && !entries.get(columnName).equals(targetColumnName))
						{

							String changedFrom = entries.get(columnName);
							String changedTo = changedFrom.replaceAll(replaceFrom, replaceWith);
							addCell(uid, key, changedTo);
						}
						else
						{

							addCell(uid, key, entries.get(key));
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e);
			}
			finally
			{
				if (null != in)
				{
					in.close();
				}
			}
			printCsvFinal(path);
			if (null != targetColumnName)
			{
				printLibSvmFinal(truncatePathSuffix(path) + LIBSVM, targetColumnName, columnsToIgnore);
			}
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private String convertToNumericLabel(String key, String value)
	{

		if (key.equalsIgnoreCase(targetColumnName))
		{

			Integer label = targetMap.get(value);

			if (null == label)
			{
				return EMPTY;
			}

			return label + SPACE;

		}
		return EMPTY;
	}

	private void deleteFile(String path)
	{
		try
		{
			Files.deleteIfExists(Paths.get(path));
		}
		catch (NoSuchFileException e)
		{
			logger.error(e);
		}
		catch (DirectoryNotEmptyException e)
		{
			logger.error(e);
		}
		catch (IOException e)
		{
			logger.error(e);
		}

	}

	/**
	 * 
	 * Emails the dataset in CSV format
	 * @param emailTo
	 * @param subject
	 * @param messageContent
	 * @throws IKodaUtilsException
	 */
	public synchronized void email(String emailTo, String subject, String messageContent) throws IKodaUtilsException
	{
		try
		{

			List<String> rows = generateRows();
			File f = makeTempFile(rows);

			EmailOut emailOut = new EmailOut();

			emailOut.addTO(emailTo);

			emailOut.setMessage(messageContent);
			emailOut.setSubject(subject);

			if (f.length() < 24000000)
			{
				emailOut.addAttachment(f);
			}

			EmailQueueSingleton.getInstance().addEmailOut(emailOut);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.toString(), e);
		}
	}

	/* (non-Javadoc)
	 * @see ikoda.utils.AbstractSpreadsheetCreator#finalizeAndJoinBlocks(java.lang.String)
	 */
	public synchronized void finalizeAndJoinBlocks(String fileName) throws IKodaUtilsException
	{
		finalizeAndJoinBlocks(fileName, true);
	}

	/**
	 * If a print block method saved data to disk (typically in order to reduce memory load),
	 * then this command will merge all blocks into a single data set.
	 * @param fileName
	 * @param dumpOldFiles
	 * @throws IKodaUtilsException
	 */
	public synchronized void finalizeAndJoinBlocks(String fileName, boolean dumpOldFiles) throws IKodaUtilsException
	{

		try
		{
			if (data.size() > 1)
			{

				printCsvBlock(fileName);
			}

			clearAll();
			String[] s = {};
			List<String> pathsUsedForCompile = processFinalizeAndJoinBlocks(fileName, s);

			// printCsvFinal(prefix + truncatePathSuffix(fileName) + ".csv");

			if (dumpOldFiles)
			{
				moveRecompiledFiles(pathsUsedForCompile);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}

	}

	/**
	 * If a print block method saved data to disk (typically in order to reduce memory load),
	 * then this command will merge all blocks into a single data set.
	 * @param fileName
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void finalizeAndJoinBlocksLibSvm(String fileName, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		finalizeAndJoinBlocksLibSvm(fileName, columnsToIgnore, "FINAL_");
	}

	/**
	 * If a print block method saved data to disk (typically in order to reduce memory load),
	 * then this command will merge all blocks into a single data set.
	 * @param fileName
	 * @param columnsToIgnore
	 * @param prefix
	 * @throws IKodaUtilsException
	 */
	public synchronized void finalizeAndJoinBlocksLibSvm(String fileName, String[] columnsToIgnore, String prefix)
			throws IKodaUtilsException
	{
		try
		{

			clearData();
			if (null == targetColumnName)
			{
				logger.warn("No targetColumn. Nothing to do. Aborting");
				return;
			}
			printLibSvmBlock(fileName, targetColumnName, columnsToIgnore);
			logger.info("Finalizing ...");
			List<String> pathsUsedForCompile = processFinalizeAndJoinBlocks(fileName, columnsToIgnore);

			logger.info("Printing ...");
			printLibSvmFinal(prefix + truncatePathSuffix(fileName) + LIBSVM, targetColumnName, columnsToIgnore);

			moveRecompiledFiles(pathsUsedForCompile);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}

	}

	/**
	 * Generates a CSV format String of the entire dataset. Handy for smaller datasets. Not suited for very large datasets.
	 * @param path
	 * @return
	 * @throws IKodaUtilsException
	 */
	public synchronized String generateCSVFile(String path) throws IKodaUtilsException
	{
		try
		{

			String fileName = "Report_" + System.currentTimeMillis() + ".csv";

			String fullPath = path + "temp";

			if (!(Paths.get(fullPath).toFile().exists()))
			{
				Files.createDirectories(Paths.get(fullPath));
			}

			Path to = Paths.get(fullPath + File.separator + fileName);
			// overwrite existing file, if exists\

			printByRowCsv(to);

			/**
			 * try (BufferedWriter writer = Files.newBufferedWriter(to,
			 * Charset.forName("UTF-8"))) { List<String> rows = generateRows();
			 * writeToFile(rows, writer);
			 * 
			 * } catch (IOException ex) {
			 * 
			 * logger.error(ex.getMessage(), ex); throw new
			 * IKodaUtilsException(ex.getMessage());
			 * 
			 * }
			 */
			return "temp" + "/" + fileName;

		}
		catch (Exception e)
		{

			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage());
		}
	}

	public List<String> generateLibSvmColumnFile(Map<String, Integer> hm, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		try
		{

			Iterator<String> itr = columnHeadings.keySet().iterator();
			int count = 1;

			List<String> list = new ArrayList<>();
			while (itr.hasNext())
			{

				String colName = itr.next();
				StringBuilder sb = new StringBuilder();
				if (arrayContains(columnsToIgnore, colName))
				{

					continue;
				}

				if (null != targetColumnName && colName.equals(targetColumnName))
				{

					continue;
				}

				sb.append(String.valueOf(count));
				sb.append(COMMA);
				sb.append(colName);
				sb.append(END);
				list.add(sb.toString());

				count++;
			}

			return list;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * @param columnsToIgnore
	 * @return
	 * @throws IKodaUtilsException
	 */
	public Map<String, Integer> generateLibSvmColumnMap(String[] columnsToIgnore) throws IKodaUtilsException
	{
		try
		{

			Map<String, Integer> hm = new TreeMap<>();
			Iterator<String> itr = columnHeadings.keySet().iterator();
			int count = 1;

			while (itr.hasNext())
			{

				String colName = itr.next();

				if (arrayContains(columnsToIgnore, colName))
				{

					continue;
				}
				if (null != targetColumnName && colName.equals(targetColumnName))
				{

					continue;
				}


				hm.put(cleanString(colName), Integer.valueOf(count));
				count++;
			}

			return hm;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}


	public List<LabelValuesTuple> generateLibSvmRows(String[] columnsToIgnore) throws IKodaUtilsException
	{
		List<LabelValuesTuple> rows = new ArrayList<LabelValuesTuple>();
		try
		{
			int count = 0;

			Iterator<String> itrRowPKs = data.keySet().iterator();

			Map<String, Integer> columnIndicesMap = generateLibSvmColumnMap(columnsToIgnore);

			/** Iterate all rows */
			while (itrRowPKs.hasNext())
			{
				String id = itrRowPKs.next();
				LabelValuesTuple lvt = processLibSvmRow(id, columnIndicesMap, false);

				if (null == lvt)
				{
					logger.warn("Label is empty, dropping row");
					continue;
				}

				count++;
				rows.add(lvt);

			} // end all rows
			logger.info("Generated " + count + " rows.");
			return rows;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	private List<String> generateLibSvmRowsAsString(String path, String[] columnsToIgnore) throws IKodaUtilsException
	{
		List<String> rows = new ArrayList<String>();
		try
		{
			int count = 0;

			Iterator<String> itrRowPKs = data.keySet().iterator();
			List<String> targetList = generateLibSvmTargetMap();
			saveLibSvmTargetMap(path, targetList);
			Map<String, Integer> columnIndicesMap = generateLibSvmColumnMap(columnsToIgnore);

			List<String> colList = generateLibSvmColumnFile(columnIndicesMap, columnsToIgnore);
			String savePathTruncated = truncatePathSuffix(path);
			saveColumnMap(colList, savePathTruncated);

			List<String> idList = new ArrayList();
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
			write(idList, truncatePathSuffix(path) + ID_MAP_SUFFIX, false);
			return rows;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	protected List<String> generateLibSvmTargetMap() throws IKodaUtilsException
	{
		try
		{

			Iterator<String> itr = targetMap.keySet().iterator();
			logger.info("targetColumnMap size " + targetMap.size());
			int count = 0;

			List<String> list = new ArrayList<String>();
			while (itr.hasNext())
			{
				StringBuilder sb = new StringBuilder();
				String s = itr.next();
				sb.append(s);
				sb.append(COMMA);
				sb.append(targetMap.get(s));
				sb.append(END);
				list.add(sb.toString());
			}

			return list;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	private List<String> generateRows() throws IKodaUtilsException
	{
		List<String> rows = new ArrayList<String>();
		try
		{
			rows.add(this.generateColumnNames());
			Iterator<String> itrRowPKs = data.keySet().iterator();

			while (itrRowPKs.hasNext())
			{

				StringBuilder sb = new StringBuilder();
				String s = itrRowPKs.next();
				Map<String, String> dataRow = data.get(s);

				processRow(columnHeadings, sb, dataRow);
				sb.append(END);

				rows.add(sb.toString());
			}
			return rows;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	protected String getCassandraName()
	{
		return "|uuids|"+getKeyspaceUUID()+"|uuide||kss|"+getKeyspaceName()+"|kse|";
	}

	private List<String> getCSvBlockPaths(String fileName) throws IKodaUtilsException
	{
		List<String> files = new ArrayList();
		DirectoryStream<Path> stream = null;
		logger.info("coreFileName: " + fileName);
		logger.info("Finding files ...");

		try
		{
			stream = Files.newDirectoryStream(Paths.get(pathToDir));
			for (Path entry : stream)
			{

				if (entry.toString().toUpperCase().contains(truncatePathSuffix(fileName).toUpperCase())
						&& entry.toString().toUpperCase().contains(CSV) && !(entry.toString().contains("FINAL")))
				{
					logger.info("adding file " + entry);

					files.add(entry.toString());
				}
			}
			if (files.isEmpty())
			{
				logger.warn("NO FILES MET CRITERIA");
			}
			return files;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e);
		}
		finally
		{
			if (null != stream)
			{
				try
				{
					stream.close();
				}
				catch (IOException ioe)
				{
					logger.error(ioe);
				}
			}
		}

	}

	/**
	 * @return
	 */
	public String getLocalPorts()
	{
		return localPorts;
	}

	/**
	 * @return
	 */
	public String getLocalUrl()
	{
		return localUrl;
	}

	private boolean isValidLibSvmEntry(Map<String, String> dataRow, String key, Map<String, Integer> columnIndicesMap)
	{
		if (null == dataRow.get(key) || dataRow.get(key).isEmpty())
		{

			return false;
		}
		if (key.equals(targetColumnName))
		{
			return true;
		}

		Integer columnIndex = columnIndicesMap.get(key);
		if (null == columnIndex)
		{

			return false;
		}

		return true;
	}

	/**
	 * Loads CSV from local file system
	 * @param fileName
	 * @param uidCol
	 * @throws IKodaUtilsException
	 */
	public synchronized void loadCsv(String fileName, String uidCol) throws IKodaUtilsException
	{
		String path = cleanFullPath(pathToDir,fileName);

		readCsvBlock(path, uidCol);
	}

	private synchronized File makeTempFile(List<String> rows) throws IKodaUtilsException
	{
		try
		{
			File temp = File.createTempFile("report_" + System.currentTimeMillis(), ".csv");

			FileWriter writer = new FileWriter(temp);
			try
			{
				writeToFile(rows, writer);
			}
			catch (Exception x)
			{
				logger.error(x);
			}
			finally
			{
				writer.close();
			}
			return temp;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private String mlServerCheckIn(String serverurl, int serverport, int localport, String localurl)
			throws IKodaUtilsException
	{

		try
		{
			logger.info("Checking in with MLServer.");
			SimpleHtmlUnit shu = new SimpleHtmlUnit(serverurl, serverport);
			MomentSocket ms = new MomentSocket(localport, String.valueOf(localport));
			HashMap<String, String> params = new HashMap<>();
			params.put("port", String.valueOf(localport));
			params.put("url", String.valueOf(localurl));
			logger.debug("Checking in to stream target address of " + serverurl + ":" + serverport);
			String connectResponse = shu.getAsText(serverurl, serverport, "testConnection", params);
			logger.info("Connected to spark server at " + serverurl + ":" + serverport + " " + connectResponse);
			if (!connectResponse.toUpperCase().contains("SUCCESS"))
			{
				logger.info("Failed to complete connection  " + serverurl + ":" + serverport + " " + connectResponse);
				throw new IKodaUtilsException(
						"Failed to complete connection  " + serverurl + ":" + serverport + " " + connectResponse);
			}
			return connectResponse;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private void moveRecompiledFiles(List<String> pathsUsedForCompile)
	{
		try
		{
			File tempDir = new File(cleanFullPath(pathToDir ,"tmp"));
			if (!tempDir.exists())
			{
				tempDir.mkdir();
			}
			for (String s : pathsUsedForCompile)
			{
				Path f = Paths.get(s);
				Path toPath = Paths.get(tempDir.getPath() + File.separator + f.getFileName() + "_DISCARDED_PART");
				Files.move(f, toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private boolean printByBlockCalled()
	{
		if (printByBlockCalled)
		{
			logger.warn(
					"\n\n\nprintBlock has been called. Cache has been cleared. Print Overwrite would wipe data.\n\n");
			return true;
		}
		return false;
	}

	private void printByRowCsv(Path path) throws IKodaUtilsException, IOException
	{
		printByRowCsv(path, new ArrayList<String>());
	}

	private void printByRowCsv(Path path, List<String> columnsToPrint) throws IKodaUtilsException, IOException
	{

		boolean fileExists = Files.exists(path);

		File file = path.toFile();

		FileWriter writer = new FileWriter(file, true);
		try
		{

			if (!fileExists)
			{
				write(this.generateColumnNames(columnsToPrint), path, true);
			}

			Iterator<String> itrRowPKs = data.keySet().iterator();

			Map<String, String> columnHeadsToPrint = selectColumnHeadsToPrint(columnsToPrint);

			while (itrRowPKs.hasNext())
			{

				StringBuilder sb = new StringBuilder();
				String s = itrRowPKs.next();
				Map<String, String> dataRow = data.get(s);

				processRow(columnHeadsToPrint, sb, selectColumnsToPrint(dataRow, columnsToPrint));
				sb.append(END);
				writeToFile(sb.toString(), writer);
			}

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
		finally
		{
			writer.close();
		}
	}

	private void printByRowLibsvm(String spath, String[] columnsToIgnore) throws IKodaUtilsException, IOException
	{
		Path path = Paths.get(spath);
		File file = path.toFile();

		FileWriter writer = new FileWriter(file, true);
		try
		{
			int count = 0;

			Iterator<String> itrRowPKs = data.keySet().iterator();
			List<String> targetList = generateLibSvmTargetMap();
			saveLibSvmTargetMap(spath, targetList);
			Map<String, Integer> columnIndicesMap = generateLibSvmColumnMap(columnsToIgnore);
			List<String> columnFileList = generateLibSvmColumnFile(columnIndicesMap, columnsToIgnore);
			String savePathTruncated = truncatePathSuffix(spath);
			saveColumnMap(columnFileList, savePathTruncated);

			logger.info("In Memory row count: " + data.size());
			logger.info("In Memory column count: " + columnIndicesMap.size());
			List<String> idList = new ArrayList<>();
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
				writeToFile(lvt.getTarget() + SPACE + lvt.getValue(), writer);

			} // end all rows
			logger.info("Generated " + count + " rows.");
			logger.info("Target column was  " + targetColumnName);
			write(idList, truncatePathSuffix(spath) + ID_MAP_SUFFIX, false);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvAppend() throws IKodaUtilsException
	{

		printCsvAppend(name);

	}

	/**
	 * @param fileName
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvAppend(String fileName) throws IKodaUtilsException
	{
		try
		{
			logger.info(pathToDir + File.separator + projectPrefix + fileName);
			printByRowCsv(Paths.get(cleanFullPath(pathToDir, projectPrefix + fileName)));
			data.clear();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + pathToDir + File.separator + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/* (non-Javadoc)
	 * @see ikoda.utils.AbstractSpreadsheetCreator#printCsvBlock()
	 */
	public synchronized void printCsvBlock() throws IKodaUtilsException
	{
		printCsvBlock(name);
	}

	/* (non-Javadoc)
	 * @see ikoda.utils.AbstractSpreadsheetCreator#printCsvBlock(java.lang.String)
	 */
	public synchronized void printCsvBlock(String fileName) throws IKodaUtilsException
	{
		printCsvBlock(fileName, new ArrayList<String>());
	}

	/**
	 * Saves data to file and clears data from memory
	 * Subsequently, use the {@link #finalizeAndJoinBlocks(String) finalizeAndJoinBlocks} method to recompile all blocks into a single file
	 * @param fileName
	 * @param columnsToKeep
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvBlock(String fileName, List<String> columnsToKeep) throws IKodaUtilsException
	{
		try
		{
			String path = cleanFullPath(pathToDir, projectPrefix + fileName);
			printByBlockCalled = true;
			String strPathTruncated = truncatePathSuffix(path);
			strPathTruncated = strPathTruncated + "_" + System.currentTimeMillis();
			String finalPath = strPathTruncated + ".csv";
			deleteFile(finalPath);
			printByRowCsv(Paths.get(finalPath), columnsToKeep);
			clearData();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * Saves CSV to file, overwriting any pre-existing files
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvFinal() throws IKodaUtilsException
	{
		printCsvFinal(name);
	}

	/**
	 * Saves CSV to file, overwriting any pre-existing files. Includes the project prefix (if set) and "FINAL" as part of the file name
	 * @param fileName
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvFinal(String fileName) throws IKodaUtilsException
	{
		try
		{

			logger.info("Saving by row: " + projectPrefix + fileName);
			String path = cleanFullPath(pathToDir,projectPrefix + "FINAL_" + fileName);
			if (!path.toUpperCase().endsWith(CSV))
			{
				path = path + ".csv";
			}
			deleteFile(path);
			printByRowCsv(Paths.get(path));

		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * Saves CSV to file, overwriting any pre-existing files. 
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvOverwrite() throws IKodaUtilsException
	{
		printCsvOverwrite(name);
	}

	/**
	 * Saves CSV to file, overwriting any pre-existing files. 
	 * @param fileName
	 * @throws IKodaUtilsException
	 */
	public synchronized void printCsvOverwrite(String fileName) throws IKodaUtilsException
	{
		try
		{
			if (printByBlockCalled())
			{
				return;
			}

			if (!fileName.toUpperCase().endsWith(CSV))
			{
				fileName = fileName + ".CSV";
			}

			String path = cleanFullPath(pathToDir,projectPrefix + fileName);
			deleteFile(path);
			logger.info(path);
			printByRowCsv(Paths.get(path));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * Saves Libsvm to file, appending any pre-existing files. 
	 * @param fileName
	 * @param targetColumn
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmAppender(String fileName, String targetColumn, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		try
		{
			targetColumnName = targetColumn;
			logger.info(cleanFullPath(pathToDir,projectPrefix + fileName));
			String fullPath = cleanFullPath(pathToDir,projectPrefix + fileName);
			if (!fullPath.endsWith(LIBSVM))
			{
				fullPath = fullPath + LIBSVM;
			}
			List<String> rows = generateLibSvmRowsAsString(fullPath, columnsToIgnore);
			write(rows, fullPath, true);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * Saves Libsvm to file, appending any pre-existing files. 
	 * @param targetColumn
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmAppender(String targetColumn, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		printLibSvmAppender(name, targetColumn, columnsToIgnore);
	}

	/**
	 * Saves data to file and clears data from memory
	 * Subsequently, use the {@link #finalizeAndJoinBlocks(String) finalizeAndJoinBlocks} method to recompile all blocks into a single file
	 * @param fileName
	 * @param targetColumn
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmBlock(String fileName, String targetColumn, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		try
		{
			printByBlockCalled = true;
			String path = cleanFullPath(pathToDir,projectPrefix + fileName);
			logger.info(path);
			String strPathTruncated = truncatePathSuffix(path);
			strPathTruncated = strPathTruncated + "_" + System.currentTimeMillis();
			String finalPath = strPathTruncated + LIBSVM;
			deleteFile(finalPath);
			targetColumnName = targetColumn;

			List<String> rows = generateLibSvmRowsAsString(finalPath, columnsToIgnore);
			write(rows, finalPath, false);
			clearData();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * Saves data to file and clears data from memory
	 * Subsequently, use the {@link #finalizeAndJoinBlocks(String) finalizeAndJoinBlocks} method to recompile all blocks into a single file
	 * @param targetColumn
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmBlock(String targetColumn, String[] columnsToIgnore) throws IKodaUtilsException
	{
		printLibSvmBlock(name, targetColumn, columnsToIgnore);
	}

	/**
	 * Saves data to file and clears data from memory
	 * Subsequently, use the {@link #finalizeAndJoinBlocks(String) finalizeAndJoinBlocks} method to recompile all blocks into a single file
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmBlock(String[] columnsToIgnore) throws IKodaUtilsException
	{
		printLibSvmBlock(name, targetColumnName, columnsToIgnore);
	}

	/**
	 * Saves Libsvm to file, overwriting any pre-existing files. Includes the project prefix (if set) and "FINAL" as part of the file name
	 * @param fileName
	 * @param targetColumn
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmFinal(String fileName, String targetColumn, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		try
		{
			logger.info(projectPrefix + fileName);
			logger.info("targetColumn\n" + targetColumn);
			// logger.info("targetMap\n" + targetMap);
			// logger.info("columns\n" + columnHeadings);
			String path = cleanFullPath(pathToDir ,"FINAL_" + projectPrefix + fileName);
			targetColumnName = targetColumn;
			if (!path.endsWith(LIBSVM))
			{
				path = path + LIBSVM;
			}
			deleteFile(path);

			printByRowLibsvm(path, columnsToIgnore);
			logger.info("Saved " + path);

		}
		catch (Exception e)
		{
			logger.error(e.getMessage() + " " + projectPrefix + fileName, e);
			throw new IKodaUtilsException(e);
		}
	}

	/**
	 * Saves Libsvm to file, overwriting any pre-existing files. Includes the project prefix (if set) and "FINAL" as part of the file name
	 * @param fileName
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmFinal(String fileName, String[] columnsToIgnore) throws IKodaUtilsException
	{
		if (null == targetColumnName)
		{
			logger.warn("targetColumnName is null. Aborting");
			return;
		}
		printLibSvmFinal(fileName, targetColumnName, columnsToIgnore);
	}

	/**
	 * Saves Libsvm to file, overwriting any pre-existing files. Includes the project prefix (if set) and "FINAL" as part of the file name
	 * @param columnsToIgnore
	 * @throws IKodaUtilsException
	 */
	public synchronized void printLibSvmFinal(String[] columnsToIgnore) throws IKodaUtilsException
	{
		printLibSvmFinal(name, columnsToIgnore);
	}

	private void processCell(StringBuilder sb, Map<String, String> dataRow, String key) throws IKodaUtilsException
	{
		try
		{
			String temp = dataRow.get(key);
			if (dataRow.get(key).contains(COMMA))
			{
				temp = dataRow.get(key).replace(COMMA, DASH);
			}
			sb.append(temp);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}
	}

	private List<String> processFinalizeAndJoinBlocks(String fileName, String[] columnsToIgnore)
			throws IKodaUtilsException
	{
		try
		{

			List<String> paths = getCSvBlockPaths(fileName);

			logger.debug("Paths count is " + paths.size());

			readCsvBlocks(paths, pkColumnName);
			return paths;

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e);
		}

	}

	protected LabelValuesTuple processLibSvmRow(String rowId, Map<String, Integer> columnIndicesMap, boolean asString)
			throws IKodaUtilsException
	{
		try
		{
			StringBuilder sbRow = new StringBuilder();
			LabelValuesTuple lvt = new LabelValuesTuple();

			Map<String, String> dataRow = data.get(rowId);
			Iterator<String> columnHeadsItr = columnHeadings.keySet().iterator();

			String labelAsNumeric = EMPTY;
			String targetValue = EMPTY;
			// logger.debug("targetColumnName " + targetColumnName);

			/** Iterate cells in one row */
			while (columnHeadsItr.hasNext())
			{

				String key = columnHeadsItr.next();
				Integer columnIndex = columnIndicesMap.get(key);

				if (!isValidLibSvmEntry(dataRow, key, columnIndicesMap))
				{

					continue;
				}

				if (isLabel(key))
				{
					labelAsNumeric = convertToNumericLabel(key, dataRow.get(key));

					// logger.debug("key is " + key + ", label is " + labelAsNumeric);
					continue;
				}

				String value = dataRow.get(key);
				try
				{
					Double d = Double.parseDouble(value);
					if (asString)
					{

						sbRow.append(columnIndex);
						sbRow.append(COLON);
						sbRow.append(d);
						sbRow.append(SPACE);
					}
					else
					{
						lvt.addColValPair(columnIndex, d);
					}
				}
				catch (Exception e)
				{
					logger.warn("Unable to process k:" + key + " v:" + value + " e: " + e.getMessage());
					continue;
				}

			} // end one cell
			if (labelAsNumeric.isEmpty())
			{

				labelAsNumeric = "0" + SPACE;

			}
			if (asString)
			{
				sbRow.append(END);
				lvt.setValue(sbRow.toString());
			}

			lvt.setTarget(Double.valueOf(labelAsNumeric.trim()));

			return lvt;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private void processRow(Map<String, String> columnHeadingsToPrint, StringBuilder sb, Map<String, String> dataRow)
			throws IKodaUtilsException
	{
		try
		{
			Iterator<String> columnHeadsItr = columnHeadingsToPrint.keySet().iterator();

			while (columnHeadsItr.hasNext())
			{
				String key = columnHeadsItr.next();

				if (null == dataRow.get(key))
				{
					sb.append(EMPTY);
				}
				else
				{
					processCell(sb, dataRow, key);
				}
				/**
				 * target column is inserted subsequent to the data values as it is not in the
				 * datamap (because values are dynamic)
				 */
				if (key.equals(targetColumnName))
				{
					sb.append(getTargetValue());
				}
				sb.append(COMMA);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private void readCsvBlock(String path, String uidCol) throws IKodaUtilsException
	{
		try
		{

			Reader in = null;
			try
			{
				logger.debug("Loading file: " + path + "uid column name is " + uidCol);
				in = new FileReader(path);
				Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

				for (CSVRecord record : records)
				{
					Map<String, String> entries = record.toMap();
					String uid = entries.get(uidCol);
					if (null == uid)
					{
						logger.warn("uid colname is " + uidCol + " NULL uid in record " + record.toMap().toString());
						continue;
					}
					Iterator<String> itr = entries.keySet().iterator();
					String key = null;

					while (itr.hasNext())
					{
						key = itr.next();
						if (null == entries.get(key) || entries.get(key).isEmpty())
						{
							continue;
						}
						addCell(uid, key, entries.get(key));
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e);
			}
			finally
			{
				logger.info("LOADED " + path + ". Row count: " + data.size());
				in.close();
			}
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private void readCsvBlocks(List<String> paths, String uidCol) throws IKodaUtilsException
	{
		try
		{
			for (String entry : paths)
			{
				readCsvBlock(entry, uidCol);

			}
			logger.info("Loaded " + data.size() + " rows.");
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private void saveColumnMap(List<String> list, String path) throws IKodaUtilsException
	{
		try
		{
			write(list, path + COLUMN_MAP_SUFFIX, false);
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private void saveLibSvmTargetMap(String savePath, List<String> list) throws IKodaUtilsException
	{
		try
		{
			write(list, truncatePathSuffix(savePath) + TARGET_MAP_SUFFIX, false);
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private Map<String, String> selectColumnsToPrint(Map<String, String> dataRow, List<String> columnsToPrint)
			throws IKodaUtilsException
	{
		try
		{

			if (columnsToPrint.isEmpty())
			{
				return dataRow;
			}
			Map<String, String> selectedColumns = new HashMap<>();
			Iterator<String> itr = dataRow.keySet().iterator();
			while (itr.hasNext())
			{
				String colName = itr.next();
				if (columnsToPrint.contains(colName))
				{

					selectedColumns.put(colName, dataRow.get(colName));
				}
			}
			
		

			return selectedColumns;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage());
		}

	}

	/**
	 * @param localPorts
	 */
	public void setLocalPorts(String localPorts)
	{
		this.localPorts = localPorts;
	}

	/**
	 * @param localUrl
	 */
	public void setLocalUrl(String localUrl)
	{
		this.localUrl = localUrl;
	}

	protected String sparkNewDataNofitication(String serverurl, int serverport, String localport, String localurl,
			String dataName) throws IKodaUtilsException
	{
		try
		{
			int counter = 0;

			String response = "WAIT";

			while (response.equals("WAIT"))
			{
				logger.info("Attempt " + counter + ": sparkNewDataNofitication to " + serverurl + ":" + serverport + " "
						+ dataName + " "+getKeyspaceUUID()+"-"+getKeyspaceName());
				SimpleHtmlUnit shu = new SimpleHtmlUnit(serverurl, serverport);

				HashMap<String, String> params = new HashMap<>();
				params.put("port", localport);
				params.put("url", localurl);
				params.put("DATA_SOURCE", dataName);
				params.put("KEY_SPACE", getKeyspaceName());
				params.put("KEY_SPACE_UUID", getKeyspaceUUID());

				response = shu.getAsText(serverurl, serverport, "startMLCPStream", params);
				logger.info("Initial response: " + response);
				counter++;
				if (counter == 12)
				{
					logger.warn("\n\nServer refused connection (Busy)\n\n");
					
					return "FAILED";
				}

				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					logger.warn("sparkNewDataNofitication Interrupted");
				}

			}

			return response;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}

	}
	
	

	
	
	/**
	 * 
	 * Initializes a stream with an ikodaML app deployed on Spark
	 * @param mlserverUrl
	 * @param mlserverport
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String sparkStreamInit(String mlserverUrl, int mlserverport) throws IKodaUtilsException
	{

		try
		{
			logger.info("Spark Stream Initializing");

			SimpleHtmlUnit shu = new SimpleHtmlUnit(mlserverUrl, mlserverport);
			HashMap<String, String> params = new HashMap<>();

			logger.info("Sending init signal to Spark: ");

			String response = shu.getAsText("initializeMLCPStream", params);
			if (response.toUpperCase().contains("INITIALIZED"))
			{
				return response;
			}
			throw new IKodaUtilsException(response + " No connection with server.");
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}

	}
	
	


	
	
	/**
	 * Streams data to an ikodaML instance on Spark in CSV format
	 * @param serverurl
	 * @param serverport
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String sparkStreamRun(String serverurl, int serverport) throws IKodaUtilsException
	{
		return sparkStreamRun(serverurl, serverport, new ArrayList<String>());
	}

	/**
	 * 
	 * Streams data to an ikodaML instance on Spark in CSV format
	 * @param serverurl
	 * @param serverport
	 * @param columnsToPrint
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String sparkStreamRun(String serverurl, int serverport, List<String> columnsToPrint)
			throws IKodaUtilsException
	{
		return sparkStreamRun(getCassandraName(), serverurl, serverport, columnsToPrint);
	}

	/**
	 * Streams data to an ikodaML instance on Spark in CSV format
	 * @param nameToStream
	 * @param serverurl
	 * @param serverport
	 * @param columnsToPrint
	 * @return
	 * @throws IKodaUtilsException
	 */
	public String sparkStreamRun(String nameToStream, String serverurl, int serverport, List<String> columnsToPrint)
			throws IKodaUtilsException
	{
		try
		{
			logger.debug("sparkStreamRun");
			logger.debug("sparkStreamRun columnsToPrint size: " + columnsToPrint.size());
			if (!sparkValidateStreamPorts(1))
			{
				throw new IKodaUtilsException("Connection address validation failed.");
			}
			

			String[] ports = localPorts.split(",");

			String response = sparkNewDataNofitication(serverurl, serverport, ports[0], localUrl, timeStampDataName(nameToStream,Calendar.getInstance().getTime()));
			logger.info("Sending " + data.size() + " rows");

			if (response.toUpperCase().contains("SUCCESS"))
			{
				StreamToSpark sts = new StreamToSpark(Integer.valueOf(ports[0]));
				Map<String, String> columnHeadsToPrint = selectColumnHeadsToPrint(columnsToPrint);
				Iterator<String> itrRowPKs = data.keySet().iterator();
				sts.sendLine(StreamingConstants.IKODA_DATA_NAME + "=" + nameToStream);
				sts.sendLine(generateColumnNames(columnsToPrint).replace(COMMA + END, END));
				int count = 0;
				while (itrRowPKs.hasNext())
				{
					System.out.print(count + " ");
					count = count + 1;

					StringBuilder sb = new StringBuilder();
					String s = itrRowPKs.next();
					Map<String, String> dataRow = data.get(s);

					processRow(columnHeadsToPrint, sb, selectColumnsToPrint(dataRow, columnsToPrint));
					sb.append(END);
					sts.sendLine(sb.toString().replace(COMMA + END, END));
				}

				sts.sendLine(StreamingConstants.IKODA_END_STREAM);

				sparkWaitOnStream(sts);
				sts.shutdown();
			}
			else
			{
				logger.warn("\n\nFAILED to contact spark\n\n");
			}
			return response;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new IKodaUtilsException(e.getMessage(), e);
		}

	}
	
	
	
	

	protected boolean sparkValidateStreamPorts(int expectedPortsCount)
	{
		logger.info("sparkValidateStreamPorts for " + name + ": " + localPorts + " " + localUrl);
		if (null == localPorts || null == localUrl)
		{
			logger.warn("Null local address: " + localPorts + " url " + localUrl);
			logger.debug("sparkValidateStreamPorts PASS");
			return false;
		}
		else if (localPorts.split(",").length < expectedPortsCount)
		{
			logger.warn("Not enough ports " + localPorts.split(",").length);
			return false;
		}
		return true;
	}

	protected void sparkWaitOnStream(StreamToSpark sts) throws InterruptedException, IKodaUtilsException
	{

		int lastQueueSize = sts.queueSize();
		int count = 0;
		logger.debug("Waiting on sts");
		while (sts.queueSize() > 0)
		{

			if (sts.queueSize() == lastQueueSize)
			{
				logger.warn("StreamToSpark waiting on port " + sts.getPort());
			}
			count++;
			if (count > 60)
			{

				sts.shutdown();
				logger.error("Timed out");
				throw new IKodaUtilsException("sparkWaitOnStream Timed Out on port " + sts.getPort());
			}
			lastQueueSize = sts.queueSize();

			System.out.println("waiting at queue size " + sts.queueSize());
			Thread.sleep(3000);
		}
		logger.info("Queue is now empty.");

	}

	private synchronized void write(List<String> rows, Path path, boolean append) throws IKodaUtilsException
	{

		try
		{
			logger.debug("saving " + path);

			if (!path.toFile().exists())
			{
				Files.createDirectories(path.getParent());

			}

			File file = path.toFile();

			FileWriter writer = new FileWriter(file, append);

			try
			{
				writeToFile(rows, writer);
			}
			catch (Exception x)
			{
				logger.error(x);
			}
			finally
			{
				writer.close();
			}

		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private synchronized void write(List<String> rows, String filename, boolean append) throws IKodaUtilsException
	{

		try
		{
			logger.debug("saving " + filename);

			Path path = Paths.get(filename);

			write(rows, path, append);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			throw new IKodaUtilsException(e.getMessage() + " " + filename, e);
		}
	}

	private synchronized void write(String row, Path path, boolean append) throws IKodaUtilsException
	{

		try
		{

			if (!path.toFile().exists())
			{
				Files.createDirectories(path.getParent());

			}

			File file = path.toFile();

			FileWriter writer = new FileWriter(file, append);

			try
			{
				writeToFile(row, writer);
			}
			catch (Exception x)
			{
				logger.error(x);
			}
			finally
			{
				writer.close();
			}

		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private synchronized void writeToFile(List<String> records, Writer writer) throws IOException
	{
		try
		{

			for (String record : records)
			{
				writer.write(record);
			}
			writer.flush();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

	private synchronized void writeToFile(String record, Writer writer) throws IOException
	{
		try
		{
			writer.write(record);

			writer.flush();

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

}
