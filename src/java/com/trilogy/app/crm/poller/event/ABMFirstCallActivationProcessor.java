package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.agent.ABMFirstCallActivationAgent;
import com.trilogy.framework.xhome.context.Context;


/**
 * Processor for the ER453 Activation ER from ABM
 * 
 * @author rpatel
 *
 */
public class ABMFirstCallActivationProcessor extends ABMProcessor
{
    public static final int ABM_FCA_ER_IDENTIFIER = 453;
    
    public ABMFirstCallActivationProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "ABMFirstCallActivation", "ABMFirstCallActivationErrFile", queueSize, threads, new ABMFirstCallActivationAgent(this));
    }
}
