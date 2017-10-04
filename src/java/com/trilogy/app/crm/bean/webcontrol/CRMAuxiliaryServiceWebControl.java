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

import com.trilogy.framework.xhome.webcontrol.FinalWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.web.control.RBTParamWebControl;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMAuxiliaryServiceWebControl extends CustomAuxiliaryServiceWebControl
{

    /**

    
    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getTypeWebControl()
    {
        return CUSTOM_CRM_TYPE_WC;
    }
    
    public static final WebControl CUSTOM_CRM_TYPE_WC = new FinalWebControl(new com.redknee.app.crm.web.control.AuxiliaryServiceEnumWebControl());

}
