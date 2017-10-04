/*
 * Created on Nov 2, 2006
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
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.web.service.TDMAPackageLoaderDTO;

/**
 * @author msubramanyam
 *
 * This class filters the Package Group's based on the Technology field
 */
public class TDMAPackageLoaderTechnologyFilterWebControl extends ProxyWebControl {

    /**
     * Default Constructor
     */
    public TDMAPackageLoaderTechnologyFilterWebControl(WebControl delegate) {
	super(delegate);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter pWriter, String name, Object obj)
    {
	Object dto_obj = ctx.get("DTO_OBJ");
	TDMAPackageLoaderDTO dto = null;
	if (dto_obj != null)
	{
	    dto = (TDMAPackageLoaderDTO) dto_obj;
	}
	String spid = null;
	String technology = null;
	if (dto != null)
	{
	    spid = String.valueOf(dto.getSpid());
	    technology = String.valueOf(dto.getTechnology());
	}
	TechnologyEnum techEnum = TechnologyEnum.get(technology != null?Short.parseShort(technology):TechnologyEnum.TDMA_INDEX );

        Home pkgGrpHome = (Home) ctx.get(PackageGroupHome.class);

        Collection coll = null;
        Home home = new PackageGroupTransientHome(ctx);

        try
        {
            And and = new And();
            and.add(new EQ(PackageGroupXInfo.TECHNOLOGY,techEnum));
            and.add(new EQ(PackageGroupXInfo.SPID, Integer.valueOf(spid != null?spid:"0")));
            coll = pkgGrpHome.where(ctx,and).selectAll(ctx);
        
        } 
        catch (UnsupportedOperationException e) 
        {
            new DebugLogMsg(this,"Exception while getting the UIM Package Group collection for the Technology =" + techEnum.getDescription(),e).log(ctx);
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this,"Exception while getting the UIM Package Group collection for the Technology =" + techEnum.getDescription(),e).log(ctx);
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

        //this.setDelegate(new PackageGroupKeyWebControl(1,true,true));

        super.toWeb(subCtx, pWriter, name, obj);
    }
}
