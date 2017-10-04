package com.trilogy.app.crm.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import com.trilogy.app.crm.bean.ClosedUserGroupIdentitySupport;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplateHome;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Implements the common tasks for actions related to the ClosedUserGroupTemplate.
 * @author marcio.marques@redknee.com
 *
 */
public abstract class ClosedUserGroupTemplateAction extends SimpleWebAction
{
    /**
     * Creates a bean with the specific key and label set.
     * @param key Key.
     * @param label Label.
     */
   public ClosedUserGroupTemplateAction(String key, String label)
    {
        super(key, label);
    }

    /**
     * Writes a link for the action.
     * @param ctx Context.
     * @param out Output print writer.
     * @param bean Bean.
     * @param link Link.
     * @param action Action being performed.
     */
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link, String action)
    {
        link.add("action", getKey());

        out.print("<a href=\"");
        link.write(out);
        out.print("\" onclick=\"try{return confirm('Proceed with " + action + " of the CUG Template ");
        out.print(XBeans.getIdentifier(bean));
        out.print("');}catch(everything){}\">");
        out.print(getLabel());
        out.print("</a>");
    } 
    
    /**
     * Executes the specific action command.
     * @param ctx Context.
     * @param cugTemplate Closed user group template.
     * @param home Closed user group template home.
     * @param key Closed user group template key.
     * @throws Throwable
     */
    abstract protected void executeAction(Context ctx, ClosedUserGroupTemplate cugTemplate, Home home, String key) throws Throwable;

    /**
     * Executes the action
     * @param ctx Context.
     */
    public void execute(Context ctx) throws AgentException
    {
        String stringKey = WebAgents.getParameter(ctx, "key");
        String cmd = WebAgents.getParameter(ctx, "cmd");

        IdentitySupport idSupport = ClosedUserGroupIdentitySupport.instance();

        try
        {
            Home home = (Home) ctx
                    .get(ClosedUserGroupTemplateHome.class);
            ClosedUserGroupTemplate cugTemplate = (ClosedUserGroupTemplate) home.find(ctx, idSupport
                    .fromStringID(stringKey));
            
            executeAction(ctx, cugTemplate, home, stringKey);
            
        }
        catch (Throwable t)
        {
            String message = "Failed to " + getKey() + " CUG Template " + stringKey + ": " + t.getMessage();
            WebAgents.getWriter(ctx).println("<font color=red>" + message + "</font><br/><br/>");
            new MinorLogMsg(this, message, t).log(ctx);       
        }
        
        Link link = new Link(ctx);
        link.add("cmd", cmd);

        try
        {
            WebAgents.service(ctx, link.write(), WebAgents.getWriter(ctx));
        }
        catch (ServletException ex)
        {
            throw new AgentException("Fail to redirect to " + cmd, ex);
        }
        catch (IOException ioEx)
        {
            throw new AgentException("Fail to redirect to " + cmd, ioEx);
        }
    }    

}
