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

import com.trilogy.app.crm.bean.DependencyGroup;

public class DependencyGroupAdapter implements Adapter {

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj) throws HomeException {
    	
    	DependencyGroup dependencygroup = (DependencyGroup) obj;
  
    	//Adapt Services
      	String str = dependencygroup.getServicesset();        
    	Set set = new HashSet();
        StringTokenizer st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            set.add(st.nextToken());
      
        }  	
    	dependencygroup.setServices(set);

    	//Adapt Auxiliary Services
      	String auxstr = dependencygroup.getAuxSet();     
    	Set auxset = new HashSet();
        StringTokenizer st2 = new StringTokenizer(auxstr,",");
        while (st2.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            auxset.add(st2.nextToken());
      
        }  	
    	dependencygroup.setAuxiliaryService(auxset);
    	
    	
    	//Adapt Bundle Services && Aux Bundle Services
      	str = dependencygroup.getBundleserviceset(); 
    	set = new HashSet();
        st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            set.add(st.nextToken());
      
        }  	
    	dependencygroup.setServicebundle(set); 	
    	
/*    	//Adapt Aux Bundle Services
      	str = dependencygroup.getAuxbundleset(); 
    	set = new HashSet();
        st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            set.add(st.nextToken());
      
        }  	
    	dependencygroup.setAuxbundle(set);  	
  */  	
      
    	return dependencygroup;
       // return obj;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj) throws HomeException {
    	DependencyGroup dependencygroup = (DependencyGroup) obj;
    	//dependenygroup.setHashset("from unAdapt");
    	
    	//UnAdapt Services
    	Set set = dependencygroup.getServices();
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
    	dependencygroup.setServicesset(buff.toString());

    	
    	//UnAdapt Auxiliary Services
    	Set auxset = dependencygroup.getAuxiliaryService();
    	arr = auxset.toArray();
    	buff = new StringBuilder();
    	
    	for (int x=0; x<arr.length; x++)
    	{
    		if (x!=0)
            {
                buff.append(",");
            }
    		buff.append(arr[x]);
    	}
    	dependencygroup.setAuxSet(buff.toString());
    	
    	
       	//UnAdapt Bundle Services & Aux Bundle Services
    	set = dependencygroup.getServicebundle();
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
    	dependencygroup.setBundleserviceset(buff.toString());
    	
     /*  	//UnAdapt Aux Bundle Services
    	set = dependencygroup.getAuxbundle();
    	arr = set.toArray();
    	buff = new StringBuilder();
    	
    	for (int x=0; x<arr.length; x++)
    	{
    		if (x!=0) buff.append(",");
    		buff.append(arr[x]);
    	}
    	
    	dependencygroup.setAuxbundleset(buff.toString());
    	*/

    	
    	
        return obj;
    }

}

