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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.PricePlanGroup;

public class PricePlanGroupAdapter implements Adapter {

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException {
    	
    	PricePlanGroup priceplangroup = (PricePlanGroup) obj;
  
    	//Adapt Dependent Services
      	String str = priceplangroup.getDepend_group_list();        
    	Set set = new HashSet();
        StringTokenizer st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            set.add(st.nextToken());
      
        }  	    	
    	priceplangroup.setDepend_list(set);

    	//Adapt PreReq Services
      	str = priceplangroup.getPrereq_group_list();     
    	set = new HashSet();
        StringTokenizer st2 = new StringTokenizer(str,",");
        while (st2.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            set.add(st2.nextToken());
      
        }
        priceplangroup.setPrereq_list(set);
    	
      
    	return priceplangroup;
       // return obj;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException {
    	PricePlanGroup priceplangroup = (PricePlanGroup) obj;

    	
    	//UnAdapt Dependent Services
    	Set set = priceplangroup.getDepend_list();
    	Object[] arr = set.toArray();
    	StringBuilder buff = new StringBuilder();
    	
    	for (int x=0; x<arr.length; x++)
    	{
    		if (x!=0)
            {
                buff.append(",");
            }
    		buff.append(arr[x]);
    	}
    	priceplangroup.setDepend_group_list(buff.toString());
    	

    	
    	//UnAdapt PreReq Services
    	set = priceplangroup.getPrereq_list();
    	arr = set.toArray();
    	buff = new StringBuilder();
    	
    	for (int x=0; x<arr.length; x++)
    	{
    		if (x!=0)
            {
                buff.append(",");
            }
    		buff.append(arr[x]);
    	}
    	priceplangroup.setPrereq_group_list(buff.toString());
    	
    	
        return obj;
    }

}

