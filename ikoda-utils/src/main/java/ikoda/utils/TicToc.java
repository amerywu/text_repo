package ikoda.utils;

import java.util.HashMap;
import java.util.Iterator;

/****
 * Utility for timing function performance
 * @author jake
 *
 */
public class TicToc
{

	private class OneAction
	{

		private String actionName;
		private long startTime;
		private long maxPermissibleTime = System.currentTimeMillis();
		private long durationTime = -1;

		private OneAction(String actionName, long startTime)
		{
			this.actionName = actionName;
			this.startTime = startTime;

		}

		private OneAction(String actionName, long startTime, long maxPermissibleTime)
		{
			this.actionName = actionName;
			this.startTime = startTime;
			this.maxPermissibleTime = maxPermissibleTime;

		}

		private String getActionName()
		{
			return actionName;
		}

		private long getCurrentDuration()
		{
			return System.currentTimeMillis() - startTime;
		}

		private long getDuration()
		{
			if (durationTime < 0)
			{
				durationTime = System.currentTimeMillis() - startTime;
			}
			return durationTime;
		}

		private long getMaxPermissibleTime()
		{
			return maxPermissibleTime;
		}

		private long getStartTime()
		{
			return startTime;
		}

		private boolean isActionOverTime()
		{
			return getDuration() > maxPermissibleTime;
		}

		private void stopActionTimer()
		{
			getDuration();
		}

	}

	private long permissibleTimeForMostRecentAction;
	private HashMap<String, OneAction> actions = new HashMap();

	public TicToc()
	{

	}

	public synchronized void clear()
	{
		actions.clear();
	}

	public String getOverTimeLog()
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> itr = actions.keySet().iterator();
		sb.append("\nWARN\n**********");
		while (itr.hasNext())
		{
			String key = itr.next();
			OneAction oneAction = actions.get(key);
			if (oneAction.isActionOverTime())
			{

				sb.append("\n");
				sb.append(key);
				sb.append(":");
				sb.append(oneAction.getDuration());
				sb.append(" > ");
				sb.append(oneAction.maxPermissibleTime);

			}

		}
		sb.append("\n**********\n");
		return sb.toString();
	}

	public synchronized boolean isOverTime(String action)
	{
		OneAction oneAction = actions.get(action);

		if (null == oneAction)
		{
			return false;
		}

		return oneAction.isActionOverTime();

	}

	public synchronized void stopTimer(String action)
	{
		OneAction oneAction = actions.get(action);

		if (null == oneAction)
		{
			return;
		}

		oneAction.stopActionTimer();

	}

	public String tic(String actionName)
	{
		return tic(actionName, 3000);
	}

	public String tic(String actionName, long maxPermissibleTime)
	{

		permissibleTimeForMostRecentAction = maxPermissibleTime;
		actions.put(actionName, new OneAction(actionName, System.currentTimeMillis(), maxPermissibleTime));

		return "Started " + actionName;

	}

	public synchronized String toc()
	{

		if (actions.size() == 0)
		{
			return "No actions were recorded.";
		}

		Iterator<String> itr = actions.keySet().iterator();

		StringBuilder sb = new StringBuilder();

		while (itr.hasNext())
		{

			String key = itr.next();
			OneAction action = actions.get(key);

			long duration = action.getDuration();

			if (action.isActionOverTime())
			{
				sb.append("\n\n*********\nTICTOC WARNING: " + action.getActionName() + " completed in " + duration
						+ " > " + permissibleTimeForMostRecentAction + "\n*********\n");
			}

		}

		return sb.toString();
	}

	public synchronized String toc(String action)
	{
		OneAction oneAction = actions.get(action);

		if (null == oneAction)
		{
			return "Time not recorded for " + action;
		}

		if (oneAction.isActionOverTime())
		{

			return "\n\n*********\nTICTOC WARNING: " + action + " completed in " + oneAction.getDuration() + " > "
					+ oneAction.getMaxPermissibleTime() + "\n*********\n";
		}
		else
		{

			return "\n\nTICTOC ACTION TIMER: " + action + " completed in " + oneAction.getDuration() + "\n\n";
		}
	}

}
