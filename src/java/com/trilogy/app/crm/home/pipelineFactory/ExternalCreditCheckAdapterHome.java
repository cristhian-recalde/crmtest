package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.app.crm.bean.ExternalCreditCheck;
import com.trilogy.app.crm.creditcheck.ExternalCreditCheckSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

public class ExternalCreditCheckAdapterHome extends HomeProxy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String MODULE = ExternalCreditCheckAdapterHome.class.getName();
	public ExternalCreditCheckAdapterHome(Context ctx, Home home)
	{
		super(ctx, home);
	}
	
	@Override
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException {
		ExternalCreditCheck externalCreditCheck = (ExternalCreditCheck) obj;
		LogSupport.info(ctx, MODULE, "<<< SPID: " + externalCreditCheck.getSpid()+" BAN: "+externalCreditCheck.getBan()+" CCDATE: "+externalCreditCheck.getCreditCheckDate());
		LogSupport.info(ctx, MODULE, "<<< CCR: " + externalCreditCheck.getCreditCheckResult()+" SCC: "+externalCreditCheck.getSystemDefinedCreditCategory()+" MUCC: "+externalCreditCheck.getManuallyUpdatedCreditCategory()+" AU: "+externalCreditCheck.getAssociatedUser());
		ExternalCreditCheckSupport.validateSystemDefinedCreditCategory(ctx, externalCreditCheck);
		ExternalCreditCheckSupport.validateManuallyUpdatedCreditCategory(ctx, externalCreditCheck);
		ExternalCreditCheckSupport.validateCreditCheckResults(ctx, externalCreditCheck);
		return super.store(ctx, obj);
	}
}
