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

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.BeanOperationEnum;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DealerCodeXInfo;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.xhome.home.SimpleBeanERHome;

/**
 * Logs ER 1106 everytime a Dealer Code is added or modified in the system 
 * through the GUI.
 * 
 * [April 20, 2007] New ER format was introduced to track Add/Update/Delete of Dealer Codes
 * ER 1123.  This class will generate both ERs.
 *
 * @author angie.li@redknee.com
 */
public class DealerCodeERLogHome extends SimpleBeanERHome
{
    public DealerCodeERLogHome(final Home delegate)
    {
        super(delegate, IDENTIFIER, CLASS, TITLE, FIELDS);
    }

    /**
     * Calls the SimpleBeanERHome to create the ER 1123, and
     * Calls the ERLogger to log ER 1106.
     */
    public Object create(Context ctx, Object obj)
    throws HomeException
    {
        DealerCode dealerCode = (DealerCode) super.create(ctx,obj);

        ERLogger.modifyDealerCodeEr(ctx, dealerCode, String.valueOf(BeanOperationEnum.ADD_INDEX), TPSPipeConstant.RESULT_CODE_SUCCESS);

        return dealerCode;
    }

    /**
     * Calls the SimpleBeanERHome to create the ER 1123, and
     * Calls the ERLogger to log ER 1106.
     */
    public Object store(Context ctx, Object obj)
    throws HomeException
    {
        DealerCode dealerCode = (DealerCode) super.store(ctx,obj);

        ERLogger.modifyDealerCodeEr(ctx, dealerCode, String.valueOf(BeanOperationEnum.UPDATE_INDEX), TPSPipeConstant.RESULT_CODE_SUCCESS);

        return dealerCode;
    }

    protected Object getOriginal(final Context context, final Object object) throws HomeException
    {
        final DealerCode newBean = (DealerCode)object;

        final Home home = (Home)context.get(DealerCodeHome.class);

        And and = new And();
        and.add(new EQ(DealerCodeXInfo.CODE, newBean.getCode()));
        and.add(new EQ(DealerCodeXInfo.SPID, Integer.valueOf(newBean.getSpid())));
        
        return home.find(context, and);
    }

    private static final int IDENTIFIER = 1123;
    private static final int CLASS = 700;
    private static final String TITLE = "Dealer Code Management";

    private static final PropertyInfo[] FIELDS =
    {
        DealerCodeXInfo.CODE,
        DealerCodeXInfo.SPID,
        DealerCodeXInfo.DESC
    };

}
