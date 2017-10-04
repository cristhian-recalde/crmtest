package com.trilogy.app.crm.subscriber.agent;

import java.io.File;
import java.util.Date;

import com.trilogy.app.crm.bean.GenericBeanBulkLoadTask;
import com.trilogy.app.crm.bulkloader.generic.BulkloadConstants;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkLoadTaskManager;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloadManager;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.logger.FileLoggerConfig;
import com.trilogy.framework.xlog.logger.LoggerSupport;

/**
 * added for customer segmentation feature.
 * @author atul.mundra@redknee.com
 * @since dec 19, 2014
*/
public class SubscriptionSegmentUpdateLifecycleAgent extends LifecycleAgentScheduledTask{

	private static final String FILE_NAME = "SubSegmentReport.csv";
	private static final String BASE_DIR_NAME = "bulkLoader";
	private static final int BULK_LOADER_ID =1002;
	
	
	public SubscriptionSegmentUpdateLifecycleAgent(Context ctx, String agentId)
			throws AgentException 
	{
		super(ctx, agentId);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException 
	{
		GenericBeanBulkLoadTask subscriptionSegmentUpdateBeanBulkLoadTask = new GenericBeanBulkLoadTask();
		try
		{
			if(getParameter1(ctx, String.class) == null){
				throw new AgentException("Parameter 1 is null");
			}
			subscriptionSegmentUpdateBeanBulkLoadTask.setFileLocation(getParameter1(ctx, String.class));
			File file1 = new File(getParameter1(ctx, String.class));
			subscriptionSegmentUpdateBeanBulkLoadTask.setFileName(file1.getName());
			if(getParameter2(ctx, String.class) == null)
				subscriptionSegmentUpdateBeanBulkLoadTask.setLogDirectory(file1.getParent());
			else{
				File file2 = new File(getParameter2(ctx, String.class));
				if(!file2.isDirectory()){
					subscriptionSegmentUpdateBeanBulkLoadTask.setLogDirectory(file1.getParent());
				}
				else {
					subscriptionSegmentUpdateBeanBulkLoadTask.setLogDirectory(getParameter2(ctx, String.class));
				}
			}
			subscriptionSegmentUpdateBeanBulkLoadTask.setBulkLoader(BULK_LOADER_ID);
			renameLogFiles(ctx,subscriptionSegmentUpdateBeanBulkLoadTask);
			subscriptionSegmentUpdateBeanBulkLoadTask.execute(ctx);
			renameProcessedFiles(ctx,subscriptionSegmentUpdateBeanBulkLoadTask);
			
		}
		catch (AgentException e)
		{
			new MinorLogMsg(this, "bulk bean upload failed for Subscription Segment Update due to :" + e.getMessage(), e).log(ctx);
			throw new LifecycleException("bulk bean upload failed for Subscription Segment Update due to :" + e.getMessage());
		}
		catch(Exception e)
		{
			new MinorLogMsg(this, e.getMessage(), e).log(ctx);
			throw new LifecycleException("bulk bean upload failed for Subscription Segment Update due to :" + e.getMessage());
		}
		
	}
	/**
	 * method to rename processed files, as we will get always file with that name from reporting application.
	 * @param ctx
	 * @param task
	 */
	private void renameProcessedFiles(Context ctx,GenericBeanBulkLoadTask task)
	{
		String processedFilePath = task.getFileLocation();
		String logFileName = task.getBaseDirectory()+ task.getLogDirectory() +"/"+ task.getFileName();
		File errorFile = new File(logFileName + ".err");
		if(!errorFile.exists())
		{
			CalendarSupport calendar = CalendarSupportHelper.get(ctx);
			String fileRenamePostFix = "_"+ calendar.getDateWithNoTimeOfDay(new Date());
			File processedFile = new File(processedFilePath);
			File renamedFile = new File(processedFilePath + fileRenamePostFix );
			processedFile.renameTo(renamedFile);
		}
	}
	/**
	 * method to rename Log files as daily a new file will be getting created
	 * so we are renaming the old file to keep it.
	 * @param ctx
	 * @param task
	 */
	private void renameLogFiles(Context ctx,GenericBeanBulkLoadTask task)
	{
		final CalendarSupport calendar = CalendarSupportHelper.get(ctx);
		String processedLogFile = task.getLogDirectory();
		String fileRenamePostFix = "_"+ calendar.getDateWithNoTimeOfDay(new Date());
		String logFileName = task.getBaseDirectory()+processedLogFile +"/"+ task.getFileName();
		File errorFile = new File(logFileName + ".err");
		File logFile = new File(logFileName + ".log");
		
		if(errorFile.exists())
		{
			File renamedErrorFile = new File(logFileName + ".err" + fileRenamePostFix);
			errorFile.renameTo(renamedErrorFile);
		}
		if(logFile.exists())
		{
			File renamedLogFile = new File(logFileName + ".log" + fileRenamePostFix);
			logFile.renameTo(renamedLogFile);
		}
	}
}
