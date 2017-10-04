package com.trilogy.app.crm.subscriber.charge.handler;

import java.text.MessageFormat;

import com.trilogy.app.crm.api.rmi.support.ApiOptionsUpdateResultSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.BundleProfile;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.support.ChargeRefundResultHandlerSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.s2100.ErrorCode;

public class LoggingHandler
extends GenericHandler
{
    
    public LoggingHandler(ChargeRefundResultHandler handler)
    {
        delegate_ = handler;
    }

    public LoggingHandler()
    {
        
    }
     
    
    public void handleError(Context ctx,  ChargableItemResult ret)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Unable to ");
        sb.append(ACTION_TYPE_NAMES.get(new Integer(ret.getAction())));
        sb.append(" to subscriber '"); 
        sb.append(ret.getSubscriber().getId()); 
        sb.append("' for ");
        sb.append(CHARGABLE_ITEM_NAMES[ret.getChargableItemType()]);
        sb.append(" '"); 
        sb.append(ret.getId());
        
        //boolean restrictProvisioning = GenericHandler.isItemMarkedForProvisioningRestriction(ret);
        
        long chargableItemId = -1;
        
        switch (ret.getChargableItemType())
        {
            case CHARGABLE_ITEM_PACKAGE:
                ServicePackage servicePackage = (ServicePackage) ret.getChargableObjectRef();
                if (servicePackage!=null)
                {
                    sb.append(" - ");
                    sb.append(servicePackage.getName());
                }
                break;
                
            case CHARGABLE_ITEM_BUNDLE:
                BundleProfile bundle = (BundleProfile) ret.getChargableObjectRef();
                if (bundle!=null)
                {
                    sb.append(" - ");
                    sb.append(bundle.getName());
                    chargableItemId = bundle.getBundleId();
                }
                break;
                
            case CHARGABLE_ITEM_SERVICE:
                Service service = (Service) ret.getChargableObjectRef();
                if (service!=null)
                {
                    sb.append(" - ");
                    sb.append(service.getName());
                    chargableItemId = service.getIdentifier();
                }
                break;
            
            case CHARGABLE_ITEM_AUX_SERVICE:
                AuxiliaryService auxService = (AuxiliaryService) ret.getChargableObject();
                if (auxService!=null)
                {
                    sb.append(" - ");
                    sb.append(auxService.getName());
                    chargableItemId = auxService.getIdentifier();
                }
                break;
        }
        
        String errorMessage = (null == ret.getThrownObject())?null:ret.getThrownObject().getMessage();
        
        if (ret.getChargeResult() == TRANSACTION_FAIL_OCG && ret.getThrownObject() instanceof OcgTransactionException &&
                (((OcgTransactionException) ret.getThrownObject()).getErrorCode() == ErrorCode.BALANCE_INSUFFICIENT ||
                 ((OcgTransactionException) ret.getThrownObject()).getErrorCode() == ErrorCode.BALANCE_DEDUCT_FAILED ||
                 ((OcgTransactionException) ret.getThrownObject()).getErrorCode() == ErrorCode.NOT_ENOUGH_BAL ||
                 ((OcgTransactionException) ret.getThrownObject()).getErrorCode() == ErrorCode.LOW_BALANCE ||
                 ((OcgTransactionException) ret.getThrownObject()).getErrorCode() == ErrorCode.SUCCESS_NO_BALANCE)
                ) 
        {
            sb.append(" due to insufficient funds"); 
        }
        else if (null != errorMessage && !errorMessage.isEmpty())
        {
            sb.append(errorMessage);
        }
        else
        {
            sb.append("': ");
            sb.append(TRANSACTION_RETURN_NAMES.get( new Integer(ret.getChargeResult()))); 
        }        

        if (
                // !restrictProvisioning && 
                ret.subscriber.isPrepaid()
                && ret.getChargeResult() == TRANSACTION_FAIL_OCG)
        {
            sb.append(". ");
            sb.append(CHARGABLE_ITEM_NAMES[ret.getChargableItemType()]);
            sb.append(" not provisioned or suspended");
        }        
        
        ChargeRefundResultHandlerSupport.logErroMsg(ctx, sb.toString(), ret);
        
        
        try
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                String msg = MessageFormat.format(
                        "FOR API-RES-HOLDER :: ChargableItemType: {0}, ChargableItemId: {1} isOCG-error: {2}, OCG-RC: {3}, err-mesg: {4}, txn-result: {5}", 
                            new Object[]{Integer.valueOf(ret.getChargableItemType()), 
                                Long.valueOf(chargableItemId), Boolean.valueOf(ret.isOCGError()), 
                                        Integer.valueOf(ret.getOCGErrorCode()), ret.getOCGErrorMessage(), 
                                        Integer.valueOf(ret.getChargeResult())});
                LogSupport.debug(ctx, this, msg);
            }
            
            if(ApiOptionsUpdateResultSupport.isInstalledApiResultSetInContext(ctx))
            {
                if(ret.isOCGError())
                {
                    ApiOptionsUpdateResultSupport.setOCGErrorCode(ctx, chargableItemId, 
                            ret.getChargableItemType(), ret.getOCGErrorCode());
                    ApiOptionsUpdateResultSupport.setApiErrorMessage(ctx, chargableItemId, 
                            ret.getChargableItemType(), ret.getOCGErrorMessage());
                    ApiOptionsUpdateResultSupport.setApiErrorCode(ctx, chargableItemId, 
                            ret.getChargableItemType(), ret.getChargeResult());
                }
                else
                {
                    //We are here means it was *not* "ChargingConstants.TRANSACTION_SUCCESS"
                    ApiOptionsUpdateResultSupport.setOCGErrorCode(ctx, chargableItemId, 
                            ret.getChargableItemType(), 0);
                    ApiOptionsUpdateResultSupport.setApiErrorMessage(ctx, chargableItemId, 
                            ret.getChargableItemType(), sb.toString());
                    ApiOptionsUpdateResultSupport.setApiErrorCode(ctx, chargableItemId, 
                            ret.getChargableItemType(), ret.getChargeResult());
                }
            }
            else
            {
                if(LogSupport.isDebugEnabled(ctx))
                    LogSupport.debug(ctx, this, 
                            "ApiResultHolder not installed in the context.");
            }
        } 
        catch (Throwable e)
        {
            if(LogSupport.isDebugEnabled(ctx))
                LogSupport.debug(ctx, this, 
                        "ApiResultHolder installed, but got exception while setting result-code", e);
        }
    }

    public void handleSuccess(Context ctx,  ChargableItemResult ret)
    {
    	// short cut, 
    	if (ret.isHistoryCreated())
    	{	
    		ChargeRefundResultHandlerSupport.createSubscriptionHistory(ctx, ret); 
    	}
     	ChargeRefundResultHandlerSupport.logDebugMsg(ctx, ret); 
    }
    
    
    
    
}
