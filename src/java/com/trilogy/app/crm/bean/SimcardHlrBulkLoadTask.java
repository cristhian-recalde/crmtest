package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.hlr.bulkload.SimcardHlrProvisionBulkloader;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;

public class SimcardHlrBulkLoadTask 
extends AbstractSimcardHlrBulkLoadTask
{

    @Override
    public void execute(Context ctx) throws AgentException
    {
    	SimcardHlrProvisionBulkloader loader = new SimcardHlrProvisionBulkloader(this);
    	
    	loader.process(ctx); 
    }
}
