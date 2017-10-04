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
package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.util.pattern.XPattern;


/**
 * Concrete System Feature class to implement non-generated business logic.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class SysFeatureCfg extends AbstractSysFeatureCfg
{
    public XPattern getAccountManagerIDXPattern()
    {
        if (acctMgrIdPattern_ == null
                || !SafetyUtil.safeEquals(acctMgrIdPattern_.getPattern(), this.getAccountManagerIDRegex()))
        {
            acctMgrIdPattern_ = XPattern.compile(this.getAccountManagerIDRegex());
        }
        return acctMgrIdPattern_;
    }

    private XPattern acctMgrIdPattern_;
}
