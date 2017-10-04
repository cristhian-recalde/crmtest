package com.trilogy.app.crm.integration.pc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.Permission;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplateXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.crmapi.wsdl.v3_0.api.PcService;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.NonVersionEntityStateChangeInput;
import com.trilogy.util.crmapi.wsdl.v3_0.api.core.io.common.v1.RequestContext;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.io.technicalservice.v1.RfssIOSave;
import com.trilogy.util.crmapi.wsdl.v3_0.api.pc.webservices.messages.v1.CreateTechnicalServiceResponse;
import com.trilogy.app.crm.bean.TemplateStateEnum;
import javax.xml.ws.soap.SOAPFaultException;

public class ReleaseToCOMAction extends SimpleWebAction {
	private static final long serialVersionUID = 1821883077892950324L;
	private boolean errorFromUCStatus;

	public ReleaseToCOMAction() {

		super("releasetocom", "ReleaseToCOM");
		this.defaultHelpText_ = "Send Technical Service to COM";

	}

	public ReleaseToCOMAction(Permission permission) {
		this();
		setPermission(permission);
	}

	public void writeLink(Context ctx, PrintWriter out, Object bean, Link link) {

		LogSupport.debug(ctx, this, "[ReleaseToCOMAction.writeLink] Start");
		
		link = modifyLink(ctx, bean, link);
		link.add("CMD", "ReleaseToCOM");
		
		out.print("<a href=\"");
		link.write(out);
		out.print("\" onclick=\"try{return confirm('Proceed with release request to com of ");
		out.print(XBeans.getIdentifier(bean));
		out.print("?');}catch(everything){}\">");
		out.print(getLabel());
		out.print("</a>");
	}

