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

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.extension.auxiliaryservice.GroupChargingAuxSvcExtensionWebControl;
import com.trilogy.app.crm.support.AbstractIdentitySupport;
import com.trilogy.app.crm.web.control.CurrencyContextSetupWebControl;
import com.trilogy.app.crm.web.control.CustomizedGlcodeKeyWebControl;
import com.trilogy.app.crm.web.control.IdentitySupportWebControl;
import com.trilogy.app.crm.web.control.LinkedWebControl;
import com.trilogy.app.crm.web.control.PrimaryKeyWebControl;
import com.trilogy.framework.core.web.XCurrencyWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * WebControl for the Group Charging auxiliary service extension.
 * @author Marcio Marques
 * @since 9.1.2
 *
 */
public class CRMGroupChargingAuxSvcExtensionWebControl extends GroupChargingAuxSvcExtensionWebControl
{
    public WebControl getGroupChargeWebControl() { return GROUP_CHARGE; }
    
    public WebControl getGroupAdjustmentTypeWebControl() { return GROUP_ADJUSTMENT_TYPE; }
    
    public WebControl getGroupGLCodeWebControl() { return GROUP_GL_CODE; }
    
    public static final WebControl GROUP_CHARGE         = new CurrencyContextSetupWebControl(new XCurrencyWebControl(
                                                                  false));
    
    public static final WebControl GROUP_ADJUSTMENT_TYPE = new LinkedWebControl(
                                                                  new PrimaryKeyWebControl(
                                                                          new IdentitySupportWebControl(
                                                                                  new TextFieldWebControl(),
                                                                                  new AbstractIdentitySupport()
                                                                                  {
                                                                                      public String toStringID(
                                                                                              Object bean)
                                                                                      {
                                                                                          final AdjustmentType type = (AdjustmentType) bean;
                                                                                          return type.getName();
                                                                                      }
                                                                                  }), AdjustmentTypeHome.class),
                                                                  "AppCrmAdjustmentType");
    
    public static final WebControl GROUP_GL_CODE         = new com.redknee.framework.xhome.msp.SetSpidProxyWebControl(
                                                                  new CustomizedGlcodeKeyWebControl());
}
