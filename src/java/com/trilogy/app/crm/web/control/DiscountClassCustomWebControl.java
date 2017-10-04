/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.CRMSpidKeyWebControl;
import com.trilogy.app.crm.bean.DiscountClassWebControl;
import com.trilogy.app.crm.bean.TaxAuthorityKeyWebControl;
import com.trilogy.app.crm.support.AbstractIdentitySupport;
import com.trilogy.framework.xhome.webcontrol.FinalWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * 
 * @author simar.singh@redknee.com A custom web-control to drive the Discount Class web in
 *         CRM It extends the generated one to override the Adjustment-Type and
 *         Tax-Authority Related behviour This web-control should not be moved down to
 *         model projects because it depends on higher modules.
 * 
 */
public class DiscountClassCustomWebControl extends DiscountClassWebControl
{

    @Override
    public WebControl getSpidWebControl()
    {
        return spid_wc;
    }


    @Override
    public WebControl getAdjustmentTypeWebControl()
    {
        return adjustmentType_wc;
    }


    @Override
    public WebControl getAdjustmentTypeDescriptionWebControl()
    {
        return adjustmentTypeDescription_wc;
    }


    @Override
    public WebControl getGLCodeWebControl()
    {
        return GLCode_wc;
    }


    @Override
    public WebControl getInvoiceDescriptionWebControl()
    {
        return invoiceDescription_wc;
    }


    @Override
    public WebControl getTaxAuthorityWebControl()
    {
        return taxAuthority_wc;
    }

    public static final WebControl adjustmentType_wc = new LinkedWebControl(new PrimaryKeyWebControl(
            new IdentitySupportWebControl(new TextFieldWebControl(), new AbstractIdentitySupport()
            {

                private static final long serialVersionUID = 1L;


                public String toStringID(Object bean)
                {
                    final AdjustmentType type = (AdjustmentType) bean;
                    return type.getName();
                }
            }), AdjustmentTypeHome.class), "AppCrmAdjustmentType");
    public static final WebControl adjustmentTypeDescription_wc = new TextFieldWebControl(50);
    public static final WebControl GLCode_wc = new com.redknee.framework.xhome.msp.SetSpidProxyWebControl(
            new CustomizedGlcodeKeyWebControl());
    public static final WebControl invoiceDescription_wc = new TextFieldWebControl(50);
    public static final WebControl taxAuthority_wc = new com.redknee.framework.xhome.msp.SetSpidProxyWebControl(
            new TaxAuthorityKeyWebControl());
    public static final WebControl spid_wc = new FinalWebControl(new CRMSpidKeyWebControl(1, true,
            new KeyWebControlOptionalValue("--", "0")));
}
