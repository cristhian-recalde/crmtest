/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.extension.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.web.control.AccountExtensionReadOnlySupportedWebControl;
import com.trilogy.app.crm.web.control.SimpleHelpRenderer;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.entity.EntityInfo;
import com.trilogy.framework.xhome.entity.EntityInfoHome;
import com.trilogy.framework.xhome.entity.EntityInfoXInfo;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.web.renderer.HelpRenderer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 
 */
public class AccountExtensionSupport
{
    public static final String ACCOUNT_EXTENSION_PARENT_BEAN_CTX_KEY = "Account.Extension.ParentBean";
    
    public static final String ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY = "Account.RegisteredExtensions";
    
    private static final String PPSM_MSISDN_EXTENSION_NAME = "PPSM MSISDN";
    private static final String PPSM_MSISDN_EXTENSION_DESC = "";
    
    private static final String SUBSCRIBER_LIMIT_EXTENSION_NAME = "Subscriber Limit";
    private static final String SUBSCRIBER_LIMIT_EXTENSION_DESC = "";
    
    private static final String FRIENDS_N_FAMILY_EXTENSION_NAME = "Friends And Family";
    private static final String FRIENDS_N_FAMILY_EXTENSION_DESC = "";
    
    private static final String GROUP_PRICE_PLAN_EXTENSION_NAME = "Group Price Plan";
    private static final String GROUP_PRICE_PLAN_EXTENSION_DESC = "";

    private static final String POOL_EXTENSION_NAME = "Pool";
    private static final String POOL_EXTENSION_DESC = "";
    
    
    public static void registerExtension(Context ctx, Class extensionCls)
    {        
        registerExtension(ctx, extensionCls, XBeans.getClass(ctx, extensionCls, Home.class));
    }
    
    public static void registerExtension(Context ctx, Class extensionCls, Object homeKey)
    {
        Map<Class, Object> extensionHomeMap = (Map<Class, Object>)ctx.get(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY);
        if( extensionHomeMap == null )
        {
            extensionHomeMap = new HashMap<Class, Object>();
            ctx.put(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY, extensionHomeMap);
        }
        
        if( extensionCls == null || !Extension.class.isAssignableFrom(extensionCls) )
        {
            return;
        }
        
        if( extensionHomeMap.get(extensionCls) == null )
        {
            FacetMgr fmgr = (FacetMgr)ctx.get(FacetMgr.class);
            if( fmgr != null )
            {
                // Install the read-only supported web control so that it all works in the Account GUI
                fmgr.register(ctx, extensionCls, WebControl.class, new AccountExtensionReadOnlySupportedWebControl(
                        (WebControl)XBeans.getInstanceOf(ContextLocator.locate(), extensionCls, WebControl.class)));
                
                // Update the help text.  This is no longer neaded as of FW revision 5415.
                MessageMgr    mmgr     = new MessageMgr(ctx, AccountExtensionSupport.class);
                if( fmgr != null )
                {
                    final Context helpTextCtx = ctx.createSubContext();
                    helpTextCtx.put(HelpRenderer.class, SimpleHelpRenderer.instance());
                }
            }
        }
        
        extensionHomeMap.put(extensionCls, homeKey);
    }
    
    public static Object getExtensionHomeKey(Context ctx, Class extension)
    {
        Map<Class, Object> extensionHomeMap = (Map<Class, Object>)ctx.get(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY);
        if( extensionHomeMap == null )
        {
            extensionHomeMap = new HashMap<Class, Object>();
            ctx.put(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY, extensionHomeMap);
        }
        
        return extensionHomeMap.get(extension);
    }
    
    public static Collection getAllExtensionHomeKeys(Context ctx)
    {
        Map<Class, Object> extensionHomeMap = (Map<Class, Object>)ctx.get(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY);
        if( extensionHomeMap == null )
        {
            extensionHomeMap = new HashMap<Class, Object>();
            ctx.put(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY, extensionHomeMap);
        }
        
        return extensionHomeMap.values();
    }
    
    public static boolean isExtensionRegistered(Context ctx, Class extensionCls)
    {
        if( extensionCls == null || !Extension.class.isAssignableFrom(extensionCls) )
        {
            return false;
        }
        Set<Class> registeredExtensions = getRegisteredExtensions(ctx);
        if( registeredExtensions != null )
        {
            return registeredExtensions.contains(extensionCls);
        }
        return false;
    }
    
    public static Set<Class> getRegisteredExtensions(Context ctx)
    {
        Map<Class, Object> extensionHomeMap = (Map<Class, Object>)ctx.get(ACCOUNT_EXTENSION_HOME_KEYS_CTX_KEY);
        if( extensionHomeMap == null )
        {
            return new HashSet<Class>();
        }
        return extensionHomeMap.keySet();
    }
    
