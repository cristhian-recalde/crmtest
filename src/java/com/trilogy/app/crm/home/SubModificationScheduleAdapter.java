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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * Adapter to set the values of snapshot and supporting info
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_9
 */
public class SubModificationScheduleAdapter implements Adapter {

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException {
        
    	SubModificationSchedule subModificationSchedule = (SubModificationSchedule) obj;
    	
    	if (subModificationSchedule != null)
    	{
    		subModificationSchedule.setNewPricePlanDetails(((StringHolder) (subModificationSchedule.getSnapshot().get(0))).getValue());
    		subModificationSchedule.setOldPricePlanDetails(((StringHolder) (subModificationSchedule.getSupportingInformation().get(0))).getValue());
    	}
    	return subModificationSchedule;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException {
    	SubModificationSchedule subModificationSchedule = (SubModificationSchedule) obj;     
        List listSnapshot = new ArrayList();
        List listSupportingInfo = new ArrayList();
        if (subModificationSchedule != null)
    	{
        	listSnapshot.add(new StringHolder(subModificationSchedule.getNewPricePlanDetails()));
        	listSupportingInfo.add(new StringHolder(subModificationSchedule.getOldPricePlanDetails()));
        
        	subModificationSchedule.setSnapshot(listSnapshot);
        	subModificationSchedule.setSupportingInformation(listSupportingInfo);
    	}
	
        return subModificationSchedule;
    }

}

