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
package com.trilogy.app.crm.home.calldetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.calldetail.CGPAPresentationRestrictedEnum;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Replaced the number and location of calldetail with "blocked"
 * if the privacy restriction indicator is set
 *
 * @author kason.wong@redknee.com
 */
public class CallDetailPrivacyRestrictionHome extends HomeProxy
{

    public CallDetailPrivacyRestrictionHome(final Home delegate)
    {
        super(delegate);
    }

    public static CallDetail censor(Context ctx, final CallDetail detail)
    {
        if (detail.getRestricted() == CGPAPresentationRestrictedEnum.RESTRICTED)
        {
            if (detail.getCallType() == CallTypeEnum.TERM)
            {
                detail.setOrigMSISDN(getBlockedFormat(ctx, detail));
                detail.setOrigPrefixDesc(getBlockedFormat(ctx, detail));
                detail.setCallingPartyLocation(getBlockedFormat(ctx, detail));
            }
        }

        return detail;
    }

    private Collection censor(Context ctx, final Collection col)
    {
        final Collection result = new ArrayList();
        final Iterator iter = col.iterator();
        for (; iter.hasNext();)
        {
            result.add(censor(ctx, (CallDetail) iter.next()));
        }
        return result;
    }

    public Object find(final Context ctx, final Object key)
        throws HomeException
    {
        final CallDetail detail = (CallDetail) super.find(ctx, key);
        return censor(ctx, detail);
    }

    public Collection select(final Context ctx, final Object what)
        throws HomeException
    {
        return censor(ctx, super.select(ctx, what));
    }

    private static String getBlockedFormat(Context ctx, CallDetail cd)
    {
        CRMSpid spid = null;
        try
        {
            spid = SpidSupport.getCRMSpid(ctx, cd.getSpid());
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, CallDetailPrivacyRestrictionHome.class.getName(), 
                    "Unable to retrieve SPID for Call Detail with ID : [ " + cd.getId() + "]," +
            		" for Account / BAN : [" + cd.getBAN() + "], Error Message : [" + e.getMessage() + "]" );
        }
        if(spid != null)
        {
            return spid.getRestrictedCallFormat();
        }
        
        return BLOCKED;
    }
    
    private static final String BLOCKED = "restricted";
}
