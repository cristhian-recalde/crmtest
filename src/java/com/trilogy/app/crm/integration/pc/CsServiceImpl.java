package com.trilogy.app.crm.integration.pc;



/**
 * This class is implementation of Publish Offer when released from Unified Catalog.
 * 
 * @author dinesh.valsatwar@redknee.com
 */
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.pcom.cs.webservices.messages.v1.PublishOfferingResponse;
import com.trilogy.pcom.cs.webservices.messages.v1.SetOfferingConsumptionStatusResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CsServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.OfferingService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.OfferingServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PcService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.ProductCatalogInterfaceException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.catalog.v1.OfferingSummaryE;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.catalog.v1.SourceSystems;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ExecutionStatus;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.RequestContext;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.offering.v1.OfferingPublicationStatusIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.UpdateOfferingConsumptionStatusResponse;

public class CsServiceImpl implements CsServiceSkeletonInterface ,ContextAware{
	
	public CsServiceImpl(Context context) {
		super();
		this.context_ = context;
	}


	@Override
	public SetOfferingConsumptionStatusResponse setOfferingConsumptionStatus(
			String businessKey, SourceSystems sourceSystem,
			ExecutionStatus executionStatus, String messageID,
			String errorMessage, RequestContext context) {
		
		return null;
	}

	@Override
	public PublishOfferingResponse publishOffering(String messageID0,
			OfferingIO offering) {
		if (LogSupport.isDebugEnabled(context_))
		LogSupport.debug(getContext(), MODULE, "CsServiceImpl-publishOffering started");
		Context subCtx = getContext().createSubContext();
		
		PublishOfferingResponse response = new PublishOfferingResponse();
		OfferingSummaryE summary = new OfferingSummaryE();
		response.setOfferingSummary(summary);
		
		OfferingServiceSkeletonInterface offeringService = new OfferingServiceImpl(subCtx);
		offeringService.prepareOffering(messageID0, offering);
		
		// If prepare Offer is successful then call enableOffering()
		// Retrieve the business Key
		String businessKey = offering.getBusinessKey();
		try {
			offeringService.enableOffering(businessKey,messageID0);
			// If prepareOffering and enableOffering is successful then call API on catalog of confirmation
			
			PcService client = (PcService) subCtx.get(PCConstants.PC_SOAP_CLIENT);
			if (client != null)		
			
			{
				//ExecutionStatus status = new ExecutionStatus();
			    String errorMessage = "";
			    RequestContext rc = new RequestContext();
			    rc.setBusinessKey(businessKey);		
			    LogSupport.debug(getContext(), MODULE, "CsServiceImpl- invoking updateOfferingConsumptionStatus on Catalog");
				//client.updateOfferingConsumptionStatus(businessKey, ExecutionStatus.SUCCESS, errorMessage, rc);
			    OfferingPublicationStatusIO offeringPublicationStatus = new OfferingPublicationStatusIO();
			    offeringPublicationStatus.setBusinessKey(businessKey);
			    offeringPublicationStatus.setStatus("SUCCESS");
			    offeringPublicationStatus.setErrorMessage("");
			    UpdateOfferingConsumptionStatusResponse updateOfferingResponse = client.updateOfferingConsumptionStatus(offeringPublicationStatus, rc);
			}
			else
			{
				
				String msg = PCConstants.PC_SOAP_CLIENT
						+ " is not configured in BSS Please configure the client";
				new MajorLogMsg(this, "PcService client is null " +msg).log(subCtx);
				//throw new HomeException(msg);
			}
		} catch (ProductCatalogInterfaceException e) {
			LogSupport.major(subCtx, MODULE, "Failed completing Offering "+e);
//			response.setOfferingSummary(summary);
			
			
			e.printStackTrace();
		}catch (Exception e) {
			LogSupport.major(subCtx, MODULE, "Failed to completing Offering "+e);
//			response.setOfferingSummary(summary);			
			//e.printStackTrace();
		}
		
		
		response.setMessageID(messageID0);
		summary.setSpid(offering.getSpid());
		summary.setBusinessKey(businessKey);
		
		if (LogSupport.isDebugEnabled(context_))
			LogSupport.debug(getContext(), MODULE, "CsServiceImpl-publishOffering completed");
		return response;
	}

	@Override
	public Context getContext() {
		return this.context_;
	}

	@Override
	public void setContext(Context ctx) {
		this.context_ = ctx;
	}

	
	private Context context_ = ContextLocator.locate();
	public static String MODULE = CsServiceImpl.class.getName();

}
