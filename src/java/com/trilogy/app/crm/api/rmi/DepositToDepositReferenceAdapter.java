package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bean.Deposit;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.support.DepositConstants;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.types.depositmanagement.DepositHistoryReference;

//adapter for deposit history api
public class DepositToDepositReferenceAdapter implements Adapter
{
	private static final String MODULE = DepositSupport.class.getName();
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
    	Deposit deposit = (Deposit) obj;
        
    	DepositHistoryReference reference = new DepositHistoryReference();
        reference.setSpid(deposit.getSpid());
        reference.setDepositId(deposit.getDepositID());
        reference.setExternalTransactionId(deposit.getExternalTransactionID());
        reference.setBan(deposit.getBAN());
        reference.setSubscriptionId(deposit.getSubscriptionID());
        reference.setSubscriptionType(deposit.getSubscriptionType());
        reference.setDepositeType(deposit.getDepositType());
        
        reference.setProductId(deposit.getProductID());
        reference.setProductDescription(getServiceName(ctx,deposit.getProductID()));
        
        reference.setAmountHeld(deposit.getAmountHeld());
        reference.setDepositeDate(DepositSupport.dateToCalendarConverter(deposit.getDepositDate()));
        reference.setExpectedReleaseDate(DepositSupport.dateToCalendarConverter(deposit.getExpectedReleaseDate()));
        reference.setReleasedAmount(deposit.getReleasedAmount());
        reference.setInterestReleased(deposit.getInterestReleased());
        reference.setReleaseDate(DepositSupport.dateToCalendarConverter(deposit.getReleaseDate()));
        reference.setStatus(deposit.getStatus().getName());
        reference.setReasonCode(deposit.getReasonCode());
        reference.setReleaseType(deposit.getReleaseType().getName());
        reference.setTransactionId(deposit.getTransactionId());
        reference.setAdjustmentType(deposit.getAdjustmentType());
        reference.setGlCode(deposit.getGlCode());
        reference.setInterestTransactionID(deposit.getInterestTransactionID());
        return reference;
    }
  	
	private String getServiceName(Context ctx, long productID) {
		try {
			if (productID != -1) {
				Service service = com.redknee.app.crm.support.ServiceSupport.getService(ctx, productID);
				if (service != null) {
					return service.getName();
				}
			}
		} catch (HomeException e) {
			LogSupport.minor(ctx, MODULE, "Getting error while fetching deposit details" + e, e);
		}
		return DepositConstants.EMPTY_STRING;
	}

	public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
}
