package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.interfaces.crm.hlr.HlrID;
import com.trilogy.interfaces.crm.hlr.HlrIDHome;

public class HlrIDHomePipelineFactory implements PipelineFactory {

	/**
	 * Singleton instance.
	 */
	private static HlrIDHomePipelineFactory instance_;

	/**
	 * Create a new instance of <code>CoreBillCyclePipelineFactory</code>.
	 */
	protected HlrIDHomePipelineFactory() {
		// empty
	}

	/**
	 * Returns an instance of <code>CoreBillCyclePipelineFactory</code>.
	 * 
	 * @return An instance of <code>CoreBillCyclePipelineFactory</code>.
	 */
	public static PipelineFactory instance() {
		if (instance_ == null) {
			instance_ = new HlrIDHomePipelineFactory();
		}
		return instance_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {

		Home home = CoreSupport.bindHome(ctx, HlrID.class, true);
		home = new AuditJournalHome(ctx, home);
		ctx.put(HlrIDHome.class, home);

		return home;
	}

}
