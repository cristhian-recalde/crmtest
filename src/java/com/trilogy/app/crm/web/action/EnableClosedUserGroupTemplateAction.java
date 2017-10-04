package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;


/**
 * Action responsible for enabling a closed user group template.
 * @author marcio.marques@redknee.com
 *
 */
public class EnableClosedUserGroupTemplateAction extends ClosedUserGroupTemplateAction
{
    /**
     * Creates an empty bean.
     */
    public EnableClosedUserGroupTemplateAction()
    {
        super("enable", "Enable");
        defaultHelpText_ = "Enable the current bean.";        
    }
    
    /**
     * Creates an empty bean with the permission set.
     * @param permission Permission.
     */
    public EnableClosedUserGroupTemplateAction(final Permission permission)
    {
        this();
        setPermission(permission);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Context ctx, Object bean)
    {
        if (bean == null)
        {
            return false;
        }
        else if (bean instanceof ClosedUserGroupTemplate)
        {
            return ((ClosedUserGroupTemplate) bean).isDeprecated();
        }
        else
        {
            return false;
        }
    } 
    
    /**
     * {@inheritDoc}
     */
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
       writeLink(ctx, out, bean, link, "enabling");
    }

    
    /**
     * {@inheritDoc}
     */
    protected void executeAction(Context ctx, ClosedUserGroupTemplate cugTemplate, Home home, String key) throws Throwable
    {
        cugTemplate.enable(ctx);
        WebAgents.getWriter(ctx).println(
                "<font color=green>CUG Template " + key + " has been enabled.</font><br/><br/>");      
    }    
    
}
