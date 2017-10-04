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

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * Validates that the group id corresponds to an existing Msisdn Group.
 * @author victor.stratan@redknee.com
 */
public final class MsisdnValidator implements Validator
{
    private static final MsisdnValidator INSTANCE = new MsisdnValidator();

    private static final String MSISDN_GROUP_NOT_PROVIDED = "MSISDN Group not provided.";
    private static final String MSISDN_GROUP_NOT_PROVIDED_KEY = "MSISDN_GROUP_NOT_PROVIDED";
    private static final String MSISDN_GROUP_NOT_FOUND = "MSISDN Group not found.";
    private static final String MSISDN_GROUP_NOT_FOUND_KEY = "MSISDN_GROUP_NOT_FOUND";
    private static final String MSISDN_GROUP_ERROR = "Error while retreiving MSISDN Group.";
    private static final String MSISDN_GROUP_ERROR_KEY = "MSISDN_GROUP_ERROR";

    private static final Map DEFAULTS = new HashMap();
    static
    {
        DEFAULTS.put(MSISDN_GROUP_NOT_PROVIDED_KEY, MSISDN_GROUP_NOT_PROVIDED);
        DEFAULTS.put(MSISDN_GROUP_NOT_FOUND_KEY, MSISDN_GROUP_NOT_FOUND);
        DEFAULTS.put(MSISDN_GROUP_ERROR_KEY, MSISDN_GROUP_ERROR);
    }

    private MsisdnValidator()
    {
    }

    public static MsisdnValidator instance()
    {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Msisdn msisdn = (Msisdn) obj;
        ensureState(ctx, msisdn);
       
        final Home grpHome = (Home) ctx.get(MsisdnGroupHome.class);
        final int msisdnGrpId = msisdn.getGroup();

        if (msisdnGrpId == Msisdn.DEFAULT_GROUP)
        {
            throwError(ctx, MsisdnXInfo.GROUP, MSISDN_GROUP_NOT_PROVIDED_KEY);
        }

        try
        {
            final EQ condition = new EQ(MsisdnGroupXInfo.ID, Integer.valueOf(msisdnGrpId));
            final MsisdnGroup msisdnGroup = (MsisdnGroup) grpHome.find(ctx, condition);
            if (msisdnGroup == null)
            {
                throwError(ctx, MsisdnXInfo.GROUP, MSISDN_GROUP_NOT_FOUND_KEY);
            }
        }
        catch (HomeException e)
        {
            throwError(ctx, MsisdnXInfo.GROUP, MSISDN_GROUP_ERROR_KEY, e);
        }
    }

    private void ensureState(Context ctx, Msisdn msisdn)throws IllegalStateException
    {
        try
        {
            Msisdn originalMsisdn = HomeSupportHelper.get(ctx).findBean(ctx,Msisdn.class, msisdn.ID());
            if(null !=originalMsisdn && originalMsisdn.getState() != msisdn.getState())
            {
                if( MsisdnStateEnum.IN_USE == originalMsisdn.getState())
                {
                    if(MsisdnStateEnum.AVAILABLE == msisdn.getState())
                    {
                        throw new IllegalStateException("MSISDN in-use cannot be made available. It must go to held state first");
                    }
                }
            }
        } 
        catch(IllegalStateException ie)
        {
            throw ie;
        }        
        catch (Throwable t)
        {
            throw new IllegalStateException("Error validating state of the MSISDN");
        }
        
    }

    private void throwError(final Context ctx, final PropertyInfo property, final String key)
    {
        throwError(ctx, property, key, null);
    }

    private void throwError(final Context ctx, final PropertyInfo property, final String key, final Exception cause)
    {
        final String msg = getErrorMessage(ctx, key);
        final CompoundIllegalStateException compoundException = new CompoundIllegalStateException();
        final IllegalPropertyArgumentException propertyException = new IllegalPropertyArgumentException(property, msg);
        if (cause != null)
        {
            propertyException.initCause(cause);
        }
        compoundException.thrown(propertyException);
        compoundException.throwAll();
    }

    private String getErrorMessage(final Context ctx, final String key)
    {
        final MessageMgr mgr = new MessageMgr(ctx, this);
        return mgr.get(key, (String) DEFAULTS.get(key));
    }
}
