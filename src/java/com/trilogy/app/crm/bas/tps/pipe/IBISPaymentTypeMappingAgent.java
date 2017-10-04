/*
 * Created on Nov 2, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bas.tps.PaymentTypeEnum;
import com.trilogy.app.crm.bas.tps.TPSAdjMap;
import com.trilogy.app.crm.bas.tps.TPSAdjMapHome;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * Map TPS payment to CRM payment type
 * @author Larry Xia
 *
 */
public class IBISPaymentTypeMappingAgent extends PipelineAgent {

	public IBISPaymentTypeMappingAgent(ContextAgent delegate)
	{
	   super(delegate);
	}
	
	/**
	* @param ctx
	*           A context
	* @exception AgentException
	*               thrown if one of the services fails to initialize
	*/

	@Override
    public void execute(Context ctx)
	   throws AgentException
	{
		TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class); 
		Account acct = (Account) ctx.get(Account.class);
		try {
			AdjustmentType type = null; 
 
 			if( tps.getPaymentType().equalsIgnoreCase( PaymentTypeEnum.BILL.getDescription())){
					type = findAdjustment(ctx, BILLTYPE_BILL, tps.getVoidFlag() ); 
		
 			} else if ( tps.getPaymentType().equalsIgnoreCase( PaymentTypeEnum.ADEP.getDescription() )){
					type =  findAdjustment(ctx, BILLTYPE_ADEP, tps.getVoidFlag() );
		
 			} else if (tps.getPaymentType().equalsIgnoreCase( PaymentTypeEnum.RDEP.getDescription()) ) {  
					type = findAdjustment(ctx, BILLTYPE_RDEP, tps.getVoidFlag() );
 			} else {
 				throw new Exception( "illegal payment type"); 
 			}
			
			if (type == null)
			{
				throw new Exception ("The adjustment type for " + tps.getPaymentType() + " does noe exist");
			}
			
			ctx.put(AdjustmentType.class, type);
			tps.setAdjType(type); 
		
			pass(ctx, this, "Adjustment type mapped to " + type.getCode());
		} catch ( Exception e ){
			
			ERLogger.genAccountAdjustmentER(ctx, 
				tps, 
				TPSPipeConstant.FAIL_TO_FIND_ADJUST_TYPE);
			
			// send out alarm
			new EntryLogMsg(10534, this, "","", new String[]{"Adjustment type table searching fails"}, e).log(ctx);
			fail(ctx,this, "Lookup adjustment type fails", e, TPSPipeConstant.FAIL_TO_FIND_ADJUST_TYPE); 
			
		}
	}
	
	
	static public AdjustmentType findAdjustment(Context ctx, String key, boolean voidFlag) throws HomeException{
		Home  mapHome = (Home) ctx.get(TPSAdjMapHome.class); 
		TPSAdjMap  map = (TPSAdjMap)mapHome.find(ctx, key); 
		if ( map != null ) {
	       return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx, voidFlag?map.getReverseAdjType():map.getAdjType());
		} else {
			throw new HomeException( "Illegale Adjustment Type"); 
		}

	}
	
	final static String BILLTYPE_BILL = "BIL"; 
	final static String BILLTYPE_ADEP = "ADP";
	final  static String BILLTYPE_RDEP = "RDP"; 
	
}













