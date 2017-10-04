package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.Permission;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.trilogy.app.crm.bean.ui.PriceTemplate;
import com.trilogy.app.crm.bean.ui.PriceTemplateXInfo;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.app.crm.bean.TemplateStateEnum;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PcService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NonVersionEntityStateChangeInput;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.RequestContext;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.price.v1.PriceTemplateIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreatePriceTemplateResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreateTechnicalServiceResponse;

/**
 * 
 * @author AChatterjee
 *
 */
public class ReleasePriceTemplateToCOMAction extends SimpleWebAction {

	private static final long serialVersionUID = 1L;
	private boolean errorFromUCStatus;
	public ReleasePriceTemplateToCOMAction() {

		super("releasetocom", "ReleasePriceTemplateToCOM");
		this.defaultHelpText_ = "Send PriceTemplate to COM";

	}

	public ReleasePriceTemplateToCOMAction(Permission permission) {
		this();
		setPermission(permission);
	}

	public void writeLink(Context ctx, PrintWriter out, Object bean, Link link) {

		LogSupport.debug(ctx, this, "[ReleasePriceTemplateToCOMAction.writeLink] Start");
		
		link = modifyLink(ctx, bean, link);
		// link.add("cmd", "TechnicalServiceTemplate1");
		link.add("CMD", "ReleasePriceTemplateToCOM");
		// LogSupport.info(ctx, this, "FINDME: inside writeLink() ");

		out.print("<a href=\"");
		link.write(out);
		out.print("\" onclick=\"try{return confirm('Proceed with release request to com of ");
		out.print(XBeans.getIdentifier(bean));
		out.print("?');}catch(everything){}\">");
		out.print(getLabel());
		out.print("</a>");
	}
	
	public void execute(Context ctx) throws AgentException {
		
		LogSupport.info(ctx, this, "[ReleasePriceTemplateToCOMAction.execute] Creating and Releasing Price Template on COM");

		String cmd = WebAgents.getParameter(ctx, "cmd");
		String action = WebAgents.getParameter(ctx, "action");
		String stringKey = WebAgents.getParameter(ctx, "key");
		final PMLogMsg pmLog = new PMLogMsg("AppCrm",
				"PriceTemplate Release");
		if (getKey().equals(action)) {
			PriceTemplate priceTemplate = null;
			try {
				And filter = new And();
				filter.add(new EQ(PriceTemplateXInfo.ID, Integer.valueOf(Integer.parseInt(stringKey))));
				priceTemplate = (PriceTemplate) HomeSupportHelper.get(ctx).findBean(ctx, PriceTemplate.class, filter);
			} catch (Exception e) {
				LogSupport.minor(ctx, this,"Cannot find PriceTemplate " + e);
			}
			if (priceTemplate.getTemplateState().equals(TemplateStateEnum.DRAFT)) {
				String response = releasePriceTemplateToCOM(ctx, priceTemplate);
				LogSupport.info(ctx, this, "[ReleasePriceTemplateToCOMAction.execute] Price Template Response: " + response);
				if (response.equals(PCConstants.STATUS_SUCCESS)) {
					WebAgents.getWriter(ctx).println(
									"<font color=\"green\">"
											+ " Price Template with Id: "
											+ stringKey
											+ " has been sent to Unified Catalog - Response: "
											+ response + "</font><br/><br/>");
					com.redknee.app.crm.log.ERLogger.genrateERPriceTemplate(ctx,PCConstants.STATUS_SUCCESS, priceTemplate);
				} else {
					/*WebAgents.getWriter(ctx).println(
									"<font color=\"red\">"
											+ " Price Template with Id: "
											+ stringKey
											+ " has been sent to Unified Catalog - Response: "
											+ response + "</font><br/><br/>");
					com.redknee.app.crm.log.ERLogger.genrateERPriceTemplate(ctx, "FAIL", priceTemplate);*/
					if(errorFromUCStatus){
						WebAgents.getWriter(ctx).println(
								"<font color=\"red\">"
								+ " Price Template with Id: "
								+ stringKey
								+ " has been sent to Unified Catalog - Response: "
								+ response + "</font><br/><br/>");
						com.redknee.app.crm.log.ERLogger.genrateERPriceTemplate(ctx, PCConstants.STATUS_FAIL, priceTemplate);
					}else{
						WebAgents.getWriter(ctx).println(
								"<font color=\"red\">"
								+ " Failed to send Price Template with Id: "
								+ stringKey
								+ "  to Unified Catalog - Response: "
								+ response + "</font><br/><br/>");
						com.redknee.app.crm.log.ERLogger.genrateERPriceTemplate(ctx, PCConstants.STATUS_FAIL, priceTemplate);
					}
				}
			} else {
				WebAgents.getWriter(ctx).println("<font color=\"red\">This Price Template already in Released state</font><br /><br />");
			}
		}
		Link link = new Link(ctx);
		link.add("cmd", cmd);
		try {
			WebAgents.service(ctx, link.write(), WebAgents.getWriter(ctx));
		} catch (ServletException ex) {
			throw new AgentException("fail to redirect to " + cmd, ex);
		} catch (IOException ioEx) {
			throw new AgentException("fail to redirect to " + cmd, ioEx);
		} finally {
			pmLog.log(ctx);
		}
	}

