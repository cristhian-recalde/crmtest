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

import com.trilogy.app.crm.bean.PrerequisiteGroup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

public class PrerequisiteGroupAdapter implements Adapter {

    public Object adapt(Context ctx, Object obj) throws HomeException {
    	
    	PrerequisiteGroup prereqgroup = (PrerequisiteGroup) obj;
  
/*    	prereqgroup.setIdentifier(prereqgroup.getIdentifier()+1);
    	//Adapt Services
      	String str = prereqgroup.getDependency_list();        
    	Set set = new HashSet();
        StringTokenizer st = new StringTokenizer(str,",");
        while (st.hasMoreTokens()) {
           // System.out.println(st.nextToken());
            set.add(st.nextToken());
      
        }  	
        prereqgroup.setDependencyGroup(set);
    	

      
    	return prereqgroup;*/
        return obj;
    }

    public Object unAdapt(Context ctx, Object obj) throws HomeException {
    /*	PrerequisiteGroup prereqgroup = (PrerequisiteGroup) obj;
    	//dependenygroup.setHashset("from unAdapt");
    	

    	Set set = prereqgroup.getDependencyGroup();
    	Object[] arr = set.toArray();
    	StringBuilder buff = new StringBuilder();
    	
    	for (int x=0; x<arr.length; x++)
    	{
    		if (x!=0) buff.append(",");
    		buff.append(arr[x]);
    	}
    	prereqgroup.setDependency_list(buff.toString());

    	return prereqgroup;*/
        return obj;
    }

}

