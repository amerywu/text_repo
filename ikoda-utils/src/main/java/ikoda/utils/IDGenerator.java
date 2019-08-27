package ikoda.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


/****
 * Generatees UIDs and UUIDs
 * @author jake
 *
 */
public class IDGenerator
{
	private static IDGenerator idgenerator;
	private static final String ID_FILE = ".idgen";
	public static IDGenerator getInstance() throws IKodaUtilsException
	{
		if (null == idgenerator)
		{
			idgenerator = new IDGenerator();
		}
		return idgenerator;
	}

	private long lastId = 1000;

	private String path = "";

	private IDGenerator() throws IKodaUtilsException
	{
		initializeId();
	}

	private synchronized void initializeId() throws IKodaUtilsException
	{
		try
		{
			path = new File(".").getCanonicalPath();
			File idFile = new File(path + File.separator + ID_FILE);
			SSm.getAppLogger().info("initializeId path: " + path + File.separator + ID_FILE);
			if (!idFile.exists())
			{
				writeFile(String.valueOf(lastId));
			}
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public synchronized long nextID() throws IKodaUtilsException
	{
		try
		{
			lastId = readId(path + File.separator + ID_FILE);
			long newId = lastId + 1;
			writeFile(String.valueOf(newId));
			return newId;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public synchronized long nextIDInMem() throws IKodaUtilsException
	{
		try
		{
			lastId = lastId + 1;
			return lastId;
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public synchronized String nextUUID() throws IKodaUtilsException
	{
		try
		{

			return java.util.UUID.randomUUID().toString();
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	private synchronized long readId(String filename) throws IKodaUtilsException
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(filename)))
		{
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null)
			{
				line.replaceAll("\r", "");
				line.replaceAll("\n", "");
				sb.append(line);
			}
			if (sb.length() == 0)
			{
				SSm.getAppLogger().warn("FAILED TO RETRIEVE ID FROM FILE");
				initializeId();
				lastId++;
				return new Long(lastId);
			}
			return Long.valueOf(sb.toString()).longValue();
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public synchronized void resetId() throws IKodaUtilsException
	{
		try
		{

			writeFile("1000");

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public synchronized void writeFile(String text) throws IKodaUtilsException
	{
		try (BufferedWriter out = new BufferedWriter(new FileWriter(path + File.separator + ID_FILE)))
		{

			out.write(text);

		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

}
