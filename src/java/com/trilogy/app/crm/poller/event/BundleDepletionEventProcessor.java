package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.agent.BundleDepletionEventAgent;
import com.trilogy.framework.xhome.context.Context;

public class BundleDepletionEventProcessor extends CRMProcessor implements Constants{

	public BundleDepletionEventProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "Bundle Depetion Event", "BundleDepletionEventERErrFile", queueSize, threads, new BundleDepletionEventAgent(this));
    }
}
