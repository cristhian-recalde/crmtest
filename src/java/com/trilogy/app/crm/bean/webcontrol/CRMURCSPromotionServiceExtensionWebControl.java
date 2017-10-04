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

import com.trilogy.app.crm.extension.auxiliaryservice.URCSPromotionAuxSvcExtensionWebControl;
import com.trilogy.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtension;
import com.trilogy.app.crm.extension.service.URCSPromotionServiceExtensionWebControl;
import com.trilogy.app.crm.urcs.CustomServiceOptionKeyWebControl;
import com.trilogy.app.crm.urcs.CustomSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.FinalWebControl;
import com.trilogy.framework.xhome.webcontrol.HiddenWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for the URCS Promotion auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class CRMURCSPromotionServiceExtensionWebControl extends URCSPromotionServiceExtensionWebControl
{
    public WebControl getServiceOptionWebControl()
    {
        return serviceOption_wc;
    }

    public static final WebControl serviceOption_wc = new HiddenWebControl(
                                                        new FinalWebControl(
                                                            new CustomSpidProxyWebControl(
                                                            new com.redknee.framework.xhome.msp.SetSpidProxyWebControl(
                                                                    new CustomServiceOptionKeyWebControl(
                                                                            1,
                                                                            false,
                                                                            new KeyWebControlOptionalValue(
                                                                                    "--",
                                                                                    Long.valueOf(URCSPromotionAuxSvcExtension.DEFAULT_SERVICEOPTION)))))));
}