    public static String getExtensionName(Context ctx, Class cls)
    {
        if( !isExtensionClass(cls) )
        {
            return "N/A";
        }
        
        MessageMgr msgs = new MessageMgr(ctx, cls);
        
        String name = msgs.get(cls.getName() + ".extensionName");
        
        if( name == null )
        {
            Home entityHome = (Home)ctx.get(EntityInfoHome.class);
            if( entityHome != null )
            {
                try
                {
                    EntityInfo info = (EntityInfo)entityHome.find(ctx, new EQ(EntityInfoXInfo.CLASS_NAME, cls.getName()));
                    if( info != null )
                    {
                        name = info.getName();
                    }
                }
                catch (HomeException e)
                {
                    new DebugLogMsg(AccountExtensionSupport.class,"Unable to find the extension name", e).log(ctx);
                }
            }
        }
        
        if( name == null )
        {
            if( SubscriberLimitExtension.class.getName().equals(cls.getName())) 
            {
                name = SUBSCRIBER_LIMIT_EXTENSION_NAME;   
            }
            else if( FriendsAndFamilyExtension.class.getName().equals(cls.getName())) 
            {
                name = FRIENDS_N_FAMILY_EXTENSION_NAME;   
            }
            else if( GroupPricePlanExtension.class.getName().equals(cls.getName())) 
            {
                name = GROUP_PRICE_PLAN_EXTENSION_NAME;   
            }else if( PoolExtension.class.getName().equals(cls.getName())) 
            {
                name = POOL_EXTENSION_NAME;   
            }
        }
        
        if( name == null )
        {
            // Insert spaces into camel-case class name and remove 'Extension' at end of class name by default
            StringBuilder titleCaseName = new StringBuilder(cls.getSimpleName());
            titleCaseName.replace(0, 1, String.valueOf(Character.toUpperCase(titleCaseName.substring(0, 1).charAt(0))));
            String extensionSuffix = "Extension";
            if( extensionSuffix.equalsIgnoreCase(titleCaseName.substring(titleCaseName.length()-extensionSuffix.length(), titleCaseName.length())) )
            {
                titleCaseName.delete(titleCaseName.length()-extensionSuffix.length(), titleCaseName.length());
            }
            
            StringBuilder buf = new StringBuilder();
            for (char c : titleCaseName.toString().toCharArray()) {
                if (buf.length() > 0 && Character.isUpperCase(c)) {
                    buf.append(' ');
                }
                buf.append(c);
            }
            
            name = buf.toString();
        }
        
        return name;
    }

    public static String getExtensionDescription(Context ctx, Class cls)
    {
        MessageMgr msgs = new MessageMgr(ctx, cls);
        
        String name = msgs.get(cls.getName() + ".extensionDescription");
        if( name == null )
        {
            if( SubscriberLimitExtension.class.getName().equals(cls.getName())) 
            {
                name = SUBSCRIBER_LIMIT_EXTENSION_DESC;   
            }
            else if( FriendsAndFamilyExtension.class.getName().equals(cls.getName())) 
            {
                name = FRIENDS_N_FAMILY_EXTENSION_DESC;   
            }
            else if( GroupPricePlanExtension.class.getName().equals(cls.getName())) 
            {
                name = GROUP_PRICE_PLAN_EXTENSION_DESC;   
            }
            else
            {
                name = "";
            }
        }
        
        return name;
    }


    /**
     * Get a map of the existing extension IDs for each of an account's extensions.
     * 
     * @param ctx The operating context
     * @param ban The BAN of the account for which to query the current list of extensions
     * @return Extension class to list of existing extensions for the given BAN 
     */
    public static Map<Class, List<AccountExtension>> getExistingExtensionMap(Context ctx, String ban)
    {
        List<AccountExtension> oldExtensions = getExtensions(ctx, ban);
        Map<Class, List<AccountExtension>> oldExtensionMap = new HashMap<Class, List<AccountExtension>>();
        for( AccountExtension extension : oldExtensions )
        {
            List<AccountExtension> extensionList = oldExtensionMap.get(extension.getClass());
            if( extensionList == null )
            {
                extensionList = new ArrayList<AccountExtension>();
                oldExtensionMap.put(extension.getClass(), extensionList);
            }
            extensionList.add(extension);
        }
        return oldExtensionMap;
    }

