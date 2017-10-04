/**
 * 
 */
package com.trilogy.app.crm.home;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.DependencyGroup;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;

/**
 * @author skularajasingham
 *
 */
public class DependencyGroupPipelineFactory implements PipelineFactory
{

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
	 */

	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx,DependencyGroup.class, "DependencyGroup");

        home = new IdentifierSettingHome(ctx, home, IdentifierEnum.AUTO_DEPENDENCY_GROUP_ID, null);
        
        IdentifierSequenceSupportHelper.get(ctx).ensureNextIdIsLargeEnough(ctx, IdentifierEnum.AUTO_DEPENDENCY_GROUP_ID,
            home);
        
        home = new SortingHome(home);

        return home;
	}

}
