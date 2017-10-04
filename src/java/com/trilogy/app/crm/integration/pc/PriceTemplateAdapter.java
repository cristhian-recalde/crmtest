package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
//import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NonVersionEntityStateChangeInput;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreatePriceTemplateResponse;

/**
 * 
 * @author AChatterjee
 *
 */
public class PriceTemplateAdapter {

	public static PriceTemplateIOSave adapt(Context ctx, PriceTemplate priceTemplate, PriceTemplateIOSave priceTemplateIOSave) {
		
		LogSupport.debug(ctx, "PriceTemplateAdapter", "[PriceTemplateAdapter.adapt] printing priceTemplate ID:"+priceTemplate.getID());
		
		return PriceTemplateSupport.getServiceInstance(ctx, priceTemplate, priceTemplateIOSave);
	}

	public static String unAdapt(Context ctx, CreatePriceTemplateResponse response) {
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, "PriceTemplateAdapter", "[PriceTemplateAdapter.unAdapt] printing CreatePriceTemplateResponse:"+response);
		}
		
		return response.getStatus().getValue();		
	}
	
	/*public static NonVersionEntityStateChangeInput adaptState(Context ctx, PriceTemplate priceTemplate,
			NonVersionEntityStateChangeInput nonVersionEntityStateChangeInput) {
		return null;
	}*/

}
