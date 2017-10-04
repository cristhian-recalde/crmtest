package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.DDRFailureMessage;
import com.trilogy.app.crm.bean.DDRFailureMessageXInfo;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.search.DDRFailureMessageSearch;
import com.trilogy.app.crm.bean.search.DDRFailureMessageSearchWebControl;
import com.trilogy.app.crm.bean.search.DDRFailureMessageSearchXInfo;
import com.trilogy.app.crm.bean.search.DirectDebitRecordSearch;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.web.search.LimitSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.xdb.ByDate;

public class DDRFailureMessageSearchBorder extends SearchBorder
{
    public DDRFailureMessageSearchBorder(final Context context, final WebControl webControl)
    {
        super(context, DDRFailureMessage.class, webControl);
        registerSearchAgents();
        
    }
    public DDRFailureMessageSearchBorder(final Context context)
    {
        super(context, DDRFailureMessage.class, new DDRFailureMessageSearchWebControl());
        registerSearchAgents();
    }


    public DDRFailureMessageSearchBorder(final Context context, final Object homeKey)
    {
        super(context, homeKey, DDRFailureMessage.class, new DDRFailureMessageSearchWebControl());
        registerSearchAgents();
    }

    private void registerSearchAgents()
    {
        addAgent(new SelectSearchAgent(DDRFailureMessageXInfo.BAN, DDRFailureMessageSearchXInfo.BAN));

        addAgent(new ContextAgentProxy()
        {
           public void execute(Context ctx)
              throws AgentException
           {
        	   DDRFailureMessageSearch criteria = (DDRFailureMessageSearch)getCriteria(ctx);
              if (criteria.getMessageType() != -1) 
              {	  
             	  SearchBorder.doSelect(ctx, new EQ(DDRFailureMessageXInfo.MESSAGE_TYPE, Long.valueOf(criteria.getMessageType())));
              }
              delegate(ctx);
            }
          }
        );

        addAgent(new LimitSearchAgent(DDRFailureMessageSearchXInfo.LIMIT));
              

        addAgent(new ContextAgentProxy()
        {
            @Override
            public void execute(final Context ctx) throws AgentException
            {
                final DDRFailureMessageSearch criteria = (DDRFailureMessageSearch) getCriteria(ctx);
                doSelect(ctx, getPostedDateLogic(ctx, criteria));

                delegate(ctx);
            }
        }
        );

    }

    /**
     * Creates the Logic object coresponding to the postedDate part of the search.
     * @param ctx
     * @param criteria
     * @return the Logic object
     */
    protected Object getPostedDateLogic(final Context ctx, final DDRFailureMessageSearch criteria)
    {
        ByDate dt = null;


        dt = new ByDate("generatedDate");
 

        boolean bdt = false;
        if (criteria.getStartDate() != null)
        {
            dt.setStartInclusive(criteria.getStartDate().getTime());
            bdt = true;
        }

        if (criteria.getEndDate() != null)
        {
            dt.setEndInclusive(criteria.getEndDate().getTime());
            bdt = true;
        }

        return bdt ? dt : True.instance();
    }

}
