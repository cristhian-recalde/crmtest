package com.trilogy.app.crm.bean;

import java.io.Serializable;
import java.util.Comparator;

import com.trilogy.framework.xhome.beans.SafetyUtil;

public class ConvergedAcctSubComparator implements Comparator, Serializable
{

	public int compare(Object obj1, Object obj2) {
		
		ConvergedAccountSubscriber conAcctSub1 = (ConvergedAccountSubscriber) obj1;
		ConvergedAccountSubscriber conAcctSub2 = (ConvergedAccountSubscriber) obj2;

        if (conAcctSub1 == null && conAcctSub2 == null)
        {
            return 0;   
        }

        if (conAcctSub1 == null)
        {
            return -1;   
        }

        if (conAcctSub2 == null)
        {
            return 1;   
        }
        
        int result = SafetyUtil.safeCompare(conAcctSub1.getBAN(), conAcctSub2.getBAN());
		if(result == 0)
		{
			result = SafetyUtil.safeCompare(conAcctSub1.getMSISDN(), conAcctSub2.getMSISDN());
		}
		
		return result;
	}
}
