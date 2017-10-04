package com.trilogy.app.crm.esbconnect;

import java.io.BufferedReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import com.trilogy.esb.schema.genericrequestresponse.GenericParameterType;
import com.trilogy.esb.schema.genericrequestresponse.ObjectFactory;
import com.trilogy.esb.schema.genericrequestresponse.RequestType;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.ContextHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.GenericHTTPConnectionConfig;
import com.trilogy.app.crm.esbconnect.http.GenericHttpClient;
import com.trilogy.app.crm.esbconnect.http.GenericHttpConnectionConstants;
import com.trilogy.esb.schema.genericrequestresponse.ResponseType;
import com.trilogy.app.crm.esbconnect.ESBMessageResult;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.DefaultHomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.bean.EsbTargetName;

public class DefaultEsbMessage extends AbstractESBGenericConnectionGateway{

	public DefaultEsbMessage () {
		
	}
	

	public ArrayList<String> generateGrrMsg(Context ctx,int spid,String msisdn	)
	throws Exception
	{
		 final ObjectFactory grrFactory = new ObjectFactory();
		 
		ArrayList<RequestType> ReqType = createRequest(grrFactory,ctx,spid,msisdn);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(RequestType.class.getPackage().getName()); 
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		ArrayList<String> ReqTypeMsg = new ArrayList<String>();
		StringWriter writer = null;
				
		for (Iterator iterator = ReqType.iterator(); iterator.hasNext();) 
		{
			RequestType request = (RequestType) iterator.next();
			writer = new StringWriter();	
			jaxbMarshaller.marshal(grrFactory.createRequest(request), writer);
			ReqTypeMsg.add(writer.toString());
		}
		
		
		return ReqTypeMsg;
	}
	
	private ArrayList<RequestType> createRequest(final ObjectFactory grrFactory,Context ctx,int spid,String msisdn)
	throws Exception
	{
		
        ArrayList<RequestType> templatesReqType =  new ArrayList<RequestType>();
		
		ArrayList<String> param1 = new ArrayList<String>();
		
        BufferedReader reader = new BufferedReader( new java.io.StringReader(this.bssCmd));
        String parameter = reader.readLine(); 
      
        while ( parameter != null)
        {
            if (!parameter.trim().isEmpty() && parameter.indexOf(VALUE_DELIMITOR)!=-1)
            {
                if (parameter.trim().startsWith(TEMPLATE_KEY))
                {	
                	final RequestType reqType =  grrFactory.createRequestType();
                	reqType.getParameter().add( 
                	this.createParmeter(getParameterName(parameter), getParameterValue(parameter)));                	
                	reqType.getHeader().add(createParmeter(TARGET_NAME, this.getESBTargetValue()));
                	templatesReqType.add(reqType);
                }
                else
                {
                	param1.add(parameter); 
                }
            }
            parameter = reader.readLine(); 
        }
		
        
        GenericParameterType otherParams = new GenericParameterType(); 
        otherParams.setName(PARAMETER_KEY);
		for (Iterator iterator = templatesReqType.iterator(); iterator.hasNext();) 
		{
			RequestType requestType = (RequestType) iterator.next();
			requestType.getParameter().add(otherParams);			
		}
		
        otherParams.getPart().add(createParmeter(MSISDN, msisdn));
        otherParams.getPart().add(createParmeter(SPID, spid));
		
		for (String param: param1)
		{
			otherParams.getPart().add( 
        			this.createParmeter(getParameterName(param), getParameterValue(param)));
		}
		
		return templatesReqType;
	}
	
	
	private String getParameterName (String cmd)
	{
		return cmd.substring(0, cmd.indexOf(VALUE_DELIMITOR)).trim(); 
	}
	
	private String getParameterValue (String cmd)
	{
		return cmd.substring(cmd.indexOf(VALUE_DELIMITOR)+1).trim(); 
	}	
	
	
	private GenericParameterType createParmeter(String name, Object value)
	{
		GenericParameterType ret = new GenericParameterType(); 
		ret.setName(name);
		ret.getValue().add(value);
	
		return ret; 
	}
	
	public  String getESBTargetValue()
	{
		EsbTargetName config = (EsbTargetName) ctx.get(EsbTargetName.class);
	
		
			return config.getEsbTargetName();
	//	return "ESB.Orchestration.processTemplateExpression";
	}

	public ESBMessageResult getDatefromEsb(Context ctx,Subscriber subscriber ,int spid) {
		Map <String,String> ret = null;	
		try 
		{
			ArrayList<String> req = this.generateGrrMsg(ctx,spid,subscriber.getMsisdn());
			if(req != null)
			{
			    for (Iterator iterator = req.iterator(); iterator.hasNext();)
	            {
	                String request = (String) iterator.next();
	                if (LogSupport.isDebugEnabled(ctx))
	                {
	                	LogSupport.debug(com.redknee.app.crm.ContextHelper.getContext(), this, " send to esb "  + request); 
	                }
                    if (request != null )
	                {
	                    String response = this.getClient(ctx).sendCommand(request);
	                        if (LogSupport.isDebugEnabled(ctx))
	                        {
	                             LogSupport.debug(ctx, this, " get response from esb "  + response);
	                        }   
	                        try
	                        {
	                        	esbMessageResult_ =this.handleResponse(ctx,response);
	                            if(esbMessageResult_ != null)
	                            {
	                                return esbMessageResult_;
	                            }
	                        } 
	                        catch (Exception e) 
	                        {
	                        	LogSupport.debug(ctx, this, " error response from esb "  + response);
	                            break;
                            }
	                        
	                }
	            }
			}
			
		} catch (Exception e)
		{
			 LogSupport.debug(ctx, this, "failed to send command to Sprint through esb", e);
		}
		
		return esbMessageResult_;
	}
	
	private ESBMessageResult handleResponse(Context ctx,String response)
			throws Exception
			{
				GrrSrpintResponse resp = new GrrSrpintResponse(response); 
				esbMessageResult_ =resp.getResponseCode(ctx); 
					Map<String, String> gp = esbMessageResult_.getResultMessageMap();
					gp.keySet().toString().replaceAll("[\\[\\],]","");
					
					Iterator<Map.Entry<String, String>> entries = gp.entrySet().iterator();
					
					while (entries.hasNext()) {
					    Map.Entry<String, String> entry = entries.next();
					   
						if (LogSupport.isDebugEnabled(ctx))
			            {
			                LogSupport.debug(ctx,this, "gp.getName() " + entry.getKey());
			                LogSupport.debug(ctx, this, "gp.getValue() " + entry.getValue());
			            }
			            
			        }    
					return esbMessageResult_;
					  
			}
	
	public String setBssCommand(String bssCommand) throws Exception
	{
		this.bssCmd=bssCommand;
		return this.bssCmd; 
	}
	
	private static String MSISDN_;
	private static int SPID_;
	
	final static String TARGET_NAME = "Target";
	 
	final static String VALUE_DELIMITOR =  "="; 
	final static String SERVICE_NAME ="retail_serviceid"; 
	final static String SERVICE_TYPE ="retail_servicetype";
	final static String MSISDN ="MSISDN"; 
	final static String SPID ="SPID";
	final static String TEMPLATE_KEY = "grr_template"; 
	final static String PARAMETER_KEY = "grr_parameters";
	
	public final static short HLR_SUCCESS = 0;
	public final static short ErrorCode_SUCCESS =0;
	public final static short HLR_FAILURE_INTERNAL = 2001;
	Context ctx=com.redknee.app.crm.ContextHelper.getContext();
	ESBMessageResult esbMessageResult_ = null;

	String bssCmd; 
}