	private String releasePriceTemplateToCOM(Context ctx, PriceTemplate priceTemplate) {

		LogSupport.info(ctx, this, "[releasePriceTemplateToCOM] Releasing Price Template to COM");
		
		String result = "";
		CreatePriceTemplateResponse response = new CreatePriceTemplateResponse();
		boolean isError=false;

		if ((priceTemplate != null) && (ctx != null)) {

			PcService client = (PcService) ctx.get(PCConstants.PC_SOAP_CLIENT);
			RequestContext requestContext = null;
			
			try {
				PriceTemplateIOSave priceTemplateIOSave = PriceTemplateAdapter.adapt(ctx, priceTemplate, new PriceTemplateIOSave());
				//NonVersionEntityStateChangeInput nvesciObj = PriceTemplateAdapter.adaptState(ctx, priceTemplate, new NonVersionEntityStateChangeInput());

				if (client == null) {
					new MajorLogMsg(this, "PcService client is null ").log(ctx);
					result = PCConstants.PC_SOAP_CLIENT + " is not configured in BSS Please configure the client.";
					return result;
				} else {
					LogSupport.info(ctx, this, "[releasePriceTemplateToCOM] Creating the Price Template");
					response = client.createPriceTemplate(priceTemplateIOSave, requestContext);
					result = (String) PriceTemplateAdapter.unAdapt(ctx, response);
					//LogSupport.info(ctx, this, "Creating the Price Template, Create Price Template Response: " + result);

					if (result.equals(PCConstants.STATUS_SUCCESS)) {
						LogSupport.info(ctx, this, "[releasePriceTemplateToCOM] Releasing the Price Template, Release Price Template Response: " + result);
						if (result.equals(PCConstants.STATUS_SUCCESS)) {
							priceTemplate.setTemplateState(TemplateStateEnum.RELEASED);
							HomeSupportHelper.get(ctx).storeBean(ctx, priceTemplate);
							LogSupport.info(ctx, this, "TemplateId: [" + priceTemplate.getID() + "] Price Template State change succesfully");

						}
					}
				}
			} catch (Exception e) {
				LogSupport.major(ctx, this,
						"Failed to create Price Template on COM: " + e.getClass().getCanonicalName(), e);
				
				if (e instanceof java.net.UnknownHostException) {
					isError = true;
					result = "Host is unknown please check the host";
					LogSupport.major(ctx, this,
							"Host is unknown please check the host : " + e, e);
				}
				else if(e instanceof org.apache.axis2.AxisFault){
						if(((org.apache.axis2.AxisFault) e).getFaultCode()==null)
						{
							isError = true;
							result = "Unable to connect host";
							LogSupport.major(ctx, this,
									"Unable to connect host : " + e, e);
						}else if(e.getMessage().contains("Password")||e.getMessage().contains("User ID")){
							isError = true;
							result = "User id or Password error";
							LogSupport.major(ctx, this,
									"User id or Password error : " + e, e);
						}else if(e.getMessage().contains("port")){
							isError = true;
							result = "port out of range";
							LogSupport.major(ctx, this,
									"port out of range : " + e, e);
						}
				}
				else if (e instanceof java.net.ConnectException) {
					isError = true;
					result = "Unable to connect host";
					LogSupport.major(ctx, this,
							"Unable to connect host : " + e, e);
				}
				
				else if (e instanceof IOException) {
						isError = true;
						result = "Unable to connect host";
						LogSupport.major(ctx, this,
								"Unable to connect host : " + e, e);	
				}
				if (!isError) {
					try {
						AxisFault axisFault = (AxisFault) e;
						OMElement oMElement = axisFault.getDetail();
						if(oMElement!=null){
							result = getCOMError(ctx, oMElement.toString());
						}
						errorFromUCStatus = true;
					} catch (Exception e2) {
						LogSupport.major(ctx, this,
								"Failed to create Price Template On COM due to DOM error: "
										+ e2, e2);
					}
				}
				if (result.equals(""))
					result = e.getMessage();

			}
		}
		return result;
	}

	private String getCOMError(Context ctx, String xmlStr) {
		
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "[getCOMError] printing xmlStr " + xmlStr);
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		String result = "";
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			if (doc != null) {

				NodeList errNodes = doc.getElementsByTagName("errorMessageList");
				if (errNodes.getLength() > 0) {
					Element err = (Element) errNodes.item(0);
					result = err.getElementsByTagName("message").item(0).getTextContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

}
