/*
 * Created on Dec 19, 2013
 * 
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
 *
 * 
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeTransientHome;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.bulkloader.GemPlusGSMSIMBulkLoaderRequestServicer;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.web.service.GemPlusPackageLoaderDTO;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author msubramanyam
 * @author bdhavalshankh
 *
 * This class filters the Package dealer code based on the SPID and Technology GSM
 */
public class GemPlusPackageLoaderDealerFilterWebControl extends ProxyWebControl {

    /**
     * Default Constructor
     */
    public GemPlusPackageLoaderDealerFilterWebControl(WebControl delegate) {
	super(delegate);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter pWriter, String name, Object obj)
    {
    	Object dto_obj = ctx.get(GemPlusGSMSIMBulkLoaderRequestServicer.GEMPLUSDTO);
    	GemPlusPackageLoaderDTO dto = null;
    	if (dto_obj != null)
    	{
    	    dto = (GemPlusPackageLoaderDTO) dto_obj;
    	}
    	String spid = null;
    	if (dto != null)
    	{
    	    spid = String.valueOf(dto.getSpid());
    	}

        Collection coll = null;
        Home home = new DealerCodeTransientHome(ctx);

        try
        {
            And and = new And();
            and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(spid != null?spid:"0")));
            
            coll = HomeSupportHelper.get(ctx).getBeans(ctx, DealerCode.class , and);
        } 
        catch (UnsupportedOperationException e) 
        {
            new DebugLogMsg(this,"Exception while getting the Dealer collection for SPID =" + spid, e).log(ctx);
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this,"Exception while getting the Dealer collection for SPID =" + spid, e).log(ctx);
        }

        Context subCtx = ctx.createSubContext();
        
        if (coll != null && coll.size() > 0)
        {
            Iterator iter = coll.iterator();            
            while(iter.hasNext())
            {
                DealerCode dealerCode = (DealerCode) iter.next();
                try
                {
                    home.create(subCtx, dealerCode);
                }
                catch(HomeException he)
                {
                    LogSupport.major(ctx,he,"Exception thrown while creating Transient home with dealer code collection for spid =" +spid);
                    return;
                }
            }
        }
        
        subCtx.put(DealerCodeHome.class, home);

        super.toWeb(subCtx, pWriter, name, obj);
    }
}
