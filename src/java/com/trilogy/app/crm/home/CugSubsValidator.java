/*
 * This code is a protected work and subject 0to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ClosedSub;
import com.trilogy.app.crm.bean.ClosedSubXInfo;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.home.cug.DeltaNewOldCugSubs;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;


/**
 * Validates ClosedUserGroup Subscriber
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class CugSubsValidator
{

    /**
     * To be used when the CUG is being updated i.e, not on a new CUG.
     * 
     * @param cug
     */
    public CugSubsValidator(ClosedUserGroup cug, DeltaNewOldCugSubs deltaNewOldCugSubs)
    {
        delteNewOldClosedSubs_ = deltaNewOldCugSubs;
        cug_ = cug;
    }


    /**
     * To be used when CUG is being Added i.e, not updating an existing one.
     * 
     * @param cug
     */
    public CugSubsValidator(ClosedUserGroup cug)
    {
        delteNewOldClosedSubs_ = new DeltaNewOldCugSubs(cug, new ClosedUserGroup());
        cug_ = cug;
    }


    /**
     * Prefer {@CugSubsValidator(ClosedUserGroup cug,
     * DeltaNewOldCugSubs deltaNewOldCugSubs)} To be used when the CUG is being updated
     * i.e, not on a new CUG; and the detla is not available.
     * 
     * @param newCug
     * @param oldCug
     */
    public CugSubsValidator(ClosedUserGroup newCug, ClosedUserGroup oldCug)
    {
        delteNewOldClosedSubs_ = new DeltaNewOldCugSubs(newCug, oldCug);
        cug_ = newCug;
    }


    /**
     * Validate all Cases
     * 
     * @param ctx
     * @throws IllegalStateException
     */
    public void validateAllCases(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException exceptions;
        {
            exceptions = new CompoundIllegalStateException();
            retrieveExceptionsOnModifications(ctx, exceptions);
            retrieveExceptionsOnStructure(ctx, exceptions);
        }
        exceptions.throwAll();
    }


    /**
     * Validates, Pattern, Resctrictions which only apply to new and updated entires The
     * existing entires (same both new and old cug) as by requirement will be ignored
     * 
     * @param ctx
     * @throws IllegalStateException
     */
    public void validateModifications(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException exceptions;
        {
            exceptions = new CompoundIllegalStateException();
            retrieveExceptionsOnModifications(ctx, exceptions);
        }
        exceptions.throwAll();
    }


    /**
     * Validates Structure which only apply to all except the one's that will be removed
     * 
     * @param ctx
     * @throws IllegalStateException
     */
    public void validateStructure(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException exceptions;
        {
            exceptions = new CompoundIllegalStateException();
            retrieveExceptionsOnStructure(ctx, exceptions);
        }
        exceptions.throwAll();
    }


    private ExceptionListener retrieveExceptionsOnModifications(Context ctx, ExceptionListener exceptions)
    {
        final Map<String, ClosedSub> closedSubs = new HashMap<String, ClosedSub>();
        closedSubs.putAll(delteNewOldClosedSubs_.getsubsToBeAdded());
        closedSubs.putAll(delteNewOldClosedSubs_.getsubsToBeUpdated());
        final ClosedUserGroup cug = cug_;
        Map<String, ClosedSub> invalidSubs = new HashMap<String, ClosedSub>();
        try
        {
            CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, cug.getSpid());
            final String shortCodePattern = cug.getShortCodePattern(ctx);
            String dailingPattern = crmSpid.getDialingPattern();
            @SuppressWarnings("unchecked")
            Set<String> restrictedCodes = crmSpid.getRestrictedCodes().keySet();
            for (Map.Entry<String, ClosedSub> closedSubEntry : (closedSubs).entrySet())
            {
                final String phone = closedSubEntry.getKey();
                if (null != phone && !phone.isEmpty())
                {
                    if (null != dailingPattern && !dailingPattern.isEmpty() && !phone.matches(dailingPattern))
                    {
                        invalidSubs.put(closedSubEntry.getKey(), closedSubEntry.getValue());
                        exceptions.thrown(new IllegalStateException("Phone [" + phone
                                + "] does not match expected pattern [" + dailingPattern + "]. "
                                + getEntryString(ctx, closedSubEntry.getValue())));
                    }
                }
                else
                {
                    invalidSubs.put(closedSubEntry.getKey(), closedSubEntry.getValue());
                    exceptions.thrown(new IllegalStateException("Phone cannot be blank"));
                }
                final String shortCode = closedSubEntry.getValue().getShortCode();
                if (null != shortCode && !shortCode.isEmpty())
                {
                    if (null != shortCodePattern && !shortCodePattern.isEmpty() && !shortCode.matches(shortCodePattern))
                    {
                        invalidSubs.put(closedSubEntry.getKey(), closedSubEntry.getValue());
                        exceptions.thrown(new IllegalStateException("Short-Code [" + shortCode
                                + "] does not match expected pattern [" + shortCodePattern + "]. "
                                + getEntryString(ctx, closedSubEntry.getValue())));
                    }
                    if (restrictedCodes.contains(shortCode))
                    {
                        invalidSubs.put(closedSubEntry.getKey(), closedSubEntry.getValue());
                        exceptions.thrown(new IllegalStateException("Short-Code [" + shortCode
                                + "] is restricted at SPID level. " + getEntryString(ctx, closedSubEntry.getValue())));
                    }
                }
            }
        }
        catch (Throwable t)
        {
            exceptions.thrown(t);
        }
        return exceptions;
    }


    private ExceptionListener retrieveExceptionsOnStructure(Context ctx, ExceptionListener exceptions)
    {
        try
        {
            @SuppressWarnings("unchecked")
            final Map<String, ClosedSub> closedSubs = new HashMap<String, ClosedSub>();
            closedSubs.putAll(delteNewOldClosedSubs_.getsubsToBeAdded());
            closedSubs.putAll(delteNewOldClosedSubs_.getsubsToBeUpdated());
            closedSubs.putAll(delteNewOldClosedSubs_.getSubsToBeSame());
            final Map<String, ClosedSub> uniqueShortCodes = new HashMap<String, ClosedSub>();
            for (Map.Entry<String, ClosedSub> closedSubEntry : (closedSubs).entrySet())
            {
                final ClosedSub closedSub = closedSubEntry.getValue();
                final String shortCode = closedSub.getShortCode();
                if (null != shortCode && !shortCode.isEmpty())
                {
                    ClosedSub alreadyExisting = uniqueShortCodes.get(shortCode);
                    if (null != alreadyExisting)
                    {
                        exceptions.thrown(new IllegalStateException("Short-Code [" + shortCode
                                + "] duplicate for Phones [" + closedSub.getPhoneID() + " , "
                                + alreadyExisting.getPhoneID() + "]. " + getEntryString(ctx, closedSub)));
                    }
                    else
                    {
                        uniqueShortCodes.put(shortCode, closedSub);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            exceptions.thrown(t);
        }
        return exceptions;
    }


    private String getEntryString(Context ctx, ClosedSub entry)
    {
        return new StringBuilder("{ Entry# ").append(ClosedSubXInfo.PHONE_ID.getLabel(ctx)).append(": ")
                .append(entry.getPhoneID()).append(", ").append(ClosedSubXInfo.SHORT_CODE.getLabel(ctx)).append(": ")
                .append(entry.getShortCode()).append(" }").toString();
    }

    final DeltaNewOldCugSubs delteNewOldClosedSubs_;
    final ClosedUserGroup cug_;
}
