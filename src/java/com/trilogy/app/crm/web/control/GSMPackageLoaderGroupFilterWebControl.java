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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PackageGroupTransientHome;
import com.trilogy.app.crm.bean.PackageGroupXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.service.GSMPackageBulkLoaderRequestServicer;
import com.trilogy.app.crm.web.service.GSMPackageLoaderDTO;

/**
 * @author msubramanyam
 * @author bdhavalshankh
 *
 * This class filters the Package Group's based on the SPID and Technology field
 */
public class GSMPackageLoaderGroupFilterWebControl extends ProxyWebControl {

    /**
     * Default Constructor
     */
    public GSMPackageLoaderGroupFilterWebControl(WebControl delegate) {
	super(delegate);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter pWriter, String name, Object obj)
    {
    	Object dto_obj = ctx.get(GSMPackageBulkLoaderRequestServicer.GSMDTO);
    	GSMPackageLoaderDTO dto = null;
    	if (dto_obj != null)
    	{
    	    dto = (GSMPackageLoaderDTO) dto_obj;
    	}
    	String spid = null;
    	String technology = null;
    	if (dto != null)
    	{
    	    spid = String.valueOf(dto.getSpid());
    	}
    	TechnologyEnum techEnum = TechnologyEnum.GSM;

        Collection coll = null;
        Home home = new PackageGroupTransientHome(ctx);

        try
        {
            And and = new And();
            and.add(new EQ(PackageGroupXInfo.TECHNOLOGY,techEnum));
            and.add(new EQ(PackageGroupXInfo.SPID, Integer.valueOf(spid != null?spid:"0")));
            
            coll = HomeSupportHelper.get(ctx).getBeans(ctx, PackageGroup.class , and);
        } 
        catch (UnsupportedOperationException e) 
        {
            new DebugLogMsg(this,"Exception while getting the Package Group collection for the Technology =" + techEnum.getDescription(),e).log(ctx);
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this,"Exception while getting the Package Group collection for the Technology =" + techEnum.getDescription(),e).log(ctx);
        }

        Context subCtx = ctx.createSubContext();
        
        if (coll != null && coll.size() > 0)
        {
            Iterator iter = coll.iterator();            
            while(iter.hasNext())
            {
                PackageGroup pkgGrp = (PackageGroup) iter.next();
                try
                {
                    home.create(subCtx,pkgGrp);
                }
                catch(HomeException he)
                {
                    LogSupport.major(ctx,he,"Exception thrown while creating Transient home with package group collection for Technology =" + techEnum.getDescription());
                    return;
                }
            }
        }
        
        subCtx.put(PackageGroupHome.class,home);

        super.toWeb(subCtx, pWriter, name, obj);
    }
}