    /**
     * Get a list of AccountExtensionHolder objects containing the all of the account's extensions currently in the system.
     * 
     * @param ctx The operating context
     * @param ban The BAN of the account for which to query the current list of extension IDs
     * @return List of AccountExtensionHolder objects containing the all of the account's extensions currently in the system.
     */
    public static List<AccountExtension> getExtensions(final Context ctx, String ban)
    {
        List<AccountExtension> extensions = new ArrayList<AccountExtension>();
        
        Set<Class> extensionTypes = getRegisteredExtensions(ctx);
        for( Class extensionCls : extensionTypes )
        {
            extensions.addAll((List<AccountExtension>)getExtensions(ctx, extensionCls, ban));
        }
        return extensions;
    }
    
    public static <T extends AccountExtension> List<T> getExtensions(final Context ctx, Class extensionType, String ban)
    {
        List<T> extensions = new ArrayList<T>();
        
        Home home = getExtensionHome(ctx, extensionType);
        try
        {
            Collection c = home.select(ctx, new EQ(AccountExtensionXInfo.BAN, ban));
            if( c != null )
            {
                extensions.addAll(c);   
            }

        }
        catch (UnsupportedOperationException e)
        {
            new MajorLogMsg(AccountExtensionSupport.class, "Error loading " + getExtensionName(ctx, extensionType) + " extension for account with BAN " + ban + ". See debug logs for details.", null).log(ctx);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(AccountExtensionSupport.class, "Error loading account extensions for account with BAN " + ban + ". See debug logs for details.", null).log(ctx);
        }
        
        return extensions;
    }


    /**
     * Get the appropriate Home from the Context for the given extension.
     * 
     * @param ctx The operating context
     * @param extension Extension for which we need a Home
     * @return The Extension's Home
     */
    public static Home getExtensionHome(Context ctx, Extension extension)
    {
        if( extension != null )
        {
            return getExtensionHome(ctx, extension.getClass());
        }
        return null;
    }

    /**
     * Get the appropriate Home from the Context for the given extension type.
     * 
     * @param ctx The operating context
     * @param extensionClass Type of Extension for which we need a Home
     * @return The Extension's Home
     */
    public static Home getExtensionHome(Context ctx, Class extensionClass)
    {
        return (Home)ctx.get(AccountExtensionSupport.getExtensionHomeKey(ctx, extensionClass));
    }

    /**
     * Get a collection of all Homes for all registerd extensions.
     * 
     * @param ctx The operating context
     * @return All Extension Homes
     */
    public static Collection<Home> getExtensionHomes(Context ctx)
    {
        Collection<Home> homes = new ArrayList<Home>();
        
        Collection homeKeys = AccountExtensionSupport.getAllExtensionHomeKeys(ctx);
        for( Object homeKey : homeKeys )
        {
            Home home = (Home)ctx.get(homeKey);
            if( home != null )
            {
                homes.add(home);   
            }
        }
        
        return homes;
    }
    
    public static Account getAccount(Context ctx, AccountExtension ext)
    {
        if( AbstractAccountExtension.DEFAULT_BAN.equals(ext.getBAN()) )
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(AccountExtensionSupport.class, "Extension ban cannot be same as the default ban", null).log(ctx);
            }

            return null;
        }
        
        Account account = null;
        
        Object parentBean = ctx.get(AccountExtensionSupport.ACCOUNT_EXTENSION_PARENT_BEAN_CTX_KEY);
        if( parentBean instanceof Account )
        {
            account = (Account)parentBean;
            if( account != null && SafetyUtil.safeEquals(account.getBAN(), ext.getBAN()) )
            {
                return account;
            }   
        }
        
        account = (Account)ctx.get(Account.class);
        if( account != null && SafetyUtil.safeEquals(account.getBAN(), ext.getBAN()) )
        {
            return account;
        }
        
        try
        {
            account = AccountSupport.getAccount(ctx, ext.getBAN());
        }
        catch (HomeException e)
        {
            new DebugLogMsg(AccountExtensionSupport.class, "Not able to find the account with the ban : " + ext.getBAN(), e).log(ctx);
        } 
        
        if( account != null && SafetyUtil.safeEquals(account.getBAN(), ext.getBAN()) )
        {
            return account;
        }
        
        return null;
    }
    
    private static boolean isExtensionClass(Class cls)
    {
        Class[] interfaces = cls.getInterfaces();
        if( interfaces != null && interfaces.length > 0 )
        {
            for( Class ifc : interfaces )
            {
                if( Extension.class.getName().equals(ifc.getName()) )
                {
                    return true;
                }
            }
        }
        
        Class superclass = cls.getSuperclass();
        if( superclass != null )
        {
            return isExtensionClass(superclass);
        }
        
        return false;
    }
}
