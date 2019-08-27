package ikoda.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileTuple
{

	private List<Path> inputPaths = new ArrayList<>();
	private List<Path> files = Collections.synchronizedList(new ArrayList<Path>());
	private String name;

	protected FileTuple(String inname, List<Path> ininputPaths)
	{
		name = inname;
		inputPaths = ininputPaths;
	}

	List<Path> getFiles()
	{
		return files;
	}

	List<Path> getInputPaths()
	{
		return inputPaths;
	}

	String getName()
	{
		return name;
	}

}