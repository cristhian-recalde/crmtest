/* 
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries. 
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.blackberry.error;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.ERLogMsg;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.service.blackberry.model.ResultEnum;

/**
 * @author arturo.medina@redknee.com
 * 
 */
public class ERErrorHandler extends CoreERLogger implements ErrorHandler
{

    /**
     * {@inheritDoc}
     */
    public void handleError(Context ctx, Subscriber subscriber,
            Service service, ResultEnum resultStatus, String errorCode,
            String description)
    {
        String[] fields = new String[9];

        fields[0] = subscriber.getId();
        fields[1] = subscriber.getMSISDN();
        fields[2] = subscriber.getIMSI();
        if (service!=null)
        {
            fields[3] = Long.toString(service.getIdentifier());
	        fields[4] = BlackberrySupport.getBlackberryServicesIdsForService(ctx, service.getID()).toString();
	        fields[5] = service.getType().getDescription();
        }
        fields[6] = description;
        fields[7] = resultStatus.toString();
        fields[8] = errorCode;

        new ERLogMsg(BLACKBERRY_ERROR_ID, ERLogger.RECORD_CLASS,
                BLACKBERRY_ERROR_NAME, subscriber.getSpid(), fields).log(ctx);

    }

    private static int BLACKBERRY_ERROR_ID = 1116;

    /**
     * Description of ER 1115
     */
    public static final String BLACKBERRY_ERROR_NAME = "Blackberry external service error";
}
