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


package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;


public class SubscriberAuxiliaryServiceSubscriberValidator implements Validator
{
    private static Validator instance_ = null;
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriberAuxiliaryServiceSubscriberValidator();
        }
        return instance_;
    }
    
    protected SubscriberAuxiliaryServiceSubscriberValidator()
    {
    }

    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
       SubscriberAuxiliaryService subAux = (SubscriberAuxiliaryService) obj;
       if("-1".equals(subAux.getSubscriberIdentifier())){
           new DebugLogMsg(this,"Subscriber Auxiliary Service " + subAux + "tried to be assosaited with subscriber identifier of -1",null).log(ctx);
           throw new IllegalStateException("The subscriber Identifier cannot be set to be -1");
          
    }
}
}
