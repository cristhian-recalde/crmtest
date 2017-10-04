package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.elang.EQ;

import com.trilogy.app.crm.bean.*;

/**
 *
 * @author chandrachud.ingale
 * @since  
 */
public class SubscriberSegmentSearchBorder extends SearchBorder
{
    
    public SubscriberSegmentSearchBorder(final Context context)
    {	
        super(context, SubscriberSegment.class, new SubscriberSegmentSearchWebControl());

        // spid
        addAgent(new ContextAgentProxy()
           {
              public void execute(Context ctx)
                 throws AgentException
              {
                  SubscriberSegmentSearch criteria = (SubscriberSegmentSearch)getCriteria(ctx);

                 if (criteria.getSPID() != SubscriberSegmentSearch.DEFAULT_SPID)
                 {
                 	doSelect(
                       ctx,
                       new EQ(SubscriberSegmentXInfo.SPID, Integer.valueOf(criteria.getSPID())));
                 }
                 delegate(ctx);
              }
           }
        );
    }   
}
