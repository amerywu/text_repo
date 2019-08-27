package ikoda.utils;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;





public class StaticSundryUtils
{
	
	
	
	
	public static synchronized int countOccurrencesOfChar(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}
	
	  public synchronized static void archiveFile(Path from)
	{
		try
		{

			String todir = from.toString().replaceAll(from.getFileName().toString(), "") + "archive";
			if (!(Paths.get(todir).toFile().exists()))
			{
				Files.createDirectories(Paths.get(todir));
			}
			Path to = Paths.get(todir + File.separator + from.getFileName());

			// method 1

			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);

		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
		}
	}
	
	  
	    public static synchronized String cleanColumnName(String s)
			  {
			  
			  if(null==s)
			  {
				  return "blank_column_head_error";
			  }
			    String cleaner= s.replaceAll("[^A-Za-z0-9_-]","").toLowerCase();

			      if(cleaner.startsWith("_")) 
			      { 
			    	  return "u"+cleaner;
			      }
			      if(cleaner.startsWith("-"))
			      {
			    	  
			    	  return "u"+cleaner;

			      }
			      if(cleaner.isEmpty())
			      {
			    	  return "scrubbed_to_empty_column_head";
			      }
			      return cleaner;


			  }
	    public synchronized static String extractHeader(String startTag, String endTag, String fileContent)
		{
			try
			{
				if (fileContent.contains(startTag))
				{
					int start = fileContent.indexOf(startTag) + startTag.length();
					int end = fileContent.indexOf(endTag);
					String value = fileContent.substring(start, end);
					value = value.trim();
					return value;
				}
				else
				{
					SSm.getAppLogger().debug("No value for " + startTag);
					return null;
				}
			}
			catch (Exception e)
			{
				SSm.getAppLogger().debug(startTag + " " + e.getMessage(), e);
				return null;
			}

		}


	public static synchronized List<Path> listFiles(Path path)
	{

		List<Path> files = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
		{

			for (Path entry : stream)
			{
				if (Files.isDirectory(entry))
				{

					continue;
				}
				files.add(entry);

			}
			return files;

		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
			return new ArrayList<Path>();
		}

	}

	public synchronized static void moveFile(Path from, Path to)
	{
		try
		{

			// method 1
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);

		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
		}
	}

	public static synchronized  String removeListsAndMenus(String fileContent)
	{
	    try
	    {
	       
	        String cleanerS=fileContent.replaceAll(" \r\n", "\r\n");
	        cleanerS=cleanerS.replaceAll(" \n", "\n");
	        
	        cleanerS=removeRepeats(cleanerS,"\n\n\n\n\n", "\n");
	        cleanerS=removeRepeats(cleanerS,"\r\n\r\n\r\n\r\n", "\r\n");

	        
	        String s = cleanerS.replaceAll("(([A-Za-z&—:\\-\\/\\d ])*(\\n|\\r|\\r\\n)){5,}","");
	        
	        return s;
	    }
	    catch(Exception e)
	    {
	    	SSm.getAppLogger().error(e.getMessage(),e);
	        return fileContent;
	    }
	}

	private static String removeRepeats(String content, String pattern, String replace)
	{
	    String[] split =content.split(pattern);
	    if(split.length>1)
	    {
	        String returnString=content.replaceAll(pattern, replace);
	        return removeRepeats(returnString,pattern,replace);
	    }
	    else
	    {
	        return content;
	    }
	}

	public StaticSundryUtils()
	{

	}

}
