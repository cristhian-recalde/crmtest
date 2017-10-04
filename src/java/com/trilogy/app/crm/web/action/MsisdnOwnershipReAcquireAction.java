package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.MsisdnOwnershipHome;
import com.trilogy.app.crm.xhome.MsisdnOwnershipReAcquireCmd;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.LogSupport;

public class MsisdnOwnershipReAcquireAction
    extends SimpleWebAction
{
    public MsisdnOwnershipReAcquireAction()
    {
        
    }

    public void execute(Context ctx)
        throws AgentException
    {
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "MSISDN Ownership Re-Acquire Action INVOKED.");
        }

        String key = WebAgents.getParameter(ctx, "key");
        PrintWriter out = WebAgents.getWriter(ctx);
        Home h = (Home)ctx.get(MsisdnOwnershipHome.class);
        MsisdnOwnershipReAcquireCmd cmd = new MsisdnOwnershipReAcquireCmd(key);

        try
        {
            h.cmd(ctx, cmd);
        }
        catch(HomeException e)
        {
            throw new AgentException(e);
        }

        String message = "Successfully re-acquired MSISDN [" + key + "]. ";
        message = "<table width=\"70%\"><tr><td><center><b>" + message + "</b></center></td></tr></table>";

        out.println(message);

        ContextAgents.doReturn(ctx);
//        returnToSummaryView(ctx);
    }
}