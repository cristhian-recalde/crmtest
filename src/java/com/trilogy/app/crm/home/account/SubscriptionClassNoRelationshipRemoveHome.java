package com.trilogy.app.crm.home.account;

import java.util.Collection;

import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.AccountRoleHome;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassRow;
import com.trilogy.app.crm.bean.account.SubscriptionClassRowXInfo;
import com.trilogy.app.crm.support.CollectionSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Upon a remove action, verify that the bean selected for removal is not referenced in 
 * existing AccountRoles entities.  Only those beans with no dependencies are allowed 
 * to be removed from the system.   
 * 
 * @author angie.li
 *
 */
public class SubscriptionClassNoRelationshipRemoveHome extends HomeProxy 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public SubscriptionClassNoRelationshipRemoveHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    public void remove(Context ctx, Object obj)
        throws HomeException
    {
        //Check if Account Roles are using this Subscription Class
        checkRelationship(ctx, obj);

        getDelegate().remove(obj);
    }

    /** Assert that none of the would-be matched parents have children before remove any. 
     * Resource: com.redknee.framework.xhome.relationship.NoRelationshipRemoveHome**/   
    public void removeAll(Context ctx, Object where)
        throws HomeException
    {
        if ( where == null )
        {
            throw new NullPointerException("Null not allowed in Home.removeAll()");
        }

        // FIXME this visitor will not serialize
        forEach(
                ctx,
                new Visitor()
                {
                    public void visit(Context ctx, Object bean)
                        throws AgentException, AbortVisitException
                    {
                        try
                        {
                            checkRelationship(ctx, bean);
                        }
                        catch (HomeException e)
                        {
                            throw new AgentException(e);
                        }
                    }
                },
                where);

        // No Children so continue with removal
        getDelegate().removeAll(ctx, where);
    }

    /**
     * Go through the list of SubscriptionClassesRow in Account Roles to find out if 
     * they reference the given Subscription Class
     * @param ctx  SubscriptionClass
     * @param obj
     * @throws HomeException
     */
    private void checkRelationship(Context ctx, Object obj)
        throws HomeException
    {
        final SubscriptionClass subscriptionClass = (SubscriptionClass) obj;
        Home accountRoleHome = (Home) ctx.get(AccountRoleHome.class);
        accountRoleHome.forEach(new Visitor()
        {
            public void visit(Context ctx, Object bean)
                throws AgentException, AbortVisitException
            {
                AccountRole role = (AccountRole) bean;
                Collection classes = role.getAllowedSubscriptionClass();
                SubscriptionClassRow row = (SubscriptionClassRow)CollectionSupportHelper.get(ctx).findFirst(ctx, 
                        classes,
                        new EQ(SubscriptionClassRowXInfo.SUBSCRIPTION_CLASS, subscriptionClass.getId()));
                if (row != null)
                {
                    MessageMgr mmgr = new MessageMgr(ctx, subscriptionClass.getClass());
                    throw new AgentException(new HomeException(mmgr.get(ERRMSG_KEY, ERRMSG)));
                }
            }
        });
    }

    public final static String ERRMSG_KEY = "NoSubscriptionClassRelationshipRemoveErrorMsg";
    public final static String ERRMSG = "This Subscription Class is in use.  Cannot delete this Subscription Class.";

}
