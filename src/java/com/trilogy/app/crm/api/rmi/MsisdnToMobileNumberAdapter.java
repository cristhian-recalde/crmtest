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
package com.trilogy.app.crm.api.rmi;

import java.util.Calendar;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyType;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumber;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberStateEnum;


/**
 * Adapts Msisdn object to API objects.
 * 
 * @author Marcio Marques
 * @since 9.2
 */
public class MsisdnToMobileNumberAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        Msisdn msisdn = (Msisdn) obj;
        final MobileNumber mobileNumber = new MobileNumber();
        mobileNumber.setAccountID(msisdn.getBAN());
        mobileNumber.setSubscriberID(msisdn.getSubscriberID(ctx));
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTime(msisdn.getLastModified());
        mobileNumber.setLastModified(lastModified);
        Calendar created = Calendar.getInstance();
        created.setTime(msisdn.getStartTimestamp());
        mobileNumber.setCreated(created);
        MsisdnGroup group = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.MsisdnGroup.class,
                new EQ(MsisdnGroupXInfo.ID, msisdn.getGroup()));
        if (group != null)
        {
            mobileNumber.setFeeAmount(group.getFee());
        }
        mobileNumber.setGroupID(Long.valueOf(msisdn.getGroup()));
        mobileNumber.setTechnology(TechnologyTypeEnum.valueOf(msisdn.getTechnology().getIndex()));
        mobileNumber.setState(MobileNumberStateEnum.valueOf(msisdn.getState().getIndex()));
        mobileNumber.setSpid(msisdn.getSpid());
        mobileNumber.setPaidType(RmiApiSupport.convertCrmSubscriberPaidType2Api(msisdn.getSubscriberType()));
        mobileNumber.setIdentifier(msisdn.getMsisdn());
        return mobileNumber;
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
