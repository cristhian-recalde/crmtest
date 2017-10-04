package com.trilogy.app.crm.discount;

import com.trilogy.app.crm.bean.DiscountActivityTypeEnum;
import com.trilogy.app.crm.bean.DiscountEvaluationStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;

/**
 * @author harsh.murumkar
 *   This class defines primary filters required when running Discount Class evaluation process.
 */
public class DiscountEventSqlGenerator {
	
	private static DiscountEventSqlGenerator discountingSqlGenerator = new DiscountEventSqlGenerator();
	private static String DISCOUNTING_DEFAULT_FILTER = "DiscountingPrimaryFilter";
	private static String DISCOUNTING_SCOPE_DEFAULT_FILTER = "DiscountingScopePrimaryFilter";
	
	private static String DISCOUTING_RULE_VERSIONING_FILTER="DiscountingRuleVersioningFilter";
	private static String DISCOUNTING_ACCOUNT_REEVALUATION_FILTER = "DiscountingBanReevaluationFilter";
	private static String DISCOUNTING_SCOPE_ACCOUNT_REEVALUATION_FILTER = "DiscountingScopeBanReevaluationFilter";
	
	private static String DISCOUNTING_ACCOUNT_FOR_TRANSACTION_FILTER = "DiscountingBanForTransactionFilter";
	

	static{
		/*
		 * DISCOUNTING_DEFAULT_KEY : Primary Filter.
		 * 
		 */
	    //TODO - write down query specific to fetch data from DiscountingEvent table.
	    DISCOUNTING_DEFAULT_FILTER = new String("select distinct de.ban from discountactivitytrigger de where de.discountEvaluationStatus =" + DiscountEvaluationStatusEnum.PENDING_INDEX +
	    		                                      " and de.discountActivityType not in(" + DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX +"," + DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX+")");
	    //Discounting scope changes
	    DISCOUNTING_SCOPE_DEFAULT_FILTER = new String("select distinct de.targetban from discountactivitytrigger de where de.discountEvaluationStatus =" + DiscountEvaluationStatusEnum.PENDING_INDEX +
                " and de.discountActivityType not in(" + DiscountActivityTypeEnum.SERVICE_FEE_PERSONALIZE_EVENT_INDEX +"," + DiscountActivityTypeEnum.AUX_SERVICE_FEE_PERSONALIZE_EVENT_INDEX+")"); 
	    
	    DISCOUTING_RULE_VERSIONING_FILTER = new String("select * from discountactivitytrigger de where de.discountActivityType="+ DiscountActivityTypeEnum.RULE_VERSIONING_CHANGE_EVENT_INDEX
	    		                                                               +" and de.discountEvaluationStatus=" + DiscountEvaluationStatusEnum.PENDING_INDEX);
	    DISCOUNTING_ACCOUNT_REEVALUATION_FILTER = new String("select distinct ban from discountactivitytrigger");
	    
	    //Discounting scope changes
	    DISCOUNTING_SCOPE_ACCOUNT_REEVALUATION_FILTER = new String("select distinct targetban from discountactivitytrigger");
	    
	    DISCOUNTING_ACCOUNT_FOR_TRANSACTION_FILTER = new String("select distinct ban from discounteventactivity where state = " + DiscountEventActivityStatusEnum.ACTIVE_INDEX 
	    		+ " or state = " + DiscountEventActivityStatusEnum.CANCELLATION_PENDING_INDEX);
	}
	
	public static DiscountEventSqlGenerator getDiscountingSqlGenerator() {
		return discountingSqlGenerator;
	}
	
	public static String getDiscountingFilter() {
        return DISCOUNTING_DEFAULT_FILTER;
    }
	
	public static String getDiscountingScopeFilter() {
        return DISCOUNTING_SCOPE_DEFAULT_FILTER;
    }
	
	public static String getDiscountRuleVersionFilter()
	{
		return DISCOUTING_RULE_VERSIONING_FILTER;
	}
	public static String getDiscountingBanReevaluationFilter()
	{
		return DISCOUNTING_ACCOUNT_REEVALUATION_FILTER;
	}
	//Applicable for the new feature discounting scope on the parent/target ban
	public static String getDiscountingScopeBanReevaluationFilter()
	{
		return DISCOUNTING_SCOPE_ACCOUNT_REEVALUATION_FILTER;
	}
	public static String getDISCOUNTING_ACCOUNT_FOR_TRANSACTION_FILTER() {
		return DISCOUNTING_ACCOUNT_FOR_TRANSACTION_FILTER;
	}
}
