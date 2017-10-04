package com.trilogy.app.crm.creditcategoryupdate;

import java.util.HashMap;
import java.util.Map;
import com.trilogy.app.crm.creditcategoryupdate.CreditCategoryUpdateConstants;
import com.trilogy.app.crm.bean.AccountStateEnum;

public class AccountCreditCategoryUpdateSqlGenerator {
	
	private static Map<Integer,String> creditCategorySqlGenerator = new HashMap<Integer,String>();
	
	static{	
				
		creditCategorySqlGenerator.put(CreditCategoryUpdateConstants.CREDIT_CATEGORY_UPDATE_DEFAULT_KEY,
				new String("select acc.ban from account acc where acc.state not in ("+AccountStateEnum.INACTIVE_INDEX)+")  and SYSTEMTYPE in (0,2) and acc.responsible='y'  ");
	}
	
	public static Map<Integer, String> getcreditCategorySqlGenerator() {
		return creditCategorySqlGenerator;
	}
}
