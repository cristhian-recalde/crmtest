package com.trilogy.app.crm.integration.pc;

import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.ui.PricingTemplate;
import com.trilogy.app.crm.integration.pc.PCConstants;
import com.trilogy.app.crm.integration.pc.ProductCatalogAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PcService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreatePriceTemplateResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.RequestContext;
@SuppressWarnings("serial")
public class PricingTemplateSettingHome extends HomeProxy {

	/**
	 * Creates a new ServiceOneTimeSettingHome.
	 * 
	 * @param delegate
	 *            The home to which we delegate.
	 */
	public PricingTemplateSettingHome(final Home delegate) {
		super(delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object create(final Context ctx, final Object obj)
			throws HomeException {
		
		LogSupport.debug(ctx, this, "[PricingTemplateSettingHome.create] creating Pricing Template");
		
		if (obj instanceof PricingTemplate) {
			final PricingTemplate pricingtemplate = (PricingTemplate) obj;
			// -----------------------
			PcService client = (PcService) ctx.get(PCConstants.PC_SOAP_CLIENT);// get client from context
			if(client != null){
				RequestContext requestContext = null ;
				try {

					PriceTemplateIO priceTemplateIO = ProductCatalogAdapter.createPriceTemplateAdapter(pricingtemplate);
					/*CreatePriceTemplateResponse createPriceTemplateResponseE = client.createPriceTemplate(priceTemplateIO, requestContext);*/
					//remove this:
					CreatePriceTemplateResponse createPriceTemplateResponseE = null;

					
					String result = (String)ProductCatalogAdapter.unAdpatPricingTemlate(createPriceTemplateResponseE);
					LogSupport.info(ctx, this, "[PricingTemplateSettingHome.create] Pricing Template Response: "+result);
					
				} catch (Exception e) {
					LogSupport.major(ctx, this, "Error in Pc Service: "+e.getMessage());
				}
			}else{
				LogSupport.major(ctx, this, "Can not find PCService in context");
				throw new HomeException("Can not find PCService in context");
			}
			
		}
		return super.create(ctx, obj);
	}

}
