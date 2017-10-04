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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberStateEnum;

/**
 * Adapts Msisdn object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class MsisdnToMobileNumberReferenceAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        Msisdn msisdn = (Msisdn) obj;
        
        final MobileNumberReference reference = new MobileNumberReference();

        reference.setIdentifier(msisdn.getMsisdn());
        reference.setSpid(msisdn.getSpid());
        reference.setTechnology(TechnologyTypeEnum.valueOf(msisdn.getTechnology().getIndex()));
        reference.setPaidType(RmiApiSupport.convertCrmSubscriberPaidType2Api(msisdn.getSubscriberType()));
        reference.setGroupID(Long.valueOf(msisdn.getGroup()));
        reference.setState(MobileNumberStateEnum.valueOf(msisdn.getState().getIndex()));

        return reference;
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
