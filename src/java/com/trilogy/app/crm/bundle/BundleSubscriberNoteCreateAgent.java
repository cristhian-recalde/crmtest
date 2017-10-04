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
package com.trilogy.app.crm.bundle;

import java.util.Iterator;

import com.trilogy.app.crm.bas.tps.pipe.PipelineAgent;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Writes a subscriber Note indicatong the adjustment has been made
 *
 * @author amedina
 */
public class BundleSubscriberNoteCreateAgent extends PipelineAgent
{

    /**
     * @param delegate
     */
    public BundleSubscriberNoteCreateAgent(ContextAgent delegate)
    {
        super(delegate);
    }

    /**
     * @param ctx A context
     * @throws AgentException thrown if one of the services fails to initialize
     */
    @Override
    public void execute(Context ctx) throws AgentException
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        BundleAdjustment form = (BundleAdjustment) ctx.get(BundleAdjustment.class);

        ExceptionListener exceptions = (ExceptionListener) ctx.get(ExceptionListener.class);

        
        
        if (sub != null && form != null)
        {
            for (Iterator i = form.getItems().iterator(); i.hasNext();)
            {
                BundleAdjustmentItem item = (BundleAdjustmentItem) i.next();

               
                try
                {
                	BundleProfile bundle =  HomeSupportHelper.get(ctx).findBean(ctx,com.redknee.app.crm.bundle.BundleProfile.class,item.getBundleProfile());
                	
                	int bundleType = bundle.getType(); 
                	UnitTypeEnum unitType = UnitTypeEnum.get((short)bundleType);
                	String strUnit = unitType.getUnits();
                	String basicUnit = unitType.getBasicUnit();
                	long amount = item.getAmount();
                	
                	if("$".equals(strUnit)){
                		  final Currency currency =
                                  ReportUtilities.getCurrency(ctx, sub.getCurrency(ctx));
                	
                		strUnit = currency.formatValue(amount); // For Currency bundles append $.
                
                	}else if(strUnit.contains(basicUnit)){ //if unit and basic units are similar e.g. unit:messages and basicUnit:message
                	
                		strUnit = amount+" "+strUnit;
                
                	}else{//If unit and basicUnits are different. e.g. unit:minutes and basicUnit: seconds
               
                		strUnit = amount+" "+basicUnit;
                	}
                    
                    NoteSupportHelper.get(ctx).addSubscriberNote(ctx,
                            sub.getId(),
                            "Bundle adjustment: " + form.getAgent() + "," +
                                    item.getType().getDescription() + "," +
                                    item.getBundleProfile() + "," + strUnit,
                            SystemNoteTypeEnum.ADJUSTMENT,
                            SystemNoteSubTypeEnum.SUBUPDATE);
                }
                catch (HomeException e)
                {
                    AgentException ex = new AgentException("Unable to create Subscriber Note for " + sub.getMSISDN(), e);
                    exceptions.thrown(ex);
                    throw ex;
                }
            }
        }
        else
        {
            AgentException ex = new AgentException("System error: No parameters or no subscriber set in the context");
            exceptions.thrown(ex);
            throw ex;
        }

        pass(ctx, this, "SubscriberNote successfuly created");

    }
}
