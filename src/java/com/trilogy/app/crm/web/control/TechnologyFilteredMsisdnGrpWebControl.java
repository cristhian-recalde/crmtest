/*
 * Created on Jun 7, 2006
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
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.MsisdnGroupKeyWebControl;
import com.trilogy.app.crm.bean.MsisdnGroupTransientHome;
import com.trilogy.app.crm.bean.MsisdnGroupXInfo;
import com.trilogy.app.crm.bean.SubBulkCreate;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author msubramanyam
 *
 * This web control filters the Msisdn group based on the Technology Segmentation.
 * 
 */
public class TechnologyFilteredMsisdnGrpWebControl extends ProxyWebControl {

    /**
     * Constructor 
     */
    public TechnologyFilteredMsisdnGrpWebControl() {}    

    
    /* 
     * @see com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework.xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter pWriter, String name, Object obj)
    {
        SubBulkCreate subBulkCreate = (SubBulkCreate)ctx.get(AbstractWebControl.BEAN);
        
        TechnologyEnum techEnum = subBulkCreate.getTechnology();
        
        Home msisdnGrpHome = (Home) ctx.get(MsisdnGroupHome.class);
        
        Collection coll = null;
        
        Home home = new MsisdnGroupTransientHome(ctx);
        
        try 
        {
            if (techEnum != null)
            {
        	coll = msisdnGrpHome.where(ctx,new EQ(MsisdnGroupXInfo.TECHNOLOGY,techEnum)).selectAll(ctx);
            }
        } 
        catch (UnsupportedOperationException e) 
        {
            new DebugLogMsg(this,"Exception while getting the Msisdn Group collection for the Technology =" + techEnum.getDescription(),e).log(ctx);
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this,"Exception while getting the Msisdn Group collection for the Technology =" + techEnum.getDescription(),e).log(ctx);
        }
        
        Context subCtx = ctx.createSubContext();
        
        if (coll != null && coll.size() > 0)
        {
            Iterator iter = coll.iterator();            
            while(iter.hasNext())
            {
                MsisdnGroup msisdnGrp = (MsisdnGroup) iter.next();
                try
                {
                    home.create(subCtx,msisdnGrp);
                }
                catch(HomeException he)
                {
                    LogSupport.major(ctx,he,"Exception thrown while creating Transient home with msisdn group collection for Technology =" + techEnum.getDescription());
                    return;
                }
            }
        }
        else
        {
            home = msisdnGrpHome;
        }
        
        subCtx.put(MsisdnGroupHome.class,home);

        this.setDelegate(new MsisdnGroupKeyWebControl(1,true,true));
        super.toWeb(subCtx, pWriter, name, obj);
    }
}
