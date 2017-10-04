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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.support.AccountIdentificationSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Account border responsable by filling in the account identification spots
 * based on the spid identification groups configuration.
 * 
 * @author marcio.marques@redknee.com
 *
 */
public class AccountIdentificationGroupCustomWebControl extends ProxyWebControl
{
    public AccountIdentificationGroupCustomWebControl()
    {
        this((WebControl)XBeans.getInstanceOf(ContextLocator.locate(), Account.class, WebControl.class));
    }

    public AccountIdentificationGroupCustomWebControl(WebControl delegate)
    {
        setDelegate(delegate);
    }

    public void fromWeb(Context ctx, Object obj, javax.servlet.ServletRequest req, String name)
    {
        super.fromWeb(ctx, obj, req, name);
        Account act = (Account) obj;
        
        updateIdentificationGroupsInformation(ctx, act);                
    }
    

    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Account act = (Account) super.fromWeb(ctx, req, name);
        
        updateIdentificationGroupsInformation(ctx, act);                
        
        return act;
    }    

    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Account act = (Account) obj;

        List<AccountIdentificationGroup> idGroupList = act.getIdentificationGroupList();
        Iterator<AccountIdentificationGroup> i = idGroupList.iterator();
        while (i.hasNext())
        {
            AccountIdentificationGroup aig = i.next();
            List<AccountIdentification> identificationList = aig.getIdentificationList();
            if (identificationList.get(0).getIdType()==AccountIdentification.DEFAULT_IDTYPE && 
                    identificationList.get(identificationList.size()-1).getIdType()!=AccountIdentification.DEFAULT_IDTYPE)
            {
                while (identificationList.get(0).getIdType()==AccountIdentification.DEFAULT_IDTYPE)
                {
                    AccountIdentification ai = identificationList.get(0);
                    identificationList.remove(0);
                    identificationList.add(ai);
                }
            }
        }
        
        super.toWeb(ctx, out, name, obj);
    }
    
    private boolean groupsDiffer(Account account, SpidIdentificationGroups idGroups)
    {
        boolean result = false;
        if ((account.getIdentificationGroupList().size() == idGroups.getGroups().size())
            || (account.getIdentificationGroupList().size() == (idGroups.getGroups().size() + 1) && account.getIdentificationList(-1)!=null))
        {
            Set<Integer> idSet = new HashSet<Integer>();
            Iterator<IdentificationGroup> i = idGroups.getGroups().iterator();
            while (i.hasNext())
            {
                IdentificationGroup group = i.next();
                idSet.add(Integer.valueOf(group.getIdGroup()));
            }
            Iterator<AccountIdentificationGroup> j = account.getIdentificationGroupList().iterator();
            while (j.hasNext())
            {
                AccountIdentificationGroup group = j.next();
                if (group.getIdGroup()!=-1 && !idSet.contains(Integer.valueOf(group.getIdGroup())))
                {
                    result = true;
                    break;
                }
            }
        }
        else
        {
            result = true;
        }
        
        return result;
    }

    
    private void updateIdentificationGroupsInformation(Context ctx, Account act)
    {
        try
        {
            int spid = act.getSpid();
            Home home = (Home) ctx.get(SpidIdentificationGroupsHome.class);
            SpidIdentificationGroups idGroups = (SpidIdentificationGroups) home.find(new EQ(
                    SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid)));
            
            if (idGroups==null)
            {
                act.setIdentificationGroupList(new ArrayList());
            }
            else if (act.getIdentificationGroupList().size()==0 ||
                     groupsDiffer(act, idGroups))
            {
                List<AccountIdentification> list = act.getIdentificationList();
                AccountSupport.createEmptyAccountIdentificationGroupsList(ctx, act, idGroups);
                Iterator<AccountIdentification> i = list.iterator();
                
                while (i.hasNext())
                {
                    AccountIdentification ai = i.next();
                    AccountIdentificationSupport.addAccountIdentification(ctx, ai, act, idGroups);
                }
            }
            else
            {
                Map<Integer,AccountIdentificationGroup> aigGroups = new HashMap<Integer,AccountIdentificationGroup>();
                
                Iterator<AccountIdentificationGroup> i = act.getIdentificationGroupList().iterator();
                while (i.hasNext())
                {
                    AccountIdentificationGroup aig = i.next();
                    aigGroups.put(Integer.valueOf(aig.getIdGroup()), aig);
                }
                
                Iterator<IdentificationGroup> j = idGroups.getGroups().iterator();
                while (j.hasNext())
                {
                    IdentificationGroup ig = j.next();
                    if (aigGroups.get(Integer.valueOf(ig.getIdGroup()))!=null)
                    {
                        aigGroups.get(Integer.valueOf(ig.getIdGroup())).setGroup(ig.getName());
                    }
                }
                
                if (aigGroups.get(Integer.valueOf(AccountIdentificationGroup.DEFAULT_IDGROUP))!=null)
                {
                    AccountIdentificationGroup defaultGroup = aigGroups.get(Integer.valueOf(AccountIdentificationGroup.DEFAULT_IDGROUP));
                    if (defaultGroup.getIdentificationList().size()>0)
                    {
                        aigGroups.get(Integer.valueOf(AccountIdentificationGroup.DEFAULT_IDGROUP)).setGroup("--");
                    }
                    else
                    {
                        act.getIdentificationGroupList().remove(defaultGroup);
                    }
                }
            }
        }
        catch (Exception e)
        {
          if(LogSupport.isDebugEnabled(ctx))
          {
            new DebugLogMsg(this,e.getMessage(),e).log(ctx);
          }
        }    
    }
}
