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

import com.trilogy.app.crm.bean.account.SpidAwareSecurityQuestionAnswer;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;

/**
 * @author Suyash Gaidhani
 * @since 9.2
 */
public class SpidAwareSecurityQuestionAnswerHomePipelineFactory implements
    PipelineFactory
{
	public static SpidAwareSecurityQuestionAnswerHomePipelineFactory instance()
	{
		return INSTANCE;
	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = new NullHome(ctx);
		
		home = ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(
		    ctx, home, SpidAwareSecurityQuestionAnswer.class);
		return home;
	}

    private static final SpidAwareSecurityQuestionAnswerHomePipelineFactory INSTANCE =
            new SpidAwareSecurityQuestionAnswerHomePipelineFactory();

}
