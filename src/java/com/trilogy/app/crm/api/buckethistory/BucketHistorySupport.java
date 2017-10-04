package com.trilogy.app.crm.api.buckethistory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.api.rmi.impl.SubscribersImpl;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.BucketHistory;
import com.trilogy.app.crm.bean.BucketHistoryXInfo;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.elang.PagingXStatement;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.subscription.SubscriptionBundleUnitTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustmentOperationType;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.BundleAdjustmentOperationTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.BucketHistoryReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.DetailedBucketHistoryQueryResult;

/**
 * 
 * @author abhurke
 *
 */
public final class BucketHistorySupport {

	
	public static final BucketHistoryQueryResult createBucketHistoryQueryResult(String pageKey) {
		
		BucketHistoryQueryResult result = new BucketHistoryQueryResult();
		
		result.setPageKey(pageKey);
		
		return result;
	}
	
	public static final DetailedBucketHistoryQueryResult createDetailedBucketHistoryQueryResult(String pageKey) {
		
		DetailedBucketHistoryQueryResult result = new DetailedBucketHistoryQueryResult();
		
		result.setPageKey(pageKey);
		
		return result;
	}	
	
	private static final BucketHistoryQueryResult addBucketHistoryReferenceToResult(BucketHistoryQueryResult result
			,Long id , Calendar adjDate , int spid , String accID , String subID , Long bundleID , 
			Integer bundleCategoryID , Long amount , Long balance , UnitTypeEnum unitType ,
			Calendar expiry , String externalRef ) {
		
		BucketHistoryReference ref = new BucketHistoryReference();
		ref.setIdentifier(id);
		ref.setDate(adjDate);
		ref.setSpid(spid);
		ref.setAccountID(accID);
		ref.setSubscriptionID(subID);
		ref.setBundleID(bundleID);
		ref.setBundleCategoryID(bundleCategoryID);
		if(amount > 0) {
			ref.setOperation(BundleAdjustmentOperationTypeEnum.INCREMENT.getValue());
		} else {
			ref.setOperation(BundleAdjustmentOperationTypeEnum.DECREMENT.getValue());
		}
		ref.setAmount(Math.abs(amount));
		ref.setBalance(Math.abs(balance));
		ref.setExpiryTime(expiry);
		ref.setReference(externalRef);
		ref.setUnitType((unitType == null) ? null : SubscriptionBundleUnitTypeEnum.valueOf(unitType.getIndex()));
		
		result.addReferences(ref);
		
		return result;
	}
	
	private static final DetailedBucketHistoryQueryResult addBucketHistoryToResult(DetailedBucketHistoryQueryResult result
			,Long id , Calendar adjDate , int spid , String accID , String subID , Long bundleID , 
			Integer bundleCategoryID , Long amount , Long balance , UnitTypeEnum unitType ,
			Calendar expiry , String externalRef , long bucketID, int expiryExtension , int adjustmentType) {
		
		com.redknee.util.crmapi.wsdl.v3_0.types.subscription.BucketHistory ref = new com.redknee.util.crmapi.wsdl.v3_0.types.subscription.BucketHistory();

		ref.setIdentifier(id);
		ref.setDate(adjDate);
		ref.setSpid(spid);
		ref.setAccountID(accID);
		ref.setSubscriptionID(subID);
		ref.setBundleID(bundleID);
		ref.setBundleCategoryID(bundleCategoryID);
		if(amount > 0) {
			ref.setOperation(BundleAdjustmentOperationTypeEnum.INCREMENT.getValue());
		} else {
			ref.setOperation(BundleAdjustmentOperationTypeEnum.DECREMENT.getValue());
		}
		ref.setAmount(Math.abs(amount));
		ref.setBalance(Math.abs(balance));
		ref.setExpiryTime(expiry);
		ref.setReference(externalRef);
		ref.setUnitType( (unitType == null) ? null : SubscriptionBundleUnitTypeEnum.valueOf(unitType.getIndex()) );
		ref.setAdjustmentType(adjustmentType);
		ref.setExpiryExtension(expiryExtension);
		ref.setBucketID(bucketID);
		
		result.addReferences(ref);
		
		return result;
	}	
	
	
	public static final BucketHistoryQueryResult addBucketHistoryReferenceToResult(BucketHistoryQueryResult result , BucketHistory history) {
				
		return addBucketHistoryReferenceToResult(result, history.getIdentifier(), convertToCalendar(history.getAdjustmentDate()),
				history.getSpid(), history.getBan(), history.getSubscriptionID(), 
				history.getBundleID(),(int) history.getCategoryID(), history.getAmount(),
				history.getBalance(), history.getUnitType(), convertToCalendar(history.getExpiryDate()), history.getExternalReference());
	}
	
	
	public static final DetailedBucketHistoryQueryResult addBucketHistoryToDetailedResult(DetailedBucketHistoryQueryResult result , BucketHistory history) {
		
		return addBucketHistoryToResult(result, history.getIdentifier(), convertToCalendar(history.getAdjustmentDate()),
				history.getSpid(), history.getBan(), history.getSubscriptionID(), 
				history.getBundleID(),(int) history.getCategoryID(), history.getAmount(),
				history.getBalance(), history.getUnitType(), convertToCalendar(history.getExpiryDate()),
				history.getExternalReference() , history.getBucketID() , history.getExpiryExtension() ,
				history.getAdjustmentType());
	}
	

