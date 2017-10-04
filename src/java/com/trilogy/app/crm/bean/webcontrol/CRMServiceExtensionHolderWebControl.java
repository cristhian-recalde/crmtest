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

import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.extension.ExtensionFilterPredicate;
import com.trilogy.app.crm.extension.RegisteredExtensionPredicate;
import com.trilogy.app.crm.extension.service.ServiceExtensionHolderWebControl;
import com.trilogy.app.crm.extension.service.ServiceExtensionXInfo;
import com.trilogy.app.crm.web.control.ExtensionViewCustomizationWebControl;
import com.trilogy.app.crm.web.control.FilteredEntityWebControl;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMServiceExtensionHolderWebControl extends ServiceExtensionHolderWebControl
{
    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getExtensionWebControl()
    {
        return CUSTOM_EXTENSION_WC;
    }

    public static final WebControl CUSTOM_EXTENSION_WC = new ExtensionViewCustomizationWebControl(
                                  new FilteredEntityWebControl(
                                      new And()
                                          .add(RegisteredExtensionPredicate.instance())
                                          .add(ExtensionFilterPredicate.instance()), 
                                      com.redknee.app.crm.extension.service.ServiceExtension.class, false,
                                      false),
                                  /* Hidden Properties */
                                  ServiceExtensionXInfo.SERVICE_ID);
}
