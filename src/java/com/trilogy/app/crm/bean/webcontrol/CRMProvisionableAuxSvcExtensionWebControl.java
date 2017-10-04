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

import com.trilogy.app.crm.extension.auxiliaryservice.ProvisionableAuxSvcExtensionWebControl;
import com.trilogy.app.crm.extension.auxiliaryservice.core.ProvisionableAuxSvcExtension;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for the Provisionable auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class CRMProvisionableAuxSvcExtensionWebControl extends ProvisionableAuxSvcExtensionWebControl
{
    public WebControl getPostpaidProvCmdWebControl()
    {
        return postpaidProvCmd_wc;
    }


    public WebControl getPostpaidUnProvCmdWebControl()
    {
        return postpaidUnProvCmd_wc;
    }


    public WebControl getPrepaidProvCmdWebControl()
    {
        return prepaidProvCmd_wc;
    }


    public WebControl getPrepaidUnProvCmdWebControl()
    {
        return prepaidUnProvCmd_wc;
    }

    public static final WebControl postpaidProvCmd_wc   = new com.redknee.app.crm.web.control.NullHandlingTextAreaWebControl()
                                                                .setMax(ProvisionableAuxSvcExtension.POSTPAIDPROVCMD_WIDTH);
    public static final WebControl postpaidUnProvCmd_wc = new com.redknee.app.crm.web.control.NullHandlingTextAreaWebControl()
                                                                .setMax(ProvisionableAuxSvcExtension.POSTPAIDUNPROVCMD_WIDTH);
    public static final WebControl prepaidProvCmd_wc    = new com.redknee.app.crm.web.control.NullHandlingTextAreaWebControl()
                                                                .setMax(ProvisionableAuxSvcExtension.PREPAIDPROVCMD_WIDTH);
    public static final WebControl prepaidUnProvCmd_wc  = new com.redknee.app.crm.web.control.NullHandlingTextAreaWebControl()
                                                                .setMax(ProvisionableAuxSvcExtension.PREPAIDUNPROVCMD_WIDTH);
}
