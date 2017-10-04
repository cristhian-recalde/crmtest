/*
 * Created on Jul 29, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

import com.trilogy.app.crm.bean.PersonalListPlan;
import com.trilogy.app.crm.bean.PersonalListPlanSearch;
import com.trilogy.app.crm.bean.PersonalListPlanSearchWebControl;
import com.trilogy.app.crm.bean.PersonalListPlanSearchXInfo;
import com.trilogy.app.crm.bean.PersonalListPlanXInfo;

/**
 * @author candy
 */
public class PersonalListPlanSearchBorder extends SearchBorder
{
    
    public PersonalListPlanSearchBorder(final Context context)
    {	
        super(context, PersonalListPlan.class, new PersonalListPlanSearchWebControl());

        // id
        
        addAgent(new ContextAgentProxy()
           {
              public void execute(Context ctx)
                 throws AgentException
              {
                 PersonalListPlanSearch criteria = (PersonalListPlanSearch)getCriteria(ctx);

                 if (criteria.getID() != -1)
                 {
                    doSelect(
                       ctx,
                       new EQ(PersonalListPlanXInfo.ID, Long.valueOf(criteria.getID())));
                 }
                 delegate(ctx);
              }
           }
        );

        // name
        addAgent(new SelectSearchAgent(PersonalListPlanXInfo.NAME, PersonalListPlanSearchXInfo.NAME,false));
        

     	SelectSearchAgent spidAgent = new SelectSearchAgent(PersonalListPlanXInfo.SPID, PersonalListPlanSearchXInfo.SPID);
     	addAgent(spidAgent.addIgnore(Integer.valueOf(9999)));
        

    }   
}
