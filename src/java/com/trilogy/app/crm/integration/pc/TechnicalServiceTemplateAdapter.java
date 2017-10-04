package com.trilogy.app.crm.integration.pc;

import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.ErrorMessageList_type0;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.MessageType;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NonVersionEntityStateChangeInput;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.PcomFaultInfo;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreateTechnicalServiceResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.ReleaseTechnicalServiceResponse;
import com.trilogy.app.crm.integration.pc.TechnicalServiceTemplateSupport;

/**
 * @author Subhasmita Mishra
 * @since 10.3.4
 *
 */

public class TechnicalServiceTemplateAdapter {

	public static RfssIOSave adapt(final Context ctx,
			final TechnicalServiceTemplate techService, RfssIOSave rfss)
			throws Exception {
		
		LogSupport.debug(ctx, "TechnicalServiceTemplateAdapter", "[TechnicalServiceTemplateAdapter.adapt] printing TechnicalServiceTemplate.getID:"+techService.getID());
		
		return TechnicalServiceTemplateSupport.technicalServiceName(ctx, techService, rfss);
	}

	public static Object unAdapt(final Context ctx,
			CreateTechnicalServiceResponse createTechnicalServiceResponse) {
		
		LogSupport.debug(ctx, "TechnicalServiceTemplateAdapter", "[TechnicalServiceTemplateAdapter.unAdapt] Start");
		
		return createTechnicalServiceResponse.getStatus().getValue();
	}

	public static NonVersionEntityStateChangeInput adaptState(
			final Context ctx, final TechnicalServiceTemplate techService,
			NonVersionEntityStateChangeInput nvesciObj) throws HomeException {
		
		LogSupport.debug(ctx, "TechnicalServiceTemplateAdapter", "[TechnicalServiceTemplateAdapter.adaptState] printing TechnicalServiceTemplate.getID:"+techService.getID());
		
		return TechnicalServiceTemplateSupport.releaseTechnicalService( techService, nvesciObj);
	}

	public static Object unAdaptState(
			ReleaseTechnicalServiceResponse rtsresponse) {
		
		return rtsresponse.getStatus().getValue();
	}

	public static Object unAdaptError(final Context ctx,
			CreateTechnicalServiceResponse createTechnicalServiceResponse) {
		
		boolean debugEnabled = LogSupport.isDebugEnabled(ctx);
		if (debugEnabled) { 
			LogSupport.debug(ctx, "TechnicalServiceTemplateAdapter", "[TechnicalServiceTemplateAdapter.unAdaptError] Start");
		}
		
		String response = "";
		PcomFaultInfo faultInfo = new PcomFaultInfo();
		ErrorMessageList_type0 errMsgList = new ErrorMessageList_type0();
		faultInfo = createTechnicalServiceResponse.getFaultInfo();
		errMsgList = faultInfo.getErrorMessageList();
		MessageType[] localMessage = errMsgList.getMessage();
		
		for (int i = 0; i < localMessage.length; i++) {
			response = localMessage[i].getString();
			if (debugEnabled) {
				LogSupport.debug(ctx, "TechnicalServiceTemplateAdapter",
						"[TechnicalServiceTemplateAdapter.unAdaptError] failure response: "
								+ response);
			}
		}

		LogSupport.info(ctx, "TechnicalServiceTemplateAdapter",
				"[TechnicalServiceTemplateAdapter.unAdaptError] final response: " + response);
		
		return response;
		
	}

}
