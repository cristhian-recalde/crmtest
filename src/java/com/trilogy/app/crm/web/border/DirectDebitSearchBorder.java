package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.DirectDebitRecord;
import com.trilogy.app.crm.bean.DirectDebitRecordXInfo;
import com.trilogy.app.crm.bean.search.DirectDebitRecordSearch;
import com.trilogy.app.crm.bean.search.DirectDebitRecordSearchWebControl;
import com.trilogy.app.crm.bean.search.DirectDebitRecordSearchXInfo;
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

public class DirectDebitSearchBorder extends SearchBorder
{
    public DirectDebitSearchBorder(final Context context, final WebControl webControl)
    {
        super(context, DirectDebitRecord.class, webControl);
        registerSearchAgents();
        
    }
    public DirectDebitSearchBorder(final Context context)
    {
        super(context, DirectDebitRecord.class, new DirectDebitRecordSearchWebControl());
        registerSearchAgents();
    }


    public DirectDebitSearchBorder(final Context context, final Object homeKey)
    {
        super(context, homeKey, DirectDebitRecord.class, new DirectDebitRecordSearchWebControl());
        registerSearchAgents();
    }

    private void registerSearchAgents()
    {
        addAgent(new SelectSearchAgent(DirectDebitRecordXInfo.BAN, DirectDebitRecordSearchXInfo.BAN));

        addAgent(new ContextAgentProxy()
        {
           public void execute(Context ctx)
              throws AgentException
           {
        	   DirectDebitRecordSearch criteria = (DirectDebitRecordSearch)getCriteria(ctx);
              if (criteria.getState() != -1 )
              {
            	  doSelect(ctx, new EQ(DirectDebitRecordXInfo.STATE, Long.valueOf(criteria.getState())));

                   ContextAgents.doReturn(ctx);
              }
              delegate(ctx);
            }
          }
        );
        
        
        addAgent(new ContextAgentProxy()
        {
           public void execute(Context ctx)
              throws AgentException
           {
        	   DirectDebitRecordSearch criteria = (DirectDebitRecordSearch)getCriteria(ctx);
              if (criteria.getReasonCode() != -1)
              {
            	  doSelect(ctx, new EQ(DirectDebitRecordXInfo.REASON_CODE, Long.valueOf(criteria.getReasonCode())));
                 ContextAgents.doReturn(ctx);
              }
              delegate(ctx);
            }
          }
        );        
  
        addAgent(new LimitSearchAgent(DirectDebitRecordSearchXInfo.LIMIT));
              

        addAgent(new ContextAgentProxy()
        {
            @Override
            public void execute(final Context ctx) throws AgentException
            {
                final DirectDebitRecordSearch criteria = (DirectDebitRecordSearch) getCriteria(ctx);
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
    protected Object getPostedDateLogic(final Context ctx, final DirectDebitRecordSearch criteria)
    {
        ByDate dt = null;


        dt = new ByDate("requestDate");
 

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
