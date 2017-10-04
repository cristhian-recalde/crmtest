package com.trilogy.app.crm.bas.recharge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.core.ruleengine.DiscountPriorityTypeEnum;
import com.trilogy.app.crm.discount.CombinationDiscountHandler;
import com.trilogy.app.crm.discount.ContractDiscountHandler;
import com.trilogy.app.crm.discount.CrossSubscriptionDiscountHandler;
import com.trilogy.app.crm.discount.DiscountActivityUtils;
import com.trilogy.app.crm.discount.DiscountHandler;
import com.trilogy.app.crm.discount.FirstDeviceDiscountHandler;
import com.trilogy.app.crm.discount.MasterPackDiscountHandler;
import com.trilogy.app.crm.discount.PairedDiscountHandler;
import com.trilogy.app.crm.discount.SecondDeviceDiscountHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Generates discount event transactions for discount events.
 */

public class DiscountEventContextAgent implements ContextAgent {

	
	public DiscountEventContextAgent(
			DiscountEventTransactionVisitor discountEventTransactionVisitor) {
		visitor_ = discountEventTransactionVisitor;
	}

	public void execute(Context context) throws AgentException {
		final String ban = (String) context
				.get(DISCOUNT_TRANSACTION_CHECK_ACCOUNT);

		if (ban != null) {
			try {
				Set<DiscountEventActivityStatusEnum> enumValues = new HashSet<DiscountEventActivityStatusEnum>();
				enumValues.add(DiscountEventActivityStatusEnum.ACTIVE);
				enumValues.add(DiscountEventActivityStatusEnum.CANCELLATION_PENDING);
				
				Account account = AccountSupport.getAccount(context, ban);
				initialize(context, account);
				
				ArrayList<DiscountEventActivity> discountActivityEventList = (ArrayList<DiscountEventActivity>) DiscountActivityUtils.filterDiscountEventActivitybyState(context, enumValues, ban);
				
				
				List<DiscountEventActivity> discountEventListToBeUpdated = new ArrayList<DiscountEventActivity>();
				
				
				// Execute each handler for the discount transaction generation
				for(DiscountHandler handler : discountEvaluationOrder){
       				handler.generateTransactions(context, account, discountActivityEventList,discountEventListToBeUpdated);
				}
				if (LogSupport.isDebugEnabled(context)) {
					LogSupport.debug(
							context,
							MODULE,
							"Discount Transactions are created successfull for the ban : " + ban);
				}
				DiscountActivityUtils.saveDiscountEventActivity(context, discountEventListToBeUpdated);
				

			} catch (HomeException e) {
				throw new AgentException(e);
			}
		}else{
			throw new AgentException("BAN is null");
		}
	}
	
	/**
	 * Initializes the discount evaluation order list. 
	 * This will decide the order in which the discounts are evaluated for any account
	 * @param context
	 * @param account
	 * @return
	 */
	private boolean initialize(final Context context, final Account account){
		CRMSpid crmSpid = null;
		try {
			crmSpid = HomeSupportHelper.get(context).findBean(context,  CRMSpid.class, account.getSpid());

			if(crmSpid!=null) {
				discountEvaluationOrder.clear();
				discountEvaluationOrder.add(new MasterPackDiscountHandler());

			    discountPriorityList = crmSpid.getDiscountPriority();
				for(DiscountPriority discObj : discountPriorityList)
				{
					switch (discObj.getDiscountPriority().getIndex()){
					case DiscountPriorityTypeEnum.CONTRACT_DISCOUNT_INDEX :
						discountEvaluationOrder.add(new ContractDiscountHandler());
						break;
					case DiscountPriorityTypeEnum.CROSS_SUBSCRIPTION_DISCOUNT_INDEX :
						discountEvaluationOrder.add(new CrossSubscriptionDiscountHandler());
						break;
					case DiscountPriorityTypeEnum.FIRST_DEVICE_INDEX :
						discountEvaluationOrder.add(new FirstDeviceDiscountHandler());
						break;
					case DiscountPriorityTypeEnum.PAIRED_DISCOUNT_INDEX :
						discountEvaluationOrder.add(new PairedDiscountHandler());
						break;
					case DiscountPriorityTypeEnum.SECONDARY_DEVICE_DISCOUNT_INDEX :
						discountEvaluationOrder.add(new SecondDeviceDiscountHandler());
						break;
					case DiscountPriorityTypeEnum.COMBINATION_DISCOUNT_INDEX :
						discountEvaluationOrder.add(new CombinationDiscountHandler());
						break;
					default:
						break;
					}
				}
				if(LogSupport.isDebugEnabled(context)){
					LogSupport.debug(context, DiscountEventContextAgent.class.getName(), "Discount Evaluation order for ban["+account.getBAN()+"] is " + discountEvaluationOrder.toString());
				}
			}else{
				LogSupport.minor(context, DiscountEventContextAgent.class.getName(), "Failed to fetch Spid for Ban[" + account.getBAN() +"]");
				addFailedAssignedBAN(account.getBAN());
				return false;
			}
		} catch(HomeException he){
			LogSupport.minor(context, DiscountEventContextAgent.class.getName(), "Failed to fetch Spid discounting configuration for Ban[" + account.getBAN() + "] Exception:" + he);
			addFailedAssignedBAN(account.getBAN());
			return false;
		}

		return true;		
	}


    private synchronized void addFailedAssignedBAN(String BAN)
    {
        this.failedBANs_.add(BAN);
    }
    

    public synchronized List<String> getFailedAssignedBANs()
    {
        return failedBANs_;
    }

	private List<String> failedBANs_ = new ArrayList<String>();
	
	// This list stores the discount handlers in the order configured on SPID level
	private List<DiscountHandler> discountEvaluationOrder = new ArrayList<DiscountHandler>();
	public static List<DiscountPriority> discountPriorityList=null;
	private final DiscountEventTransactionVisitor visitor_;
	public static String DISCOUNT_TRANSACTION_CHECK_ACCOUNT = "discounttransactionaccount";
	public static String MODULE = DiscountEventContextAgent.class.getName();

}
