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
package com.trilogy.app.crm.priceplan.validator;

/**
 * @author skularajasingham
 *
 */
public class PreRequisiteGroupRec {

    String preq_service = null;
    String preq_dependency = null;

    public PreRequisiteGroupRec (String preq_service, String preq_dependency)
    {
        this.preq_service=preq_service;
        this.preq_dependency=preq_dependency;
    }

    public PreRequisiteGroupRec (long preq_service, long preq_dependency)
    {
        this.preq_service=""+preq_service;
        this.preq_dependency=""+preq_dependency;
    }

    public String getPreqService()
    {
        return this.preq_service;
    }

    public String getPreqDependency()
    {
        return this.preq_dependency;
    }
}
