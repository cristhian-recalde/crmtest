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

import com.trilogy.app.crm.extension.auxiliaryservice.DiscountAuxSvcExtensionWebControl;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for the Discount auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class CRMDiscountAuxSvcExtensionWebControl extends DiscountAuxSvcExtensionWebControl
{
    public WebControl getMinimumTotalChargeThresholdWebControl()
    {
        return MINIMUM_TOTAL_CHARGE_THRESHOLD_WC;
    }
    
    public static final WebControl MINIMUM_TOTAL_CHARGE_THRESHOLD_WC = new CurrencyContextSetupWebControl(new com.redknee.framework.core.web.XCurrencyWebControl(false));
}
