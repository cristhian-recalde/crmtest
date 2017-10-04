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
package com.trilogy.app.crm.home.sub;

import java.util.Map;

import com.trilogy.app.crm.bean.DefaultSubTypeMsisdnGroup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;


/**
 * This home will auto-select a MSISDN for non-msisdn-aware subscription types
 * using the MSISDN group configured in the SPID.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class DefaultMsisdnSettingHome extends HomeProxy
{
    public DefaultMsisdnSettingHome(Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        if (obj instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) obj;
            if (sub.getMSISDN() == null
                    || sub.getMSISDN().trim().length() == 0
                    || com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl.DEFAULT_OPTIONAL_VALUE.equals(sub.getMSISDN()))
            {
                SubscriptionType subType = sub.getSubscriptionType(ctx);
                if (subType != null
                        && !subType.isMsisdnAware())
                {
                    CRMSpid spid = SpidSupport.getCRMSpid(ctx, ((SpidAware)obj).getSpid());
                    if (spid != null)
                    {
                        Integer defaultMsisdnGroupId = spid.getDefaultSubTypeMsisdnGroupId(subType.getId());
                        if (defaultMsisdnGroupId != null)
                        {
                            And filter = new And();
                            filter.add(new EQ(MsisdnXInfo.STATE, MsisdnStateEnum.AVAILABLE));
                            filter.add(new EQ(MsisdnXInfo.SUBSCRIBER_TYPE, sub.getSubscriberType()));
                            filter.add(new EQ(MsisdnXInfo.GROUP, defaultMsisdnGroupId));
                            Msisdn msisdn = HomeSupportHelper.get(ctx).findBean(ctx, Msisdn.class, filter);
                            if (msisdn == null)
                            {
                                throw new HomeException(
                                        "No " + sub.getSubscriberType() + " MSISDNs available in default MSISDN group " + defaultMsisdnGroupId
                                        + " for " + subType.getTypeEnum() + " subscription type " + subType.getId()
                                        + ".  Create a MSISDN in this group, then try creating this subscription again.");
                            }
                            
                            sub.setMSISDN(msisdn.getMsisdn());
                            new InfoLogMsg(this, String.format("First available MSISDN [%s] selected from SPID level [%d] default MSISDN Group [%d]", 
                                                    msisdn.getMsisdn(), spid.getId(), defaultMsisdnGroupId), null).log(ctx); 
                        }
                        else
                        {
                            throw new HomeException(
                                    "No default MSISDN group configured for " + subType.getTypeEnum() + " subscription type " + subType.getId()
                                    + ".  Add this configuration to the SPID, then try creating this subscription again.");
                        }
                    }
                }   
            }
        }
        return super.create(ctx, obj);
    }
}
