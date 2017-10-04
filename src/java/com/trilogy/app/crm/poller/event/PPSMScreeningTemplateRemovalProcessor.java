package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.agent.PPSMScreeningTemplateRemovalAgent;
import com.trilogy.framework.xhome.context.Context;


public class PPSMScreeningTemplateRemovalProcessor extends CRMProcessor
{
    public static final int PPSM_SCREENING_TEMPLATE_REMOVAL_ER_IDENTIFIER = 7018;

    public PPSMScreeningTemplateRemovalProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx,"PPSMScreeningTemplateRemoval", "PPSMScreeningTemplateRemoval", queueSize, threads, new PPSMScreeningTemplateRemovalAgent(this));
     }
    }
