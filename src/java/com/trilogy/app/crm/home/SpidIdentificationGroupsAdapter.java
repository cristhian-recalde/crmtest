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
package com.trilogy.app.crm.home;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Adapts a spid identification groups configuration (copying the set of
 * identification ids to a CSV string, and vice-versa).
 * 
 * @author marcio.marques@redknee.com
 * 
 */
public class SpidIdentificationGroupsAdapter implements Adapter
{
    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException {
        
        SpidIdentificationGroups identificationGroups = (SpidIdentificationGroups) obj;
        
        Iterator<IdentificationGroup> iter = (Iterator<IdentificationGroup>) identificationGroups.getGroups().iterator();
        
        
        while (iter.hasNext())
        {
            IdentificationGroup group = iter.next();
            group.setIdentificationList(buildSet(ctx, group.getIdentificationIdList(), group.isAcceptAny(), identificationGroups.getSpid()));
        }

        return identificationGroups;
    }
    
    private Set buildSet(Context ctx, String str, boolean any, int spid) throws HomeException
    {
        if (any)
        {
            final StringBuilder buff = new StringBuilder();
            
            Home home = (Home) ctx.get(IdentificationHome.class);
            home.where(ctx, new EQ(IdentificationXInfo.SPID, Integer.valueOf(spid))).forEach(new Visitor(){
                public void visit(Context ctx, Object obj)
                throws AgentException, AbortVisitException
                {
                    Identification id = (Identification) obj;
                    buff.append(",");
                    buff.append(id.getCode());
                }
            });

            // Removing first comma
            if (buff.length()>0)
            {
                buff.deleteCharAt(0);
            }
            str = buff.toString();
        }
        Set set = new HashSet();
        StringTokenizer st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
            set.add(st.nextToken());
        }
        return set;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException 
    {
        SpidIdentificationGroups identificationGroups = (SpidIdentificationGroups) obj;

        Iterator<IdentificationGroup> iter = (Iterator<IdentificationGroup>) identificationGroups.getGroups().iterator();
        
        while (iter.hasNext())
        {
            IdentificationGroup group = iter.next();
            group.setIdentificationIdList(buildString(group.getIdentificationList(), group.isAcceptAny()));
        }

        return identificationGroups;
    }

    private String buildString(Set set, boolean any)
    {
        if (any)
        {
            return "-1";
        }
        else
        {
            Object[] arr = set.toArray();
            StringBuilder buff = new StringBuilder();
            
            for (int x=0; x<arr.length; x++)
            {
                if (x!=0) buff.append(",");
                buff.append(arr[x]);
            }

            return buff.toString();
        }
    }
}
