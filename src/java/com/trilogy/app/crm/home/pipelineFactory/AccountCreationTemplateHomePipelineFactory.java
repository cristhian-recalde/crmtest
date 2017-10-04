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

import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.account.NoDuplicateACTNameForSameSpidCheckingHome;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateXInfo;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.extension.account.AccountExtensionsValidator;
import com.trilogy.app.crm.extension.validator.SingleInstanceExtensionsValidator;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * @author cindy.wong@redknee.com
 * @since 2011-03-03
 */
public class AccountCreationTemplateHomePipelineFactory implements
    PipelineFactory
{

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	    throws RemoteException, HomeException, IOException, AgentException
	{
		Home home =
		    StorageSupportHelper.get(ctx).createHome(ctx,
		        AccountCreationTemplate.class, "ACCOUNTCREATIONTEMPLATE");
		home = new AdapterHome(home, new ExtensionSpidAdapter());
		home =
		    new IdentifierSettingHome(ctx, home,
		        IdentifierEnum.ACCOUNT_CREATION_TEMPLATE_ID, null);
		
		home = new ValidatingHome(new CompoundValidator().add(new AccountExtensionsValidator(AccountCreationTemplateXInfo.ACCOUNT_EXTENSIONS)), home);
		home = new SpidAwareHome(ctx, home);
		home = new NoDuplicateACTNameForSameSpidCheckingHome(ctx, home);
		home =
		    new ValidatingHome(
		        new CompoundValidator()
		            .add(new SingleInstanceExtensionsValidator()),
		        home);

		return home;
	}
}
