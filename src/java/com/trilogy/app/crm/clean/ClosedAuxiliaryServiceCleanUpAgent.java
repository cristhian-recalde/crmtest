package com.trilogy.app.crm.clean;

import java.util.Collection;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.clean.visitor.ClosedAuxiliaryServiceCleanUpVisitor;

/**
 * ContextAgent to remove dependencies for closed auxiliary services
 * 
 * @author Aaron Gourley
 * @since 8.2
 */

public class ClosedAuxiliaryServiceCleanUpAgent implements ContextAgent
{   
    public ClosedAuxiliaryServiceCleanUpAgent()
    {
        super();
    }
   
    public void execute(Context ctx) throws AgentException
    {   
        final Home accountHome = ( Home )ctx.get(AccountHome.class);       
        if ( accountHome == null )
        {
            throw new AgentException("System error: AccountHome not found in context");
        }

        Home spidHome = ( Home ) ctx.get(CRMSpidHome.class);
        if ( spidHome == null )
        {
            throw new AgentException("System error: CRMSpidHome not found in context");
        }


        try
        {
            Collection<CRMSpid> spids = spidHome.selectAll();
            for (CRMSpid spid : spids)
            {   
                And filter = new And();
                filter.add(new EQ(AuxiliaryServiceXInfo.STATE, AuxiliaryServiceStateEnum.CLOSED));
                filter.add(new EQ(AuxiliaryServiceXInfo.SPID, spid.getSpid()));

                Home auxSvcHome = (Home) ctx.get(AuxiliaryServiceHome.class);
                auxSvcHome.forEach(
                        ctx, 
                        ClosedAuxiliaryServiceCleanUpVisitor.instance(), 
                        filter);
            }  
        }
        catch(HomeException e)
        {
            String msg = "Error getting data from spid table";
            new MinorLogMsg(this, msg, e).log(ctx); 
            throw new AgentException(msg);
        }
    }
}
