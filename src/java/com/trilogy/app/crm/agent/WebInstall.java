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
package com.trilogy.app.crm.agent;

import java.security.Permission;

import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.ui.Msisdn;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.ui.MsisdnXInfo;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.VSATPackageXInfo;
import com.trilogy.app.crm.numbermgn.GenericPackage;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextFactory;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;


/**
 * For customizing mode of web controls
 * 
 * @author dannyng
 * 
 */
public class WebInstall extends CoreSupport implements ContextAgent
{
    
    protected final static Permission MSISDN_STATE_READ = new SimplePermission("");
    protected final static Permission MSISDN_STATE_WRITE = new SimplePermission("app.crm.msisdn.state.write");

    protected final static Permission PACKAGE_STATE_READ = new SimplePermission("");
    protected final static Permission PACKAGE_STATE_WRITE = new SimplePermission("app.crm.package.state.write");
    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        // Set MSISDN state mode to VIEW for IN_USE msisdn
        AbstractWebControl.setMode(ctx, MsisdnXInfo.STATE, new ContextFactory()
        {
            public Object create(Context cCtx)
            {
                Msisdn msisdn = (Msisdn) cCtx.get(AbstractWebControl.BEAN);
                
                ViewModeEnum mode1 = AbstractWebControl.check(cCtx, MSISDN_STATE_READ, MSISDN_STATE_WRITE);
                if (mode1 != ViewModeEnum.READ_WRITE)
                {
                    // Return permission based mode if it is restricted
                    return mode1;   
                }
                
                if (msisdn != null && msisdn.getState() != MsisdnStateEnum.IN_USE && msisdn.getPortingType() == PortingTypeEnum.NONE )
                {
                    // No permission restrictions, MSISDN not in use
                    return ViewModeEnum.READ_WRITE;   
                }

                // If MSISDN is in use, do not let them edit
                return ViewModeEnum.READ_ONLY;
                
            }
        });
        
        // Set Package state mode
        ContextFactory packageModeContextFactory = new ContextFactory()
        {
            public Object create(Context cCtx)
            {
                GenericPackage crmPackage = (GenericPackage) cCtx.get(AbstractWebControl.BEAN);
                
                ViewModeEnum mode1 = AbstractWebControl.check(cCtx, PACKAGE_STATE_READ, PACKAGE_STATE_WRITE);
                if (mode1 != ViewModeEnum.READ_WRITE)
                {
                    // Return permission based mode if it is restricted
                    return mode1;   
                }
                
                if (crmPackage != null && crmPackage.getState() != PackageStateEnum.IN_USE)
                {
                    // No permission restrictions, package not in use
                    return ViewModeEnum.READ_WRITE;   
                }

                // If package is in use, do not let them edit
                return ViewModeEnum.READ_ONLY;
            }
        };
        AbstractWebControl.setMode(ctx, GSMPackageXInfo.STATE, packageModeContextFactory);
        AbstractWebControl.setMode(ctx, TDMAPackageXInfo.STATE, packageModeContextFactory);
        AbstractWebControl.setMode(ctx, VSATPackageXInfo.STATE, packageModeContextFactory);
    }
}
