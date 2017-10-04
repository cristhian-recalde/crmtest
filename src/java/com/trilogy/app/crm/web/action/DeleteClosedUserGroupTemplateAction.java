package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.ff.FFClosedUserGroupSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;

/**
 * Action responsible for deleting a closed user group template.
 * @author marcio.marques@redknee.com
 *
 */
public class DeleteClosedUserGroupTemplateAction extends ClosedUserGroupTemplateAction
{
    /**
     * Creates an empty bean.
     */
    public DeleteClosedUserGroupTemplateAction()
    {
        super("delete", "Delete");
        defaultHelpText_ = "Delete the current bean.";        
    }
    
    /**
     * Creates an empty bean with the permission set.
     * @param permission Permission.
     */
    public DeleteClosedUserGroupTemplateAction(final Permission permission)
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
       writeLink(ctx, out, bean, link, "deletion");
    }

    
    /**
     * {@inheritDoc}
     */
    protected void executeAction(Context ctx, ClosedUserGroupTemplate cugTemplate, Home home, String key) throws Throwable
    {
        if (!FFClosedUserGroupSupport.isCUGTemplateInUse(ctx, cugTemplate))
        {
            home.remove(ctx, cugTemplate);
            WebAgents.getWriter(ctx).println(
                "<font color=green>CUG Template " + key + " has been removed.</font><br/><br/>");
        }
        else
        {
            throw new IllegalStateException("CUG Template in use by CUG instances.");
        }        
    }
 
}
