package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.support.AccountSupport;


public class PTPResetWebControl extends ProxyWebControl
{

    public PTPResetWebControl()
    {
    }


    public void toWeb(Context context, final PrintWriter out, final String name, final Object object)
    {
        if (!hasPermission(context))
            return;
        final StringWriter buffer = new StringWriter();
        final PrintWriter printBuffer = new PrintWriter(buffer);
        Account act = null;
        String ban = (String) object;
        try
        {
            act = AccountSupport.getAccount(context, ban);
            Home ccHome = (Home) context.get(CreditCategoryHome.class);
            CreditCategory cc = (CreditCategory) ccHome.find(context, Integer.valueOf(act.getCreditCategory()));
            printBuffer.flush();
            if ((cc.getMaxNumberPTP() <= act.getCurrentNumPTPTransitions()) && (act.getCurrentNumPTPTransitions() != 0))
            {
                super.toWeb(context, printBuffer, name, object);
                
                Link link = new Link(context);
                /*
                link.addRaw("key", object.toString());
                link.addRaw("resetPTP", "resetPTP");
                link.writeLink(out, "clear");
                */
                out.print("<a href=\""+ link.getURI() + "?key="+ object.toString() + "&amp;resetPTP=resetPTP\" onClick=\" javascript:if(!confirm('Number of PTP is to be set to 0. The account is going out of PTP state.')) return false; \">clear</a>");
            }
        }
        catch (Exception e)
        {
            out.print("Error in the link to clear the number of PTP transitions");
            new MinorLogMsg(this, "Error in the link to clear the number of PTP transitions", e).log(context);
        }
    }


    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        return null;
    }


    public boolean hasPermission(Context ctx)
    {
        AuthMgr authMgr = new AuthMgr(ctx);
        return authMgr.check("crm.admin");
    }
}