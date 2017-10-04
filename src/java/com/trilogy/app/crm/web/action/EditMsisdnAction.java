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
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.web.action.EditAction;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
/**
 * @author jke
 */
public class EditMsisdnAction extends EditAction
{
    public boolean isEnabled(Context context, Object obj)
    {
        Msisdn msisdn = (Msisdn)obj;
        boolean f = (MsisdnStateEnum.IN_USE == msisdn.getState()) || (PortingTypeEnum.NONE !=  msisdn.getPortingType() );
        return !f;
    }
}
