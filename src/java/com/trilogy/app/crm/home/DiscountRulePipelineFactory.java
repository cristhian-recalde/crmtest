/**
 * 
 */
package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.app.crm.bean.ui.DiscountRule;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.home.validator.RemovalValidatingHome;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author ishan.batra
 * @since 9.9
 *
 */
public class DiscountRulePipelineFactory implements PipelineFactory
{

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
	 */

	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx,DiscountRule.class, "DiscountRule");

        home = new SortingHome(ctx, home);
        home = new SpidAwareHome(ctx, home);        
        
        home =  new DiscountRuleIDSettingHome(ctx, home);
        
        home =
                ConfigChangeRequestSupportHelper.get(ctx)
                    .registerHomeForConfigSharing(ctx, home, DiscountRule.class);
        
        LogSupport.info(ctx, this, "Discount Rule Home installed successfully");

        return home;
	}

}
