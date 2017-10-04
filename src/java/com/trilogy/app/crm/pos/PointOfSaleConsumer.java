/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.pos;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * Calls to run the POS extraction.
 * @author Angie Li
 */
public class PointOfSaleConsumer extends ContextAwareSupport implements ContextAgent 
{

    public PointOfSaleConsumer(Context ctx)
    {
        setContext(ctx);
    }
    
    public void execute(Context ctx) throws AgentException 
    {
        PointOfSaleFileWriter writer = (PointOfSaleFileWriter) ctx.get(PointOfSaleFileWriter.class);
        
        if (writer != null)
        {
            try
            {
                writer.writeFile(ctx);
                
                new InfoLogMsg(this, "Completed Extraction of the POS file=" + writer.getFileName(ctx), null).log(ctx);
            }
            catch (HomeException e)
            {
                throw new AgentException("Failed to write to POS file=" + writer.getFileName(ctx), e);
            }
        }
        else
        {
        	throw new AgentException("Could not retrieve PointOfSaleFileWriter from context.");
        }

    }

}
