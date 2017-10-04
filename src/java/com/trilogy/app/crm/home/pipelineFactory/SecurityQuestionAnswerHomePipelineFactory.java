/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.account.SecurityQuestionAnswer;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SpidAwareSecurityQuestionAnswerInvocationHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author Marcio Marques
 * @since 9.3
 */
public class SecurityQuestionAnswerHomePipelineFactory implements
    PipelineFactory
{
	public static SecurityQuestionAnswerHomePipelineFactory instance()
	{
		return INSTANCE;
	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home =
		        StorageSupportHelper.get(ctx).createHome(ctx, SecurityQuestionAnswer.class, "SECURITYQUESTIONANSWER");
		home = new SpidAwareSecurityQuestionAnswerInvocationHome(home);
		home = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(
		    ctx, home, SecurityQuestionAnswer.class);
		return home;
	}

    private static final SecurityQuestionAnswerHomePipelineFactory INSTANCE =
            new SecurityQuestionAnswerHomePipelineFactory();

}
