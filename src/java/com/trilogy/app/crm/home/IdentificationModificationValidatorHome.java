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

import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;

/**
 * Validated whether an Identification can be removed or have its SPID changed,
 * based on it being used by any Spid Identification Group.
 * 
 * @author marcio.marques@redknee.com
 * 
 */
public class IdentificationModificationValidatorHome extends HomeProxy
{
    
    public IdentificationModificationValidatorHome(Context ctx, Home delegate)
    {
       super(ctx, delegate);
    }
    
    public void remove(Context ctx, Object obj)
    throws HomeException
    {
        Identification id = (Identification) obj;

        Identification oldId = retrieveOldId(ctx, id);
        
        if (identificationInUse(ctx, id.getCode(), oldId.getSpid()))
        {
            throw new HomeException("Cannot remove Identification '" + id.getDesc() + "' because it is used by some identification rule.");
        }
        else
        {
            super.remove(ctx, obj);
        }
        
    }

     public Object store(Context ctx, Object obj)
     throws HomeException
    {
        Identification id = (Identification) obj;
        
        Identification oldId = retrieveOldId(ctx, id);
        
        if (spidHasChanged(ctx, id, oldId) && identificationInUse(ctx, id.getCode(), oldId.getSpid()))
        {
            throw new HomeException("Cannot update identification Spid because it is used by some identification rule.");
        }
        else
        {
            return super.store(ctx, obj);
        }
        
    }
     
     private Identification retrieveOldId(Context ctx, Identification newId) throws HomeException
     {
         Home home = (Home) ctx.get(IdentificationHome.class);
         final Identification id = (Identification) home.find(ctx, new EQ(IdentificationXInfo.CODE, Integer.valueOf(newId.getCode())));
         if (id != null)
         {
             return id;
         }
         else
         {
             throw new HomeException("Spid " + newId.getSpid() + " not found.");
         }
     }
     
    private boolean spidHasChanged(Context ctx, Identification newId, Identification oldId) throws HomeException
    {
       return newId.getSpid()!=oldId.getSpid();
    }
     
    private boolean identificationInUse(Context ctx, final int code, final int spid) throws HomeException
    {
        Home home = (Home) ctx.get(SpidIdentificationGroupsHome.class);
        home = home.where(ctx, new EQ(SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid)));
        home = home.where(ctx, new Predicate()
        {
            public boolean f(Context ctx, Object obj)
            {
               boolean result = false;
               SpidIdentificationGroups identificationGroups = (SpidIdentificationGroups) obj;
               
               Iterator<IdentificationGroup> iter = (Iterator<IdentificationGroup>) identificationGroups.getGroups().iterator();
               while (iter.hasNext())
               {
                   IdentificationGroup group = iter.next();
                   if (!group.isAcceptAny() && group.getIdentificationList().contains(String.valueOf(code)))
                   {
                       result = true;
                       break;
                   }
               }
               return result;
            }
        });
        return (home.selectAll().size()>0);
    }

}
