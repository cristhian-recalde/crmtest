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
package com.trilogy.app.crm.numbermgn;

import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.home.OldTDMAPackageLookupHome;
import com.trilogy.app.crm.support.DefaultPackageSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Home class for Package .
 *
 * @author bdhavalshankh
 */

public class PackageStateChangeHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public PackageStateChangeHome(Home delegate)
    {
        super(delegate);
    }


    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Object result = null;
        if (!(obj instanceof TDMAPackage))
        {
            result = super.store(ctx, obj);
        }
        else if (obj instanceof TDMAPackage)
        {
            TDMAPackage tdmaPack = (TDMAPackage) obj;
            String currentSerialNo = tdmaPack.getSerialNo().trim();
            if(currentSerialNo!= null && !currentSerialNo.equals(tdmaPack.getPackId().trim()))
            {
            TDMAPackage oldBean = OldTDMAPackageLookupHome.getOldTDMAPackage(ctx);
            try
            {

                TDMAPackage secondaryPackage = null;
                Home packageHome = (Home) ctx.get(TDMAPackageHome.class);
                
                if(tdmaPack.getESN() != null && !tdmaPack.getESN().trim().equals(""))
                {
                    if(currentSerialNo != null && !currentSerialNo.trim().equals(""))
                    {
                        //Adding below if to make sure this update does not result into StackOverflow 
                        if(!tdmaPack.getPackId().equals(currentSerialNo))
                        {
                            secondaryPackage = DefaultPackageSupport.instance().getTDMAPackage(ctx, currentSerialNo,tdmaPack.getSpid());
                        }
                    }
                }
                
                if(oldBean.getState().equals(PackageStateEnum.IN_USE) && tdmaPack.getState().equals(PackageStateEnum.HELD))
                {
                    if(secondaryPackage != null)
                    {
                        secondaryPackage.setState(PackageStateEnum.HELD);
                        packageHome.store(secondaryPackage);
                    }
                    
                    /**
                     *  This will make sure we are not setting null serial number for secondary package
                     */
                    if(!tdmaPack.getSerialNo().equals(tdmaPack.getPackId())) 
                    {
                        tdmaPack.setSerialNo(null);
                    }
                }
                
                
                result = super.store(ctx, tdmaPack);
                
                if(oldBean.getState().equals(PackageStateEnum.AVAILABLE) && tdmaPack.getState().equals(PackageStateEnum.IN_USE))
                {
                    if(secondaryPackage != null)
                    {
                        secondaryPackage.setState(PackageStateEnum.IN_USE);
                        packageHome.store(secondaryPackage);
                    }
                }
            }
            catch (HomeException e)
            {
                LogSupport.major(ctx, this,e.getMessage());
                throw e;
            }
            }else
            {
            	result = super.store(ctx, tdmaPack);
            }
        }
        return result;
    }
}
