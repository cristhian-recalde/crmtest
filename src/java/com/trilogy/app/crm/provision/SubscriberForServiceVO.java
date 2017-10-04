/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.provision;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 21, 2005
 * 
 * This class is used to hold the newsubscriber, oldsubscriber values. Purpose: Uptill
 * CRM6 all the servcies were having similar logic for provisoning and unprovisioning and
 * depending upon the state/msisdn changes either ProvisionAgent or UnprovisionAgent was
 * getting called. The problem here is for any new service which doesnt want to obey
 * current behavior of the services we dont get correct newSubscriber and oldSubscriber
 * objects correctly in the context (because SubscriberProvisionServicesHome puts old
 * subscriber in the ocntext upon some conditions. This object will hold the correct
 * values and any new service agant like VoicemailProvisionAgent can use this object in
 * order to deviate from all the other services' behavior.
 * 
 * We should later redesign SubscriberProvisionServicesHome such that there is no need to
 * create and put this object in the context.
 */
public class SubscriberForServiceVO
{

    Subscriber oldSubscriber = null;
    Subscriber newSubscriber = null;
    Set provisionedServiceIdSet = new HashSet();
    boolean msisdnChanged = false;
    boolean stateChanged = false;    
    int callerID = -1;
    


    public SubscriberForServiceVO()
    {
    }

    public SubscriberForServiceVO(Context ctx, Subscriber oldSub, Subscriber newSub, int caller)
    {
        oldSubscriber = oldSub;
        newSubscriber = newSub;
        callerID = caller;
        // Store the provisioned servcies which subscriber has originally while entering
        // the store method
        // those are modified later, thats why we need to save them here
        Set serviceIds = newSub.getProvisionedServices(ctx);
        if( serviceIds != null )
        {
            for (Iterator itr = serviceIds.iterator(); itr.hasNext();)
            {
                provisionedServiceIdSet.add(itr.next());
            }
        }
    }

    public final static int CALLED_FROM_CREATE = 0;
    public final static int CALLED_FROM_STORE = 1;
    public final static int CALLED_FROM_REMOVE = 2;
}
