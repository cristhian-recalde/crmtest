/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader.generic.request;

import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentControl;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloadManager;
import com.trilogy.app.crm.bulkloader.generic.GenericBeanBulkloaderRequest;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import java.io.File;
import java.util.Arrays;

/**
 * 
 * 
 * @author sanjay.pagar
 * @since
 */
public class BulkLoadLifeCycleAgent extends LifecycleAgentScheduledTask {

	/**
	 * @param ctx
	 * @param agentId
	 * @throws AgentException
	 */
	public BulkLoadLifeCycleAgent(Context ctx, String agentId)
			throws AgentException {
		super(ctx, agentId);
	}

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */

	protected void start(Context ctx) throws LifecycleException, HomeException {
		try {
			
			String inputPath = null;
			File loadDir = null;
			TaskEntry task = TaskHelper.retrieve(ctx, CronConstant.GENERIC_BEAN_BULK_LOAD_PROCESS_NAME);
			if(task != null)
			{
				String inboundPathConfig = task.getParam0();

				if(inboundPathConfig != null && inboundPathConfig.length()>0 )
				{
					inputPath = inboundPathConfig;
					loadDir = new File(inboundPathConfig);
					if(!loadDir.exists())
					{
						throw new IllegalArgumentException("Input directory not found");
						
					}					
				}
				else
				{
					throw new IllegalArgumentException("Invalid input directory- please enter valid directory");
				}
				String processedDirPath = inputPath.trim() + File.separator + "processeddir";
				String reportDir = inputPath.trim() + File.separator+ "reportdir";
				
				File processedDir = new File(processedDirPath);
				File files[]= loadDir.listFiles();
							
				for (int i = 0, n = files.length; i < n; i++) 
				{
					new MajorLogMsg(this,"processing [" + files[i].toString()+ "]");
	
					if (files[i].isDirectory())
					{
						continue;
					}
					startBulkLoad(ctx, files[i].toString(), reportDir);										
					
					MoveProcessedFile(files[i], processedDir);
					
				}
			}
		} catch (Exception e) {
			new MajorLogMsg(this, "Error occurred in lifecycle agent ["
					+ getAgentId()
					+ "] while executing wholeSale price plan bulk load", e)
         			.log(ctx);
		}
	}

	/**
	 * This method creates a GUI controllable lifecycle agent bean. It may be
	 * overridden in implemenation class, although it is not likely to be.
	 * 
	 * @param ctx
	 *            Operating Context
	 * @param agentId
	 * @return
	 * @throws HomeInternalException
	 * @throws HomeException
	 */
	protected LifecycleAgentControl createLifecycleAgentControl(Context ctx)
			throws HomeInternalException, HomeException {
		LifecycleAgentControl ctl = new LifecycleAgentControl();

		ctl.setInitialState(LifecycleStateEnum.INITIALIZE);

		// Don't start this agent with the lifecycle manager
		ctl.setDependent(false);

		return ctl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getTrans() {
		return true;
	}

	protected void startBulkLoad(Context ctx, String filePath,
			String reportfilePath) {
		  final Context sCtx = ctx.createSubContext();
		GenericBeanBulkloaderRequest form = new GenericBeanBulkloaderRequest();
		int bulkloader = 101;
		form.setBulkloader(bulkloader);
		form.setFilePath(filePath);
		form.setReportFilePath(reportfilePath);

		GenericBeanBulkloadManager bulkloadMgr = new GenericBeanBulkloadManager();
		sCtx.put(GenericBeanBulkloadManager.class, bulkloadMgr);

		// Validate form information
		bulkloadMgr.validate(sCtx, form);

		// Perform bulkloading
		bulkloadMgr.bulkload(sCtx, form);	
				
	}

	protected void MoveProcessedFile(File file, File dir)
	{
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		file.renameTo(new File(dir, file.getName()));
		
	}
}
