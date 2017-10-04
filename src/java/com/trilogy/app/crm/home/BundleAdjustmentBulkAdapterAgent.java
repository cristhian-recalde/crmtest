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
package com.trilogy.app.crm.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.trilogy.app.crm.bundle.BundleAdjustment;
import com.trilogy.app.crm.bundle.BundleAdjustmentFactory;
import com.trilogy.app.crm.bundle.BundleAdjustmentItem;
import com.trilogy.app.crm.bundle.BundleBulkAdjustment;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.pipe.PipelineAgent;


/**
 * Adapt a BundleBulkAdjustment bean to a BundleAdjustment bean.
 *
 * @author suyash.gaidhani@redknee.com
 * @since 9.3.2
 */
public class BundleAdjustmentBulkAdapterAgent
extends PipelineAgent
{
	public static final String DATE_FORMAT_STRING = "MM/dd/yy HH:mm:ss";

	public BundleAdjustmentBulkAdapterAgent(ContextAgent delegate)
    {
        super(delegate);
    }
	
	public void execute(Context ctx) throws AgentException
	{
		BundleAdjustment crmAdjustment = (BundleAdjustment) unAdapt(ctx, ctx.get(BundleBulkAdjustment.class));
		ctx.put(BundleAdjustment.class, crmAdjustment);
		pass(ctx, this, "Adaption complete for BundleBulkAdjustment to BundleAdjustment");
	}

    /**
     * @throws AgentException 
     * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    
    private Object unAdapt(Context ctx,Object obj) throws AgentException 
    {
        if (obj == null)
        {
            return null;
        }
        
        final BundleBulkAdjustment bulkAdjustmentBean = (BundleBulkAdjustment) obj;
        
        BundleAdjustment crmAdjustment = (BundleAdjustment) BundleAdjustmentFactory.instance().create(ctx);

		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING);
		Date transDate = null;

		if (bulkAdjustmentBean.getTransDate()!=null && !bulkAdjustmentBean.getTransDate().trim().isEmpty())
		{
    		try
    		{
    			transDate = format.parse(bulkAdjustmentBean.getTransDate());
    		}
    		catch (ParseException e)
    		{
    			throw new AgentException(
    			    "TransDate of the adjustment is not in the proper format of "
    			        + DATE_FORMAT_STRING, e);
    		}
		}
		else
		{
		    transDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
		}

		crmAdjustment.setAgent(bulkAdjustmentBean.getPaymentAgency());

		BundleAdjustmentItem crmItem = new BundleAdjustmentItem();
        crmItem.setBundleProfile(bulkAdjustmentBean.getBundleProfile());
        crmItem.setAmount(bulkAdjustmentBean.getAmount());
        crmItem.setType(bulkAdjustmentBean.getBundleAdjustmentType()); 
        crmAdjustment.setItems(new ArrayList<BundleAdjustmentItem>());
        crmAdjustment.getItems().add(crmItem);

        return crmAdjustment;
    }
}

