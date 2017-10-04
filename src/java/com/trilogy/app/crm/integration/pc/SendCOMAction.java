package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.bean.ui.PricingTemplate;
import com.trilogy.app.crm.bean.ui.PricingTemplateXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PcService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.RequestContext;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIO;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreatePriceTemplateResponse;

import java.security.Permission;

import javax.servlet.ServletException;

public class SendCOMAction extends SimpleWebAction {
	private static final long serialVersionUID = 1821883077892950324L;

	public SendCOMAction() {
		super("sendtocom", "SendToCom");
		this.defaultHelpText_ = "send current bean to com";
	}

	public SendCOMAction(Permission permission) {
		this();
		setPermission(permission);
	}

	public void writeLink(Context ctx, PrintWriter out, Object bean, Link link) {

		LogSupport.debug(ctx, this, "[SendCOMAction.writeLink] Start");
		
		link = modifyLink(ctx, bean, link);
		link.add("cmd", "ComPricingTemplate");
		link.add("CMD", "SendToCom");

		out.print("<a href=\"");
		link.write(out);
		out.print("\" onclick=\"try{return confirm('Proceed with send request to com of ");
		out.print(XBeans.getIdentifier(bean));
		out.print("?');}catch(everything){}\">");
		out.print(getLabel());
		out.print("</a>");
	}

	public void execute(Context ctx) throws AgentException {

		LogSupport.info(ctx, this, "[SendCOMAction.execute] Creating and sending pricing template");
		
		String cmd = WebAgents.getParameter(ctx, "cmd");
		String stringKey = WebAgents.getParameter(ctx, "key");

		if (getKey().equals(WebAgents.getParameter(ctx, "action"))) {

			PricingTemplate pricingTemplate = null;
			
			try {
				And filter = new And();
				filter.add(new EQ(PricingTemplateXInfo.ID, Integer
						.parseInt(stringKey)));
				pricingTemplate = HomeSupportHelper
						.get(ctx)
						.findBean(ctx,PricingTemplate.class, filter);
			} catch (Exception e) {
				LogSupport.minor(ctx, this, "SendCOMAction: Can not find PricingTemplate "
											+ e.getMessage());
			}

			String response = createPricingTemlateOnCOM(ctx, pricingTemplate);
			// LogSupport.info(ctx, this, "Pricing Template Response: "+
			// response);

			WebAgents.getWriter(ctx).println(
					"<font color=\"green\">" + stringKey
							+ " has been send successfully - Response: "
							+ response + "</font><br /><br />");
		}

		Link link = new Link(ctx);
		link.add("cmd", cmd);

		try {
			WebAgents.service(ctx, link.write(), WebAgents.getWriter(ctx));
		} catch (ServletException ex) {
			throw new AgentException("fail to redirect to " + cmd, ex);
		} catch (IOException ioEx) {
			throw new AgentException("fail to redirect to " + cmd, ioEx);
		}
	}

	private String createPricingTemlateOnCOM(Context ctx,
			PricingTemplate pricingtemplate) {

		LogSupport.info(ctx, this, "[CreatePricingTemlateOnCOM] Creating Pricing Template on COM");
		
		String result = "null";

		if (pricingtemplate != null && ctx != null) {
			
			PcService client = (PcService) ctx.get(PCConstants.PC_SOAP_CLIENT);// get client from context
			
			if (client != null) {
				RequestContext requestContext = null;
				try {
					PriceTemplateIO priceTemplateIO = ProductCatalogAdapter
														.createPriceTemplateAdapter(pricingtemplate);
					/*
					 * CreatePriceTemplateResponse createPriceTemplateResponseE
					 * = client.createPriceTemplate(priceTemplateIO,
					 * requestContext);
					 */
					// remove this: CreatePriceTemplateResponse createPriceTemplateResponseE =
					/*
					 * result = ProductCatalogAdapter.unAdpatPricingTemlate(createPriceTemplateResponseE);
					 */
					
					LogSupport.info(ctx, this, "[CreatePricingTemlateOnCOM] Pricing Template Response: " + result);

				} catch (Exception e) {
					LogSupport.major(ctx, this,
							"Error in Pc Service: " + e.getMessage());
				}
			} else {
				LogSupport
						.major(ctx, this, "Can not find PCService in context");
			}
		}

		return result;
	}
}
