package com.trilogy.app.crm.dunning;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.AccountStateEnum;

/**
 * @author abhay.parashar
 *   This class defines primary filters required when running Dunning process from various options.
 *   E.g. Dunning account from 
 *   	1. Run Dunning on All Account process.
 *   	2. Run Dunning for specific SPID from GUI.
 *      3. Run Dunning for specific Bill Cycle from GUI.  
 *		4. When Credit Category is marked as Dunning Exmpt. 
 */
public class AccountDunningSqlGenerator {
	
	private static Map<Integer,String> dunningSqlGenerator = new HashMap<Integer,String>();
	
	static{
		/*
		 * DUNNING_DEFAULT_KEY : Primary Filter.
		 */
		dunningSqlGenerator.put(DunningConstants.DUNNING_DEFAULT_KEY, 
							new String("select acc.ban from account acc where acc.ban in( select ban from ageddebt adbt where {0} )" +			 	
			 	 " and (acc.state not in(" + AccountStateEnum.SUSPENDED.getIndex()+","+AccountStateEnum.INACTIVE.getIndex()+")" +
			 	 " or acc.ptptermstightened = 'y')"+
			 	 " and acc.responsible = 'y'"+
			 	 " and acc.systemtype = 0"));
		/*
		 * DUNNING_BILLCYCLE_KEY : Filter for running Dunning on specific bill cycle.
		 */
		dunningSqlGenerator.put(DunningConstants.DUNNING_BILLCYCLE_KEY, new String(" and acc.billCycleID ="));
		
		/*
		 * DUNNING_CREDITCATEGORY_KEY : Filter for Dunning gets triggred at the time of credit category change.
		 */
		
		dunningSqlGenerator.put(DunningConstants.DUNNING_CREDITCATEGORY_KEY, new String(" select acc.ban from account acc where (acc.state = " + AccountStateEnum.PROMISE_TO_PAY.getIndex() + 
				" or acc.state = " + AccountStateEnum.NON_PAYMENT_WARN.getIndex() + 
				" or acc.state = " + AccountStateEnum.NON_PAYMENT_SUSPENDED.getIndex() + 
				" or acc.state = " + AccountStateEnum.IN_ARREARS.getIndex() +")" +
				" and acc.responsible = 'y' " +
				" and acc.systemtype = 0 " +
				" and acc.creditCategory =") );
		
		/*
		 * DUNNING_SPID_KEY : Filter for running Dunning on specific SPID.
		 */
		
		dunningSqlGenerator.put(DunningConstants.DUNNING_SPID_KEY,new String(" and acc.Spid = "));
		
		dunningSqlGenerator.put(DunningConstants.DUNNINGPOLICY_DEFAULT_KEY,
				new String("select acc.ban from account acc where acc.state not in("
					+AccountStateEnum.SUSPENDED_INDEX+","
					+AccountStateEnum.INACTIVE_INDEX+
					") and acc.responsible = 'y' and acc.systemtype = 0"));
	}
	
	public static Map<Integer, String> getDunningSqlGenerator() {
		return dunningSqlGenerator;
	}
}
