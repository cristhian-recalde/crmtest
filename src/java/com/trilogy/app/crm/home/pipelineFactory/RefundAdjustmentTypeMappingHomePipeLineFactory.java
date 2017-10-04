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









import com.trilogy.app.crm.bean.RefundAdjustmentTypeMapping;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.RefundAdjTypeMappingIDSettingHome;
import com.trilogy.app.crm.home.UpdateRefundAdjustmentMappingHome;
import com.trilogy.app.crm.home.account.RASValidator;
import com.trilogy.app.crm.home.account.RefundAdjustmenttypeMappingFieldsValidator;
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
public class RefundAdjustmentTypeMappingHomePipeLineFactory implements PipelineFactory{

	@Override
	public Home createPipeline(Context ctx, Context ctx1)
			throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = StorageSupportHelper.get(ctx).createHome(ctx,RefundAdjustmentTypeMapping.class, "REFUNDADJUSTMENTTYPEMAPPING", true);
		//home = new RefundAdjMapIsDefaultUpdateHome(ctx, home);
		home = new UpdateRefundAdjustmentMappingHome(ctx, home);
		//home = new SetRefundAdjsutmentTypeMappingReasonCodeField(ctx, home);
		home = new RefundAdjTypeMappingIDSettingHome(ctx, home);
		//home = new ValidatingHome(home, new RefundAdjustmenttypeMappingFieldsValidator());
		home = new ValidatingHome(new RefundAdjustmenttypeMappingFieldsValidator(), new RASValidator(), home);
		return home;
	}
}
