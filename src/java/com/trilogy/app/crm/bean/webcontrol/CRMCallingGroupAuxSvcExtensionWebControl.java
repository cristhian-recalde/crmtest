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
package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.extension.auxiliaryservice.CallingGroupAuxSvcExtensionWebControl;
import com.trilogy.app.crm.web.control.CallingGroupLinkedWebControl;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for the Calling Group auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class CRMCallingGroupAuxSvcExtensionWebControl extends CallingGroupAuxSvcExtensionWebControl
{
    public WebControl getCallingGroupWebControl()
    {
        return callingGroup_wc;
    }


    public WebControl getServiceChargePostpaidWebControl()
    {
        return serviceChargePostpaid_wc;
    }


    public WebControl getServiceChargePrepaidWebControl()
    {
        return serviceChargePrepaid_wc;
    }


    public WebControl getServiceChargeExternalWebControl()
    {
        return serviceChargeExternal_wc;
    }

    public static final WebControl callingGroup_wc = CallingGroupLinkedWebControl.instance();
    public static final WebControl serviceChargePostpaid_wc = new CurrencyContextSetupWebControl(new com.redknee.framework.core.web.XCurrencyWebControl(false));
    public static final WebControl serviceChargePrepaid_wc = new CurrencyContextSetupWebControl(new com.redknee.framework.core.web.XCurrencyWebControl(false));
    public static final WebControl serviceChargeExternal_wc = new CurrencyContextSetupWebControl(new com.redknee.framework.core.web.XCurrencyWebControl(false));
}
