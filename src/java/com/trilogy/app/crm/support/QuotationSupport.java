package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bean.DepositDetail;
import com.trilogy.app.crm.bean.DepositItemReference;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ProductsListRow;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.discount.quote.QuoteAccount;
import com.trilogy.app.crm.discount.quote.QuoteDiscountRequest;
import com.trilogy.app.crm.discount.quote.QuoteSubscriber;
import com.trilogy.app.crm.discount.quote.QuoteSubscriberServiceDetail;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.DepositResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.ItemReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.ProductDetails;

public class QuotationSupport 
{
	public static final String MODULE = QuotationSupport.class.getName();
	
	public static DepositItemReference adaptApiItemToDepositReference(Context ctx, ItemReference apiItem) throws CRMExceptionFault
	{
		DepositItemReference reference = new DepositItemReference();
		Subscriber sub = null;
		try{
			sub = SubscriberSupport.getSubscriber(ctx, apiItem.getSubscriptionID());
		} 
		catch (HomeException e){
			LogSupport.minor(ctx, MODULE, "Exception while looking up for subscriber with id :: "+
								apiItem.getSubscriptionID());
		}
		
		
		if(sub != null)
		{
			reference.setSubscriptionID(apiItem.getSubscriptionID());
			LogSupport.info(ctx, MODULE, "Subscriber not found for id :: "+apiItem.getSubscriptionID());
		}
		
		 
		validateItemReferenceObjects(ctx, apiItem);
		reference.setCorrelationID(apiItem.getCorrelationID());
		reference.setDepositCategory(apiItem.getDepositCategory());
		ProductDetails[] apiProductDetails = apiItem.getProductReference();
		List<ProductsListRow> productIds = new ArrayList<ProductsListRow>();
		for (ProductDetails product : apiProductDetails) 
		{
			ProductsListRow productRow = new ProductsListRow();
			
			productRow.setProductPath(product.getProductPath());
			productRow.setProductID(product.getProductId());
			productIds.add(productRow);
		}
		reference.setProductsListRow(productIds);
		
		Set<Long> subType = new HashSet<Long>();
		subType.add(apiItem.getSubscriptionType());
		reference.setSubscriptionTypeSet(subType);

		return reference;
		
	}
	
	public static QuoteDiscountRequest adaptApiItemToDiscountReference(Context ctx, Integer spid, String ban, int[] operations, long accountType, String discountScope, 
			String discountGrade, long creditCategory, ItemReference[] itemReference) throws CRMExceptionFault
	{
		
		for (ItemReference item : itemReference) 
		{
			validateItemReferenceObjects(ctx, item);
		}
		
		QuoteDiscountRequest reference = new QuoteDiscountRequest();
		
		List<QuoteAccount> requestedAccounts = new ArrayList<QuoteAccount>();
		QuoteAccount account = new QuoteAccount();
		account.setSpid(spid);
		account.setBan(ban);
		account.setAccountType(accountType);
		account.setCreditCategory(creditCategory);
		account.setDiscountGrade(discountGrade);
		account.setRootBan(discountScope);
		requestedAccounts.add(account);
		reference.setRequestedAccounts(requestedAccounts);
		
		List<QuoteSubscriber> requestedSubscribers = new ArrayList<QuoteSubscriber>();
		
		for (ItemReference apiItem : itemReference) {
			QuoteSubscriber subscriber = new QuoteSubscriber();
			
			subscriber.setCorrelationId(apiItem.getCorrelationID());
			subscriber.setSubscriberId(apiItem.getSubscriptionID());
			subscriber.setSubscriptionType(apiItem.getSubscriptionType());
			subscriber.setPriceplanId(apiItem.getOfferId());
			subscriber.setItems(Arrays.asList(adaptServiceDetails(ctx,apiItem)));
			subscriber.setActionType(apiItem.getActionType());
			requestedSubscribers.add(subscriber);
		}
		
		reference.setRequestedSubscribers(requestedSubscribers);
		
		return reference;
	}
	
	public static QuoteSubscriberServiceDetail[] adaptServiceDetails(Context ctx,ItemReference apiItem)
	{
		QuoteSubscriberServiceDetail[] details = new QuoteSubscriberServiceDetail[apiItem.getProductReference().length];
		
		ProductDetails[] apiProductDetails = apiItem.getProductReference();
		
		int i=0;
		for (ProductDetails productDetails : apiProductDetails) {
			QuoteSubscriberServiceDetail serviceDetail = new QuoteSubscriberServiceDetail();
			serviceDetail.setServiceId(productDetails.getProductId());
			serviceDetail.setCharge(productDetails.getCharge());
			serviceDetail.setPath(productDetails.getProductPath());
			details[i++] = serviceDetail;
		}
		
		return details;
	}
	
	private static void validateItemReferenceObjects(Context ctx, ItemReference apiItem) throws CRMExceptionFault
	{
		PricePlan priceplan = null;
		long pp = apiItem.getOfferId();
		
		long subType = apiItem.getSubscriptionType(); 

		
    	/**
    	 * Validate Priceplan existance
    	 */
    	try{
    		 priceplan = PricePlanSupport.getPlan(ctx,pp);
    	}catch(HomeException e){
    		throw new CRMExceptionFault("Problem occurred while fetching priceplan for id : "+pp);
    	}
    	if(null == priceplan){
    		throw new CRMExceptionFault("priceplan does not exist with id : "+pp);
    	}
    	
    	
    	/**
    	 * Validate Subscription type
    	 */
    	if((Arrays.asList(SubscriptionTypeEnum.COLLECTION)).contains(subType)){
    		throw new CRMExceptionFault("Subscription Type does not exist with id : "+subType);
    	}
    	
		
	}

	public static DepositResult adaptCrmToApiDepositResult(Context ctx, DepositDetail bssDetail, DepositResult apiResult)
	{
		apiResult.setDepositType(Long.toString(bssDetail.getDepositTypeId()));
		apiResult.setProductId(bssDetail.getProductId());
		apiResult.setAmount(bssDetail.getAmount());
		apiResult.setProductPath(bssDetail.getProductPath());
		return apiResult;
	}
}
