package com.trilogy.app.crm.client.ringbacktone;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;

/**
 * Not intended for use outside of the RBT package
 * 
 * @author Nick Landry
 *
 */
class SwitchProxy extends ClientProxy
{
	
	// The typical test mode
    public static final String TEST_LICENSE_KEY = "DEV - Test Ring Back Tone Client";
    
    // TODO: future support of a rule simulator (for better testing coverage)
    public static final String SIM_LICENSE_KEY = "DEV - Simulator Ring Back Tone Client";

	public SwitchProxy(RBTClient delegate)
	{
		super(delegate);
	}

	@Override
	public RBTClient getDelegate(Context context)
	{
		LicenseMgr licenseManager = (LicenseMgr)context.get(LicenseMgr.class);
		
		if (licenseManager!=null && licenseManager.isLicensed(context, TEST_LICENSE_KEY))
		{
			return nullClient;
		}
		
		if (licenseManager!=null && licenseManager.isLicensed(context, SIM_LICENSE_KEY))
		{
		    //TODO: How to load Simulator. Using reflection or get it from context.
		    RBTClient sim = (RBTClient) context.get(RBTClientSim.class);
		    if (sim != null) return sim;
		}
		
		return super.getDelegate(context);
	}
	
	private static final RBTClient nullClient = new NullClient(); 
}
