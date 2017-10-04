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

import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.IdentificationGroupXInfo;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Validates the number of identification types in a spid identification groups
 * configuration.
 * 
 * @author marcio.marques@redknee.com
 * 
 */
public class SpidIdentificationGroupsListsValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        SpidIdentificationGroups identificationGroups = (SpidIdentificationGroups) obj;
        final CompoundIllegalStateException exception = 
            new CompoundIllegalStateException();
        Set<Integer> groupIdSet = new HashSet();
        Home identificationHome = (Home) ctx.get(IdentificationHome.class);

        try
        {
            boolean numIdsChecked = false;
            int numIds = 0;
            
            if (identificationGroups.getGroups().size()==0)
            {
                exception.thrown(new IllegalPropertyArgumentException(SpidIdentificationGroupsXInfo.GROUPS, "At least one group should be defined."));
            }
            
            Iterator<IdentificationGroup> iter = (Iterator<IdentificationGroup>) identificationGroups.getGroups().iterator();
            
            while (iter.hasNext())
            {
                IdentificationGroup group = iter.next();
                assertSingleInstance(group, groupIdSet, exception);
                if (group.isAcceptAny() && !numIdsChecked)
                {
                    numIds = identificationHome.where(ctx, new EQ(IdentificationXInfo.SPID, Integer.valueOf(identificationGroups.getSpid()))).selectAll().size();
                    numIdsChecked = true;
                }

                validateIdentificationGroup(group.getIdentificationList(), group.getRequiredNumber(), group.isAcceptAny(), numIds, IdentificationGroupXInfo.REQUIRED_NUMBER, exception);
            }
        }
        catch (HomeException e)
        {
            String msg = "Error verifying the number of IDs for SPID " + identificationGroups.getSpid();
            if (LogSupport.isDebugEnabled(ctx)) 
            {
                new DebugLogMsg(this, msg, e).log(ctx); 
            }

            final IllegalPropertyArgumentException newException =
                new IllegalPropertyArgumentException(IdentificationGroupXInfo.REQUIRED_NUMBER, msg);
            newException.initCause(e);
            exception.thrown(newException);
            exception.throwAll();
           
        }
        exception.throwAll();
        
    }
    
    private void assertSingleInstance(IdentificationGroup group, Set<Integer> groupIdSet, CompoundIllegalStateException exception)
    {
        if (groupIdSet.contains(Integer.valueOf(group.getIdGroup())))
        {
            exception.thrown(new IllegalPropertyArgumentException(SpidIdentificationGroupsXInfo.GROUPS, "Group " + group.getIdGroup() + " is defined more than once."));
        } 
        else
        {
            groupIdSet.add(Integer.valueOf(group.getIdGroup()));
        }           
    }

    private void validateIdentificationGroup(Set set, int requiredNumber, boolean any, int numIds, PropertyInfo propertyInfo, CompoundIllegalStateException exception)
    {
        if (requiredNumber<=0)
        {
            exception.thrown(new IllegalPropertyArgumentException(propertyInfo, "Required number of identifications must be greater then 0."));
        }
        else if ((!any && set.size()<requiredNumber) || (any && numIds<requiredNumber))
        {
            exception.thrown(new IllegalPropertyArgumentException(propertyInfo, "Required number of identifications cannot be greater than the number of available identifications."));
        }
        else if (!any && set.size()==0)
        {
            exception.thrown(new IllegalPropertyArgumentException(propertyInfo, "At least one identification should be defined for each identification group."));
        }
        
    }

}