	public void execute(Context ctx) throws AgentException {
		
		LogSupport.info(ctx, this, "[ReleaseToCOMAction.execute] Creating and Releasing technical service template on COM");
		
		String cmd = WebAgents.getParameter(ctx, "cmd");
		String action = WebAgents.getParameter(ctx, "action");
		String stringKey = WebAgents.getParameter(ctx, "key");
		final PMLogMsg pmLog = new PMLogMsg("AppCrm",
				"TechnicalServiceTemplate Release");
		if (getKey().equals(action)) {
			TechnicalServiceTemplate technicalServiceTemplate = null;
			try {
				And filter = new And();
				filter.add(new EQ(TechnicalServiceTemplateXInfo.ID, Integer
						.valueOf(Integer.parseInt(stringKey))));
				technicalServiceTemplate = (TechnicalServiceTemplate) HomeSupportHelper
						.get(ctx).findBean(ctx, TechnicalServiceTemplate.class,
								filter);
			} catch (Exception e) {
				LogSupport.minor(ctx, this,
						"Cannot find TechnicalserviceTemplate " + e);
			}

			// If the Service Type is Package, do not send the request to UC.
			// Only change the state to released.
			// After release this should be non-editable.
			// This particular template will serve as a template for Package
			// Products.

			if (technicalServiceTemplate.getTemplateState() == TemplateStateEnum.DRAFT
					&& technicalServiceTemplate.getType() == ServiceTypeEnum.PACKAGE) {
				technicalServiceTemplate
						.setTemplateState(TemplateStateEnum.RELEASED);
				try {
					HomeSupportHelper.get(ctx).storeBean(ctx,
							technicalServiceTemplate);
					LogSupport
							.info(ctx,
									this,
									"[TemplateId:"
											+ technicalServiceTemplate.getID()
											+ "] Package Product Template State change succesfully");
					WebAgents.getWriter(ctx).println(
							"<font color=\"green\">"
									+ " Package Product Template with Id: "
									+ stringKey + " is successfully released."
									+ "</font><br/><br/>");
				} catch (Exception e) {
					
					LogSupport.debug(ctx, this,
							"[ReleaseToCOMAction.execute] Failed to release Package Product Template: " + e,
							e);
					WebAgents
							.getWriter(ctx)
							.println(
									"<font color=\"red\">Failed to release Package Product Template</font><br /><br />");
				}
			} else if (technicalServiceTemplate.getTemplateState().equals(
					TemplateStateEnum.DRAFT)
					&& !(technicalServiceTemplate.getType() == ServiceTypeEnum.PACKAGE)) {
				String response = createTechnicalServiceToCOM(ctx,
						technicalServiceTemplate);
				LogSupport.info(ctx, this,
						"[ReleaseToCOMAction.execute] Technical Service Template Response: " + response);
				if (response.equals(PCConstants.STATUS_SUCCESS)) {
					WebAgents
							.getWriter(ctx)
							.println(
									"<font color=\"green\">"
											+ " Technical Service Template with Id: "
											+ stringKey
											+ " has been sent to Unified Catalog - Response: "
											+ response + "</font><br/><br/>");
					com.redknee.app.crm.log.ERLogger
							.genrateERTechnicalServiceTemplate(ctx,
									PCConstants.STATUS_SUCCESS,
									technicalServiceTemplate);
				} else {
					if (errorFromUCStatus) {
						WebAgents
								.getWriter(ctx)
								.println(
										"<font color=\"red\">"
												+ " Technical Service Template with Id: "
												+ stringKey
												+ " has been sent to Unified Catalog - Response: "
												+ response
												+ "</font><br/><br/>");
						com.redknee.app.crm.log.ERLogger
								.genrateERTechnicalServiceTemplate(ctx, "FAIL",
										technicalServiceTemplate);
					} else {
						WebAgents
								.getWriter(ctx)
								.println(
										"<font color=\"red\">"
												+ "Failed to send Technical Service Template with Id: "
												+ stringKey
												+ " to Unified Catalog - Response: "
												+ response
												+ "</font><br/><br/>");
						com.redknee.app.crm.log.ERLogger
								.genrateERTechnicalServiceTemplate(ctx,
										PCConstants.STATUS_FAIL,
										technicalServiceTemplate);
					}
				}
			} else {
				WebAgents
						.getWriter(ctx)
						.println(
								"<font color=\"red\">This Service already in Released state</font><br /><br />");
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

	private String createTechnicalServiceToCOM(Context ctx,
			TechnicalServiceTemplate technicalServiceTemplate) {

		LogSupport.info(ctx, this, "[createTechnicalServiceToCOM] Creating technical service template on COM");
		
		String result = "";
		CreateTechnicalServiceResponse tsResponse = new CreateTechnicalServiceResponse();
		boolean isError = false;

		if ((technicalServiceTemplate != null) && (ctx != null)) {

			PcService client = (PcService) ctx.get(PCConstants.PC_SOAP_CLIENT);
			RequestContext requestContext = null;

			try {

				RfssIOSave rfss = TechnicalServiceTemplateAdapter.adapt(ctx,
						technicalServiceTemplate, new RfssIOSave());
				NonVersionEntityStateChangeInput nvesciObj = TechnicalServiceTemplateAdapter
						.adaptState(ctx, technicalServiceTemplate,
								new NonVersionEntityStateChangeInput());

				if (client == null) {
					new MajorLogMsg(this, "PcService client is null ").log(ctx);
					result = PCConstants.PC_SOAP_CLIENT
							+ " is not configured in BSS Please configure the client";
					return result;
				} else {
					if (LogSupport.isDebugEnabled(ctx)){
						LogSupport.debug(ctx, this, "[createTechnicalServiceToCOM] Creating the Technical Service");
					}
					tsResponse = client.createTechnicalService(rfss,
							requestContext);

					result = (String) TechnicalServiceTemplateAdapter.unAdapt(
							ctx, tsResponse);

					LogSupport.info(ctx, this, 
							"[createTechnicalServiceToCOM] Creating the Technical Service, CreateTechnicalServiceResponse = " 
									+ result);

					if (result.equals(PCConstants.STATUS)) {

						/*
						 * As per discussion with Subhro, Release of Technical Service will be done by COM
						 * ReleaseTechnicalServiceResponse rtsresponse = client.releaseTechnicalService(nvesciObj, requestContext); 
						 * result = (String) TechnicalServiceTemplateAdapter.unAdaptState(rtsresponse);
						 */
						
						LogSupport.info(ctx, this,
								"[createTechnicalServiceToCOM] Releasing the Technical Service, ReleaseTechnicalServiceResponse:"
										+ result);

						if (result.equals(PCConstants.STATUS)) {

							technicalServiceTemplate
									.setTemplateState(TemplateStateEnum.RELEASED);
							HomeSupportHelper.get(ctx).storeBean(ctx,
									technicalServiceTemplate);
							LogSupport.info(ctx, this, "[createTechnicalServiceToCOM] [TemplateId:" + technicalServiceTemplate.getID() 
									+ "]Technical Service Template State change succesfully");

						}
					}
				}
			} catch (Exception e) {
				LogSupport.major(ctx, this,
						"Failed to create Technical Service Template On COM: "
								+ e, e);
				/*
				 * result = (String)TechnicalServiceTemplateAdapter.unAdaptError(ctx,tsResponse);
				 * 
				 */
				// PcomFaultInfo faultInfo = (PcomFaultInfo)e.getDetails();

				if (LogSupport.isDebugEnabled(ctx)){
					LogSupport.debug(ctx, this,
						"[createTechnicalServiceToCOM] Error response: " + e.getMessage());
				}
				if (e instanceof java.net.UnknownHostException) {
					isError = true;
					result = "Host is unknown please check the host";
					LogSupport.major(ctx, this,
							"Host is unknown please check the host : " + e, e);
				} else if (e instanceof java.net.ConnectException) {

					isError = true;
					result = "Unable to connect host";
					LogSupport.major(ctx, this,
							"Unable to connect host : " + e, e);
				} else if (e instanceof org.apache.axis2.AxisFault) {
					if (((org.apache.axis2.AxisFault) e).getFaultCode() == null) {
						isError = true;
						result = "Unable to connect host";
						LogSupport.major(ctx, this, "Unable to connect host : "
								+ e, e);
					} else if (e.getMessage().contains("Password")
							|| e.getMessage().contains("User ID")) {
						isError = true;
						result = "User id or Password error";
						LogSupport.major(ctx, this,
								"User id or Password error : " + e, e);
					} else if (e.getMessage().contains("port")) {
						isError = true;
						result = "port out of range";
						LogSupport.major(ctx, this, "port out of range : " + e,
								e);
					}
				} else if (e instanceof IOException) {
					isError = true;

					result = "Unable to connect host";
					LogSupport.minor(ctx, this,
							"Unable to connect host : " + e, e);
					LogSupport.major(ctx, this,
							"Unable to connect host : " + e, e);
				}

				if (!isError) {
					try {
						AxisFault axisFault = (AxisFault) e;
						OMElement oMElement = axisFault.getDetail();
						if (oMElement != null) {
							result = getCOMError(oMElement.toString());
						}
						errorFromUCStatus = true;
					} catch (Exception e2) {
						LogSupport.major(ctx, this,
								"Failed to create Technical Service Template On COM due to DOM error: "
										+ e2, e2);
					}
				}
				if (result.equals(""))
					result = e.getMessage();

			}
		}
		return result;
	}

	private String getCOMError(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		String result = "";
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			if (doc != null) {

				NodeList errNodes = doc
						.getElementsByTagName("errorMessageList");
				if (errNodes.getLength() > 0) {
					Element err = (Element) errNodes.item(0);
					result = err.getElementsByTagName("message").item(0)
							.getTextContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}
}
