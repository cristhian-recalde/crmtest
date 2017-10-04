package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.*;
import com.trilogy.framework.xhome.web.search.*;
import com.trilogy.framework.xhome.elang.EQ;

import com.trilogy.app.crm.bean.*;

/**
 *
 * @author chandrachud.ingale
 * @since  9.6
 */
public class BlacklistWhitelistTemplateSearchBorder extends SearchBorder
{
    
    public BlacklistWhitelistTemplateSearchBorder(final Context context)
    {	
        super(context, BlacklistWhitelistTemplate.class, new BlacklistWhitelistTemplateSearchWebControl());

        // spid
        addAgent(new ContextAgentProxy()
           {
              public void execute(Context ctx)
                 throws AgentException
              {
                  BlacklistWhitelistTemplateSearch criteria = (BlacklistWhitelistTemplateSearch)getCriteria(ctx);

                 if (criteria.getSPID() != BlacklistWhitelistTemplateSearch.DEFAULT_SPID)
                 {
                 	doSelect(
                       ctx,
                       new EQ(BlacklistWhitelistTemplateXInfo.SPID, Integer.valueOf(criteria.getSPID())));
                 }
                 delegate(ctx);
              }
           }
        );
    }   
}