	public static Collection<BucketHistory> getBucketHistoryCollection(Context ctx,
    		String identifier,
    		long bundleID) throws CRMExceptionFault {
		
		And condition = new And();
		
		// subscriber
		condition.add(new EQ(BucketHistoryXInfo.SUBSCRIPTION_ID, identifier));
		
		//bundle ID
		condition.add(new EQ(BucketHistoryXInfo.BUNDLE_ID, bundleID));
	
        Collection<BucketHistory> bucketHistoryCollection = null;
        
        try {
        	bucketHistoryCollection = HomeSupportHelper.get(ctx).getBeans(ctx, BucketHistory.class, condition);
        } catch (Exception e) {
        	RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, "Exception listing BucketHistory", SubscribersImpl.class);
        }
        
		return bucketHistoryCollection;
	}
	
	
	public static Collection<BucketHistory> getBucketHistoryCollection(Context ctx,
    		String identifier,
    		java.util.Calendar startTime,java.util.Calendar endTime,java.lang.Long category,java.lang.String pageKey, int limit,
    		java.lang.Boolean isAscending) throws CRMExceptionFault {
		
		And condition = new And();
		
		// subscriber
		condition.add(new EQ(BucketHistoryXInfo.SUBSCRIPTION_ID, identifier));
		
		// date range
		if(startTime != null) {
			condition.add(new GTE(BucketHistoryXInfo.ADJUSTMENT_DATE, CalendarSupportHelper.get(ctx).calendarToDate(startTime)));
		}
		
		//date range
		if(endTime != null) {
			condition.add(new LTE(BucketHistoryXInfo.ADJUSTMENT_DATE, CalendarSupportHelper.get(ctx).calendarToDate(endTime)));
		}
		
		//adjustment types
        if (category != null)
        {
            com.redknee.app.crm.bean.core.AdjustmentType adjustmentType = null;
            
            try
            {
                final Object conditionAdjTyp = new EQ(AdjustmentTypeXInfo.CODE, category.intValue());
                adjustmentType = HomeSupportHelper.get(ctx).findBean(ctx, com.redknee.app.crm.bean.core.AdjustmentType.class, conditionAdjTyp);
            }
            catch (Exception e)
            {
                final String msg = "Unable to retrieve Adjustment Type " + category;
                RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, msg, SubscribersImpl.class);
            }            
            
            final Set<Integer> set = adjustmentType.getSelfAndDescendantCodes(ctx);
            
            if ( set.size() > 999)
            {
                int i = 0;
                Set codeSet = new HashSet<Integer>();

                for(Integer code : set)
                {
                    codeSet.add(code);
                    if ( i == 998)
                    {
                        condition.add(new In(BucketHistoryXInfo.ADJUSTMENT_TYPE, codeSet));                        
                        i = 0;
                        codeSet = new HashSet<Integer>();
                    }
                    else
                    {
                        i++;
                    }                    
                }
                
            }
            else
            {
                condition.add(new In(BucketHistoryXInfo.ADJUSTMENT_TYPE, set));
            }
        }	
        
        //pagination
        if (pageKey != null)
        {
            condition.add(new PagingXStatement(BucketHistoryXInfo.IDENTIFIER, pageKey, isAscending));
        }        
		
        Collection<BucketHistory> bucketHistoryCollection = null;
        
        try {
        	bucketHistoryCollection = HomeSupportHelper.get(ctx).getBeans(ctx, BucketHistory.class, condition, limit, isAscending);
        } catch (Exception e) {
        	RmiApiErrorHandlingSupport.handleQueryExceptions(ctx, e, "Exception listing BucketHistory", SubscribersImpl.class);
        }
        
		return bucketHistoryCollection;
	}
	
	private static Calendar convertToCalendar(Date input) {
		
		if(input == null ) {
			return null;
		}
		Calendar result = new GregorianCalendar();
		result.setTime(input);
		
		return result;
	}
	
}
