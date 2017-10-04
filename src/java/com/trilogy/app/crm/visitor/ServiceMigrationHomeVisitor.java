/*
 * Created on Nov 17th, 2005
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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.visitor;

import java.util.Map;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;

/**
 * TODO: Author, please describe your creation [PaulSperneac] 
 */
public class ServiceMigrationHomeVisitor extends HomeVisitor
{
    protected Home serviceHome_;

    public ServiceMigrationHomeVisitor(Home adjustmentTypeHome, Home serviceHome)
    {
        super(adjustmentTypeHome);
        serviceHome_ = serviceHome;
    }

    @Override
    public void visit(Context _ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        Service service = (Service) obj;
        if (service.getAdjustmentType() == Service.DEFAULT_ADJUSTMENTTYPE)
        {
            AdjustmentType type = new AdjustmentType();
            type.setParentCode(AdjustmentTypeSupportHelper.get(_ctx).getAdjustmentTypeCodeByAdjustmentTypeEnum(_ctx, AdjustmentTypeEnum.RecurringCharges));
            type.setCode((int) (type.getParentCode() + service.getID() + 1));
            type.setName("Service " + service.getID() + "- "
                    + service.getName());
            type.setDesc(service.getAdjustmentTypeDesc());
            type.setAction(AdjustmentTypeActionEnum.EITHER);

            Map spidInformation = type.getAdjustmentSpidInfo();
            Object key = Integer.valueOf(service.getSpid());
            AdjustmentInfo information = (AdjustmentInfo) spidInformation
                    .get(key);

            if (information == null)
            {
                information = new AdjustmentInfo();
                spidInformation.put(key, information);
            }

            information.setSpid(service.getSpid());
            information.setGLCode(service.getAdjustmentGLCode());
            information.setInvoiceDesc(service.getAdjustmentInvoiceDesc());
            information.setTaxAuthority(service.getTaxAuthority());

            try
            {
                type = (AdjustmentType) getHome().create(_ctx, type);
            } catch (HomeException e)
            {
                try
                {
                    type = (AdjustmentType) getHome().store(_ctx, type);
                } catch (HomeException e1)
                {
                    if (LogSupport.isDebugEnabled(_ctx))
                    {
                        new DebugLogMsg(this, e1.getMessage(), e1).log(_ctx);
                    }
                }
            }

            try
            {
                service = (Service) service.deepClone();
            } catch (CloneNotSupportedException e2)
            {
                if (LogSupport.isDebugEnabled(_ctx))
                {
                    new DebugLogMsg(this, e2.getMessage(), e2).log(_ctx);
                }
            }

            service.setAdjustmentType(type.getCode());
            try
            {
                serviceHome_.store(_ctx, service);
            } catch (HomeException e1)
            {
                if (LogSupport.isDebugEnabled(_ctx))
                {
                    new DebugLogMsg(this, e1.getMessage(), e1).log(_ctx);
                }
            }
        }
    }
}
