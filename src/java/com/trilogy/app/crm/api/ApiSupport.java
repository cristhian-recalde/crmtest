/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api;

import java.security.Permission;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.configshare.ConfigShareAPIUpdateEntityHome;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.app.crm.support.DefaultConfigChangeRequestSupport;

/**
 * For methods used in all APIs.
 *
 * @author victor.stratan@redknee.com
 */
public class ApiSupport
{

    public static Map<String, Set<String>> customAPIMethodFilter = new HashMap<String, Set<String>>();
    static
    {
        // createAccount
        Set<String> set = new HashSet<String>();
        set.add(com.redknee.app.crm.bean.account.Contact.class.getName());
        set.add( com.redknee.app.crm.bean.account.AccountIdentification.class.getName());
        set.add(com.redknee.app.crm.bean.account.SecurityQuestionAnswer.class.getName());
        set.add(com.redknee.app.crm.bean.account.SpidAwareAccountIdentification.class.getName());
        set.add(com.redknee.app.crm.bean.account.SpidAwareSecurityQuestionAnswer.class.getName());
        set.add(com.redknee.app.crm.bean.Account.class.getName());
        customAPIMethodFilter.put("createAccount", set);
        customAPIMethodFilter.put("deleteAccount", set);
        customAPIMethodFilter.put("updateAccountProfile", set);
        set.add(com.redknee.app.crm.bean.Subscriber.class.getName());
        customAPIMethodFilter.put("createIndividualSubscriber", set);
        Set<String> subList = new HashSet<String>();
        subList.add(com.redknee.app.crm.bean.Subscriber.class.getName());
        subList.add(com.redknee.app.crm.contract.SubscriptionContract.class.getName());
        customAPIMethodFilter.put("createSubscription", subList);
        customAPIMethodFilter.put("deleteSubscription", subList);
        customAPIMethodFilter.put("updateSubscriptionContractDetails", subList);
        customAPIMethodFilter.put("updateSubscriptionRating", subList);
    }
    
    /**
     * Authenticates a user. Passed in context is poluted with user specific settings. 
     *
     * @param ctx The operating context
     * @param login username
     * @param passwd password
     * @return true if login is successful
     */
    public static boolean authenticateUser(final Context ctx, final String login, final String passwd)
    {
        // TODO refactor with SubscriberProvisioner.authenticateUser()
        boolean result = true;
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, ApiSupport.class, " start authenticateUser()");
        }

        try
        {
            Session.setSession(ctx, ctx);
            final AuthSPI auth = (AuthSPI) ctx.get(AuthSPI.class);
            auth.login(ctx, login, passwd);
        }
        catch (LoginException le)
        {
            result = false;
        }
        return result;
    }

    /**
     * Checks authorizations for a user.
     *
     * @param ctx The operating context
     * @param permission permitioin to check on the user
     * @return true if permision check is successful
     */
    public static boolean authorizeUser(final Context ctx, final Permission permission)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, ApiSupport.class, " start hasPermission()");
        }
        
        return AuthSupport.hasPermission(ctx, permission);
    }


    /**
     * Injects ConfigShareAPIUpdateEntityHome into the home pipeline so that before, home can populate appropriate data to avoid
     * circular updates to DCRM 
     * @param ctx
     * @param key
     * @param home
     */
    public static Home injectAPIUpdateEntityHomeIntoPipeline(final Context ctx, final Home home)
    {
        final Home newHome = new ConfigShareAPIUpdateEntityHome(ctx, home);
        
        return newHome;
    }
    
    /**
     * Injects ConfigShareAPIUpdateEntityHome into the generic entity's home pipeline so that before, home can populate appropriate data to avoid
     * circular updates to DCRM
     * 
     * @param ctx
     * @param home
     * @param entityQualifiedClassName the generic entity class name
     * @return
     */
    public static Home injectAPIUpdateEntityHomeIntoGenericEntityPipeline(final Context ctx, final Home home, final String entityQualifiedClassName)
    {
        final Home newHome = new ConfigShareAPIUpdateEntityHome(ctx, home);
        
    	Set<String> set = new HashSet<String>();
    	set.add(entityQualifiedClassName);
        ctx.put(DefaultConfigChangeRequestSupport.FILTER_MULTIPLE_ENTITIES_FOR_API_UPDATE, set);
        
        return newHome;
    }
    
    /**
     * Injects ConfigShareAPIUpdateEntityHome into the home pipeline so that before, home can populate appropriate data to avoid
     * circular updates to DCRM 
     * @param ctx
     * @param key
     * @param home
     */
    public static Home injectAPIUpdateEntityHomeIntoPipeline(final Context ctx, final Home home, final String caller)
    {
        final Home newHome = new ConfigShareAPIUpdateEntityHome(ctx, home);
        
        Collection list = getCustomFilterList(ctx, caller);
        if (list != null )
        {
            ctx.put(DefaultConfigChangeRequestSupport.FILTER_MULTIPLE_ENTITIES_FOR_API_UPDATE, list);
        }
        return newHome;
    }
    

    private static Collection<String> getCustomFilterList(final Context ctx, final String caller)
    {
        Collection<String> result = customAPIMethodFilter.get(caller);
        return result;
    }

  
}
