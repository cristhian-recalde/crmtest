package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.customize.VPNGroupLeaderTransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.customize.VPNGroupMemberTransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;

/**
 * VPNGroupChargeHandler will handle group charge for VPN auxiliary service. 
 * @author lxia
 *
 */
public class VPNGroupChargeHandler 
implements ChargeRefundResultHandler, ChargingConstants
{

    
    public void handleTransaction(Context ctx,  ChargableItemResult ret)
    {
       	try
       	{
       		handleVPNTransaction(ctx, ret); 
       	} catch (HomeException e)
       	{
       		ret.setChargeResult(TRANSACTION_FAIL_DATA_ERROR); 
       		ret.thrownObject = e; 
       	}
        	
    
    }    
    
 
    public void handleVPNTransaction(Context ctx,ChargableItemResult ret)
    throws HomeException
    {
          AuxiliaryService service = (AuxiliaryService)ret.chargableObject; 

       	  final Subscriber groupLeader = service.getGroupLeaderForCharging(ctx, ret.getSubscriber());
 
       	  if (groupLeader == null || ret.getSubscriber().getId().equals(groupLeader.getId()))
       	  {
       		  createChargeForLeader(ctx, ret); 
       	  } else 
       	  {
       		  createChargeForMember(ctx, ret, groupLeader); 
       	  }  
    }
    
    
    public void createChargeForLeader(Context ctx,ChargableItemResult ret)
    {
    	VPNGroupLeaderTransactionCustomize ownerCust = new VPNGroupLeaderTransactionCustomize((AuxiliaryService) ret.getChargableObject()); 
     	
		AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret, null, null, ownerCust, null);  	  					
    }
    
    
    
    public void createChargeForMember(Context ctx,ChargableItemResult ret, Subscriber leader)
    {
    	VPNGroupMemberTransactionCustomize ownerCust = new VPNGroupMemberTransactionCustomize( leader); 
 		AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret, null, null, ownerCust, null);
    }

         
    public void setDelegate(ChargeRefundResultHandler handler)
    {
    
    }
    
	
    public static VPNGroupChargeHandler instance()
    {
        return INSTANCE;
    }

    private static final VPNGroupChargeHandler INSTANCE = new VPNGroupChargeHandler();
}    