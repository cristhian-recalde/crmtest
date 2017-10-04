package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateSearch;
import com.trilogy.app.crm.bean.AccountCreationTemplateSearchWebControl;
import com.trilogy.app.crm.bean.AccountCreationTemplateSearchXInfo;
import com.trilogy.app.crm.bean.AccountCreationTemplateXInfo;

/**
 * Search border for AccountCreationTemplate objects.
 * @author marcio.marques@redknee.com
 *
 */
public class AccountCreationTemplateSearchBorder  extends SearchBorder
{
    public AccountCreationTemplateSearchBorder(Context ctx)
    {
       super(ctx, AccountCreationTemplate.class, new AccountCreationTemplateSearchWebControl());


         // identifier
         addAgent(new ContextAgentProxy()
               {
                  public void execute(Context ctx)
                     throws AgentException
                  {
                      AccountCreationTemplateSearch criteria = (AccountCreationTemplateSearch)getCriteria(ctx);

                     if (criteria.getIdentifier() > -1)
                     {
                        doSelect(
                           ctx,
                           new EQ(AccountCreationTemplateXInfo.IDENTIFIER, Long.valueOf(criteria.getIdentifier())));
                     }
                     delegate(ctx);
                  }
               }
         );

         // spid
         addAgent(new ContextAgentProxy()
               {
                  public void execute(Context ctx)
                     throws AgentException
                  {
                      AccountCreationTemplateSearch criteria = (AccountCreationTemplateSearch)getCriteria(ctx);

                     if (criteria.getSpid() > -1)
                     {
                        doSelect(
                           ctx,
                           new EQ(AccountCreationTemplateXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                     }
                     delegate(ctx);
                  }
               }
         );

         // name
         addAgent(new WildcardSelectSearchAgent(AccountCreationTemplateXInfo.NAME,
                AccountCreationTemplateSearchXInfo.NAME, true));
      
    }

}
