package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;

import com.trilogy.app.crm.bean.AccountCreationTemplateHome;
import com.trilogy.app.crm.bean.ClosedUserGroupTemplate;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionXInfo;
import com.trilogy.app.crm.filter.ExtensionsEQPredicate;


/**
 * Action responsible for disabling a closed user group template.
 * @author marcio.marques@redknee.com
 *
 */
public class DisableClosedUserGroupTemplateAction extends ClosedUserGroupTemplateAction
{
    /**
     * Creates an empty bean.
     */
    public DisableClosedUserGroupTemplateAction()
    {
        super("disable", "Disable");
        defaultHelpText_ = "Disable the current bean.";            
    }
    
    /**
     * Creates an empty bean with the permission set.
     * @param permission Permission.
     */
    public DisableClosedUserGroupTemplateAction(final Permission permission)
    {
        this();
        setPermission(permission);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(Context ctx, Object bean)
    {
        if (bean == null)
        {
            return false;
        }
        else if (bean instanceof ClosedUserGroupTemplate)
        {
            return !((ClosedUserGroupTemplate) bean).isDeprecated();
        }
        else
        {
            return false;
        }
    }  
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLink(Context ctx, PrintWriter out, Object bean, Link link)
    {
       writeLink(ctx, out, bean, link, "disabling");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeAction(Context ctx, ClosedUserGroupTemplate cugTemplate, Home home, String key) throws Throwable
    {
        Home actHome = (Home) ctx.get(AccountCreationTemplateHome.class);
        Predicate predicate = 
            new ExtensionsEQPredicate(FriendsAndFamilyExtension.class,
                    FriendsAndFamilyExtensionXInfo.CUG_TEMPLATE_ID, Long.valueOf(cugTemplate.getID()));

        int templateInUse = actHome.select(ctx, predicate).size();

        if (templateInUse==0)
        {
            cugTemplate.disable(ctx);
            WebAgents.getWriter(ctx).println(
                    "<font color=green>CUG Template " + key + " has been disabled.</font><br/><br/>");
        } 
        else
        {
            throw new IllegalStateException("CUG Template in use by " + templateInUse + " Account Creation Templates.");
        }    
    }    
}
