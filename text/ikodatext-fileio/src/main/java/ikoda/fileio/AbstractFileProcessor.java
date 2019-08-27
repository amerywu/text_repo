package ikoda.fileio;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.util.StringUtils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.spiders.AbstractWebSite;
import ikoda.utils.StaticSundryUtils;



public abstract class AbstractFileProcessor
{


	public final static String TAGDELIMITER = "__";

	protected final static String XML = "xml_";

	protected final static String MAIN = "test";
	protected static String RESULTS_PAGE = "/jobs?";
	protected static String JOB_POSTING = "/pagead/";
	protected static String JOB_POSTING1 = "/rc/clk?";
	protected static String NON_POSTING = "View the full job posting";
	protected static HashMap<String, String> duplicateMap = new HashMap<String, String>();
	protected ReentrantLock lock;
	protected ConfigurationBeanParent config;
	
	
	public AbstractFileProcessor(ConfigurationBeanParent inconfig)
	{
	    config=inconfig;
	}
    protected String cleanString(String inString)
    {

        String outString = inString;
        if (StringUtils.countOccurrencesOf(inString, "\r\n\r\n\r\n\r\n\r\n") > 4)
        {

            if (inString.contains(AbstractWebSite.LIST))
            {
                outString = inString.replace("\r\n\r\n\r\n\r\n\r\n", "\r\n\r\n");
            }
        }
        
        outString = outString.replace("|[", "7u8e4");
        outString = outString.replace("]|", "7u8e5");
        outString = outString.replace(".com", "_com");
        outString = outString.replace(".co", "_co");
        outString = outString.replace("?", " .");
        outString = outString.replace("&", " and ");
        outString = outString.replace(".ca", "_ca");
        outString = outString.replace(".au", "_au");
        outString = outString.replace(".org", "_org");
        outString = outString.replace("www.", "www_");
        outString = outString.replace("\r\n", "\n");
        outString = outString.replace("\n\n", "\n");
        outString = outString.replace("\n", ".\n");
        outString = outString.replace("\n.\n", ".\n");
        outString = outString.replace("..", ".");
        outString = outString.replace("£", "$");
        outString = outString.replace("|", ",");
        outString = outString.replace("[", " ");
        outString = outString.replace("]", " ");
        outString = outString.replace("<", " ");
        outString = outString.replace(">", " ");
        outString = outString.replace(" \\.", ".");
        outString = outString.replace("   ", " ");
        outString = outString.replace("  ", " ");
        outString = outString.replace("`", "");
        outString = outString.replace("\"", "");
        outString = outString.replace(";", ".");
        outString = outString.replace("...", ".");
        outString = outString.replace("7u8e4", "|[");
        outString = outString.replace("7u8e5", "]|");
        char grave = 96;
        char apostrophe = 39;
        outString = outString.replace(grave, apostrophe);
        outString = outString.replaceAll("[^A-Za-z0-9|. ,:\\r\\n&-?_;=!]", "");

        outString = outString.replace("\\", " ");
        outString = outString.replace("/", " ");
        outString = outString.replace(";", ".");

        return outString;

    }
	protected String extractTextFromPdf(Path p)
	{

		try
		{
			FioLog.getLogger().debug("getting lock...");
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{
				try
				{
					FioLog.getLogger().debug("got lock: " + p.toString());

					///////////////////////////////////////////////////////
					PdfReader reader = null;
					StringBuilder sb = new StringBuilder();
					try
					{
						reader = new PdfReader(p.toString());
						FioLog.getLogger().debug(reader);
						int n = reader.getNumberOfPages();
						FioLog.getLogger().debug("page count " + n);
						sb.append("\n\n\n---EXTRACTED FROM PDF---\n ");
						sb.append(p.toString());
						sb.append("\n\n\n");
						for (int i = 1; i <= n; i++)
						{
							sb.append(PdfTextExtractor.getTextFromPage(reader, i));
							// FioLog.getLogger().debug("page "+sb.toString());
						}
						FioLog.getLogger().debug(sb);

						byte[] b = sb.toString().getBytes(StandardCharsets.US_ASCII);
						String pdfstring = new String(b);
						pdfstring.replaceAll("\\?", " ");
						pdfstring=StaticSundryUtils.removeListsAndMenus(pdfstring);
						return pdfstring;
					}
					catch (Exception e)
					{
						FioLog.getLogger().error(e.getMessage(), e);
						return "";
					}
					finally
					{
						if(null!=reader)
						{
							reader.close();
						}
					}
				}
				/////////////////////////////////////////////////////
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				FioLog.getLogger().warn("\n\n\nFailed to acquire lock");
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
		return "";

	}

	
	
	protected boolean isDuplicate(String s)
	{
		if (null == duplicateMap.get(s))
		{
			// NioLog.getLogger().debug("Putting "+jobId);
			duplicateMap.put(s, s);
			return false;
		}
		else
		{
			FioLog.getLogger().warn("DUPLICATE: " + s);
			return true;
		}
	}
	
	
	public abstract void processFile(Path file, ReentrantLock inlock);

	protected void processNonPosting(Path fromFile, String toDir)
	{
		processNonPosting(fromFile, toDir,new ArrayList<String>());
	}

	protected void processNonPosting(Path fromFile,  String toDir,List<String> fileNameTags)
	{
		try
		{
			FioLog.getLogger().debug("getting lock...");
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{
				try
				{
					FioLog.getLogger().debug("got lock: " + fromFile.toString());

					if (!Files.exists(Paths.get(toDir)))
					{
						Files.createDirectories(Paths.get(toDir));
					}
					String filename = fromFile.getFileName().toString().replaceAll("[^A-Za-z0-9]", "");

					Path TO = Paths.get(toDir + File.separator + tagString(fileNameTags) + filename);
					// overwrite existing file, if exists
					FioLog.getLogger().debug("copying");

					CopyOption[] options = new CopyOption[]
					{ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
					Files.copy(fromFile, TO, options);
					FioLog.getLogger().debug("done");

				}
				catch (Exception e)
				{
					FioLog.getLogger().debug(fromFile.toString());
					FioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				FioLog.getLogger().warn("Failed to acquire lock");

			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	protected void processPosting(Path fromFile, String toDir)
	{
		processPosting(fromFile, toDir, new ArrayList<String>());
	}

	protected void processPosting(Path fromFile, String toDir, List<String> fileNameTags)
	{
		try
		{
			FioLog.getLogger().debug("getting lock...");
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{
				try
				{

					FioLog.getLogger().debug("got lock: " + fromFile.toString());

					String fileName = null;
					if (fromFile.getFileName().toString().length() > 75)
					{
						fileName = fromFile.getFileName().toString().substring(0, 73) + ".txt";
					}
					else
					{
						fileName = fromFile.getFileName().toString();
					}

					if (!Files.exists(Paths.get(toDir)))
					{
						Files.createDirectories(Paths.get(toDir));
					}

					Path TO = Paths.get(toDir + File.separator + tagString(fileNameTags) + fileName);
					// overwrite existing file, if exists\
					FioLog.getLogger().debug("copying");

					CopyOption[] options = new CopyOption[]
					{ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
					Files.copy(fromFile, TO, options);
					FioLog.getLogger().debug("done");

				}
				catch (Exception e)
				{
					FioLog.getLogger().debug(fromFile.toString());
					FioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				FioLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	protected void processResultsPage(Path fromFile, String toDir)
	{
		processResultsPage(fromFile, toDir, new ArrayList<String>());
	}

	protected void processResultsPage(Path fromFile, String toDir, List<String> fileNameTags)
	{
		try
		{
			FioLog.getLogger().debug("getting lock...");
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{

				try
				{
					FioLog.getLogger().debug("got lock: " + fromFile.toString());

					if (!Files.exists(Paths.get(toDir)))
					{
						Files.createDirectories(Paths.get(toDir));
					}

					Path TO = Paths
							.get(toDir + File.separator + tagString(fileNameTags) + fromFile.getFileName().toString());
					// overwrite existing file, if exists
					FioLog.getLogger().debug("copying");

					CopyOption[] options = new CopyOption[]
					{ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
					Files.copy(fromFile, TO, options);
					FioLog.getLogger().debug("done");

				}
				catch (Exception e)
				{
					FioLog.getLogger().debug(fromFile.toString());
					FioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				FioLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	////////////////////////////////////////////////////////////////

	protected void processUnidentified(Path fromFile, String toDir)
	{
		processUnidentified(fromFile, toDir, new ArrayList<String>());
	}

	protected void processUnidentified(Path fromFile, String toDir, List<String> fileNameTags)
	{
		try
		{
			FioLog.getLogger().debug("getting lock...");
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{

				try
				{

					FioLog.getLogger().debug("got lock: " + fromFile.toString());

					if (!Files.exists(Paths.get(toDir)))
					{
						Files.createDirectories(Paths.get(toDir));
					}

					Path TO = Paths.get(
					        toDir + File.separator + tagString(fileNameTags) + fromFile.getFileName().toString());
					// overwrite existing file, if exists
					CopyOption[] options = new CopyOption[]
					{ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

					FioLog.getLogger().debug("copying");
					Files.copy(fromFile, TO, options);
					FioLog.getLogger().debug("done");

				}
				catch (Exception e)
				{
					FioLog.getLogger().debug(fromFile.toString());
					FioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				FioLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	protected void processXmlFile(Path fromFile, String toDir)
	{
		processXmlFile(fromFile, toDir,new ArrayList<String>());
	}

	protected void processXmlFile(Path fromFile, String toDir, List<String> fileNameTags)
	{

		try
		{
			FioLog.getLogger().debug("getting lock...");
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{
				try
				{
					FioLog.getLogger().debug("got lock: " + fromFile.toString());
					String fileName = null;

					if (fromFile.getFileName().toString().length() > 75)
					{
						fileName = fromFile.getFileName().toString().substring(0, 73) + ".txt";
					}
					else
					{
						fileName = fromFile.getFileName().toString();
					}

					if (!Files.exists(Paths.get(toDir)))
					{
						Files.createDirectories(Paths.get(toDir));
					}

					Path TO = Paths.get(toDir + File.separator + fileName);
					// overwrite existing file, if exists
					FioLog.getLogger().debug("copying xml file "+toDir + File.separator + fileName);
					CopyOption[] options = new CopyOption[]
					{ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

					Files.copy(fromFile, TO, options);
					FioLog.getLogger().debug("--- xml file copied");
				}
				catch (Exception e)
				{
					FioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				FioLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}


	////////////////

	protected String tagString(List<String> tags)
	{
		if (tags.size() == 0)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s : tags)
		{
			sb.append(TAGDELIMITER);
			sb.append(s);

		}
		return sb.toString();

	}

	protected abstract void triage(String fileContent, Path p);

}
