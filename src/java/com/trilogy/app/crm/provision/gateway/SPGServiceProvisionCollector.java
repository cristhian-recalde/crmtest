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
package com.trilogy.app.crm.provision.gateway;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;


/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 
 */
public class SPGServiceProvisionCollector
{
    Set<Long> removeList = new HashSet<Long>();
    Set<Long> addList = new HashSet<Long>();
    Set<Long> currentList = new HashSet<Long>();

    public Subscriber newSub;
    public Subscriber oldSub;
    Map<Long, ServiceBase> servicesMap = new HashMap<Long, ServiceBase>();
    Map<Long, Object> ssMap = new HashMap<Long, Object>();
    
    Set<Long> SPGServiceIDList = new HashSet<Long>(); 

    public boolean hasProvisionActions()
    {
        return removeList.size() > 0 || addList.size() > 0;
    }
    

}
