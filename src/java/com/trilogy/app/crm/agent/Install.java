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
package com.trilogy.app.crm.agent;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;

/**
 * Installation Agent. This is the class that is called by Core to start the app.
 **/
public class Install implements ContextAgent
{
	/**
	 * Calls the other installation agents.
	 * @param ctx the context passed to all other install agents
	 * @throws AgentException
	 */
	public void execute(Context ctx) throws AgentException
	{
        // Ensure that the deployment type is initialized.
        DeploymentTypeSupportHelper.get(ctx).initializeDeploymentType(ctx);

        try
		{
			new com.redknee.service.home.operations.agent.Install(ctx,8424);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		try
		{
            new InfoLogMsg(this, "AppCrm installation starting.", null).log(ctx);

			// REVIEW(cleanup): Removed commented-out code. GEA
			new AuthInstall().execute(ctx);

            new ModuleInstall().execute(ctx);

			// on first run some homes need to be here no matter what
			new PreStorageInstall().execute(ctx);

			new BeanInstall().execute(ctx);
			new BeanFactoryInstall().execute(ctx);

			new FacetInstall().execute(ctx);
			
			new StorageInstall().execute(ctx);
            
			new MigrationInstall().execute(ctx);
			
			new ServiceInstall().execute(ctx);
            
            new WebInstall().execute(ctx);

         	if (!DeploymentTypeSupportHelper.get(ctx).isEcare(ctx))
         	{
         		new com.redknee.service.task.executor.agent.Install().execute(ctx);
         	    new CronInstall().execute(ctx);
         	}

            new com.redknee.app.crm.bundle.Install().execute(ctx);            

	    //create default configuration related to  new deployment
	    
	    if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.NEWDEPLOYMENT_LICENSE_KEY))
            {
                new NewDepolymentInstall().execute(ctx);
            }        
	    
	    
            new InfoLogMsg(this, "AppCrm installation completed normally.", null).log(ctx);
		}
		catch (Throwable t)
		{
		    new CritLogMsg(this, "AppCrm installation failed due to exception.", t).log(ctx);
			throw new AgentException("Fail to complete AppCrm install", t);
		}

	}

	public static void failAndContinue(Context ctx, String component, Throwable e)
	{
		com.redknee.app.crm.core.agent.Install.failAndContinue(ctx, component, e);
	}
}
