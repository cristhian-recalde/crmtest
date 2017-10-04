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

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.PricePlanFunctionEnum;
import com.trilogy.app.crm.bean.PricePlanKeyWebControl;
import com.trilogy.app.crm.extension.auxiliaryservice.VPNAuxSvcExtensionWebControl;
import com.trilogy.app.crm.web.control.CustomizedPricePlanKeyWebControl;
import com.trilogy.app.crm.web.control.TechnologyAwareFilterHomeWebControl;
import com.trilogy.framework.xhome.webcontrol.FinalWebControl;
import com.trilogy.framework.xhome.webcontrol.HiddenWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for the VPN auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class CRMVPNAuxSvcExtensionWebControl extends VPNAuxSvcExtensionWebControl
{
    public WebControl getVpnPricePlanWebControl() { return VPN_PRICE_PLAN; }
    
    public static final WebControl VPN_PRICE_PLAN        = new HiddenWebControl(
                                                              new FinalWebControl(
                                                                  new TechnologyAwareFilterHomeWebControl(
                                                                          new CustomizedPricePlanKeyWebControl(
                                                                                  new PricePlanKeyWebControl(
                                                                                          1,
                                                                                          false,
                                                                                          new KeyWebControlOptionalValue(
                                                                                                  "--",
                                                                                                  Long.valueOf(AbstractSubscriber.DEFAULT_PRICEPLAN))))
                                                                                  .setPricePlanFunction(PricePlanFunctionEnum.VPN))));
}
