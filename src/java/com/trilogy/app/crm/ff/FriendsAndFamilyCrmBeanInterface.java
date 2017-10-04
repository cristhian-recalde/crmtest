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
package com.trilogy.app.crm.ff;

import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.CallingGroupTypeEnum;

/**
 * This interface exposes common methods for all Friend and Family beans defined in CRM.
 */
public interface FriendsAndFamilyCrmBeanInterface
{
    long getID();
    String getName();
    int getSpid();
    CallingGroupTypeEnum getCallingGroupType();
    long getMonthlyCharge();
    boolean getSmartSuspension();
    ActivationFeeModeEnum getActivationFee();
    String getAdjustmentGLCode();
    int getTaxAuthority();
    void setAuxiliaryService(long auxiliaryServiceID);
    long getAuxiliaryService();
    // TODO these methods are better excluded in future
    void setSmartSuspension(boolean smartSuspension);
    void setActivationFee(ActivationFeeModeEnum activationFee);
}
