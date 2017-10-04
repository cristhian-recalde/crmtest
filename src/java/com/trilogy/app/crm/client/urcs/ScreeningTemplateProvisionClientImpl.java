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
package com.trilogy.app.crm.client.urcs;

import com.trilogy.app.crm.client.AbstractCrmClient;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;
import com.trilogy.product.bundle.manager.provision.profile.error.ErrorCode;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplate;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplateProvision;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ScreeningTemplateReturnParam;
import com.trilogy.product.bundle.manager.provision.v5_0.screeningTemplate.ServiceLevelUsage;



/**
 * CORBA version of screeningTemplateClient, provides Screening template Provision API on URCS.
 * 
 * @author ankit.nagpal@redknee.com
 * @since 9.9
 */
public class ScreeningTemplateProvisionClientImpl extends AbstractCrmClient<ScreeningTemplateProvision>
        implements ScreeningTemplateProvisionClient
{

    private static final Class<ScreeningTemplateProvision> SERVICE_TYPE = ScreeningTemplateProvision.class;
    private static final String FAILED_MESSAGE_PREFIX = "CORBA comunication failure during ";
    private static final String FAILED_MESSAGE_SUFFIX = " failed.";
    public static final String URCS_SERVICE_NAME = "ScreeningTemplateProvisionV5";
    public static final String URCS_SERVICE_DESCRIPTION = "CORBA client for Screening Template services";


    public ScreeningTemplateProvisionClientImpl(final Context ctx)
    {
        super(ctx, URCS_SERVICE_NAME, URCS_SERVICE_DESCRIPTION, SERVICE_TYPE);
    }


    @Override
    public String version()
    {
        return "1.0";
    }



	@Override
	public ScreeningTemplateReturnParam updateScreeningTemplate(Context ctx,
			int actionIdentifier, ScreeningTemplate screeningTemplate,
			ServiceLevelUsage[] serviceLevelUsageArray)
			throws RemoteServiceException
	{
        final String methodName = "updateScreeningTemplateProvision";
        final ScreeningTemplateProvision client = getClient(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            String msg = String.format("Creating screening template on URCS", methodName);
            new DebugLogMsg(this, msg, null).log(ctx);
        }
        //Input
        final Parameter[] inParams = new Parameter[0];
        
        //Output
        final ScreeningTemplateReturnParam screeningTemplateReturnParam;
             
        try
        {
        	screeningTemplateReturnParam = client.updateScreeningTemplate(actionIdentifier, screeningTemplate, serviceLevelUsageArray, inParams);
        }
        catch (Exception exception)
        {
            throw new RemoteServiceException(ErrorCode.INTERNAL_ERROR, FAILED_MESSAGE_PREFIX + methodName
                    + FAILED_MESSAGE_SUFFIX, exception);
        }

        if (screeningTemplateReturnParam.resultCode != ErrorCode.SUCCESS)
        {
            final String msg = String.format(
                    "Failure in URCS method while creating screening template profile",
                    methodName, screeningTemplateReturnParam.resultCode);
            new MinorLogMsg(this, msg, null).log(ctx);
            throw new RemoteServiceException(screeningTemplateReturnParam.resultCode, msg);
        }
        return screeningTemplateReturnParam;
    }

}
