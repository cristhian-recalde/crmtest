/**
 * 
 */
package com.trilogy.app.crm.client.ringbacktone;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * HttpProvClient convert ProvCommand to a Http Post request, send request to a HttpServer
 * and convert received response to ProvResponse.
 * 
 * @author jli
 * 
 */
public class HttpProvClient
{
    public HttpProvClient(Context context, String url)
    {
        this.context_ = context;
        this.url_ = url;
    }

    public HttpProvClient(Context context, String url, boolean auth, String username, String password, RbtAuthTypeEnum type)
    {
        this.context_ = context;
        this.url_ = url;
        this.authentication_ = auth;
        this.username_       = username;
        this.password_       = password;
        this.type_           = type;
    }
    
    public ProvResponse sendCommand(ProvCommand command) throws IOException
    {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url_);
        method.setParameter("Prov_command", command+";");
        
        if (LogSupport.isDebugEnabled(context_)) LogSupport.debug(context_, this, "Sending Command:Prov_command="+command+";");
        
        int code = client.executeMethod(method);
        ProvResponse response = null;
        
        if (code == 200/*HTTP OK*/) 
        {
            response = new ProvResponse(method.getResponseBodyAsString());
        }
        else
        {
            response = new ProvResponse(null,"Http Error","Http Error:"+code);
        }
        
        if (LogSupport.isDebugEnabled(context_)) LogSupport.debug(context_, this, "Response received:"+response+";");
        return response;
    }
    
    /**
     * Convenient method to invoke service.
     * @param context
     * @param name
     * @param params
     * @return
     * @throws InvalidCommandException 
     * @throws IOException 
     */
    public static ProvResponse sendCommand(Context context, String url, String name, String[] params) throws InvalidCommandException, IOException
    {
        ProvResponse resp = new ProvResponse(null, "Invoke Error", "Failure");
        ProvCommand command = ProvCommand.createCommand(context, name);
        if (params != null)
        {
            for (int i=0; i<params.length; i++) command.setIndexValue(i, params[i]);
        }
        
        resp = new HttpProvClient(context, url).sendCommand(command);
        
        return resp;
    }
    
    private Context context_;
    private String  url_;
    
    //Optional parameters to support Http Authentication.
    private boolean authentication_ = false;
    private String username_;
    private String password_;
    private RbtAuthTypeEnum type_;
}
