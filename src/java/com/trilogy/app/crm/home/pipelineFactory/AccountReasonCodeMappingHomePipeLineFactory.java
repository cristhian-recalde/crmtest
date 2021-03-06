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

package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountStateChangeReason;
import com.trilogy.app.crm.home.AccountReasonCodeMappingIDSettingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.account.ASRCodeMappingValidator;
import com.trilogy.app.crm.home.account.AccountReasonCodeMappingStoreValidator;
import com.trilogy.app.crm.home.account.AccountReasonCodeMappingUpdateHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

/**
 * 
 * @author skambab
 *
 */
public class AccountReasonCodeMappingHomePipeLineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context ctx1)
			throws RemoteException, HomeException, IOException, AgentException {
		
		Home home = StorageSupportHelper.get(ctx).createHome(ctx,AccountReasonCodeMapping.class, "ACCOUNTREASONCODEMAPPING", true);
		home = new AccountReasonCodeMappingUpdateHome(ctx, home);
		home = new AccountReasonCodeMappingIDSettingHome(ctx, home);
		//home = new ValidatingHome(home, new ASRCodeMappingValidator());
		home = new ValidatingHome(new ASRCodeMappingValidator(), new AccountReasonCodeMappingStoreValidator(), home);
		home =ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, home, AccountReasonCodeMapping.class);
		return home;
	}

}
