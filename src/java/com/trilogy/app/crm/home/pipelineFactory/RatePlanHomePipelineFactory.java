package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.ModelCrmConstants;
import com.trilogy.app.crm.bean.priceplan.RatePlanHome;
import com.trilogy.app.crm.bean.priceplan.RatePlanTypeEnum;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.priceplan.rateplan.RatePlanCorbaHome;

public class RatePlanHomePipelineFactory implements PipelineFactory
{
    /**
     * Create a new instance of <code>RatePlanHomePipelineFactory</code>.
     */
    protected RatePlanHomePipelineFactory()
    {
        // empty
    }

    /**
     * Returns an instance of <code>RatePlanHomePipelineFactory</code>.
     *
     * @return An instance of <code>RatePlanHomePipelineFactory</code>.
     */
    public static RatePlanHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new RatePlanHomePipelineFactory();
        }
        return instance;
    }	
    
	@Override
	public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException, AgentException
	{
		Home home = new RatePlanCorbaHome(ctx);
		
		// Install the general RatePlanHome
		ctx.put(RatePlanHome.class, home);
		
		// Install the Voice RatePlanHome
		ctx.put(ModelCrmConstants.RATE_PLAN_VOICE_HOME_KEY, new RatePlanCorbaHome(ctx, RatePlanTypeEnum.VOICE));
		
		// Install the SMS RatePlanHome
		ctx.put(ModelCrmConstants.RATE_PLAN_SMS_HOME_KEY, new RatePlanCorbaHome(ctx, RatePlanTypeEnum.SMS));
		
		// Install the Data RatePlanHome (for future use)
		ctx.put(ModelCrmConstants.RATE_PLAN_DATA_HOME_KEY, new RatePlanCorbaHome(ctx, RatePlanTypeEnum.DATA));
		
		return home;
	}

    /**
     * Singleton instance.
     */
    private static RatePlanHomePipelineFactory instance;
}
