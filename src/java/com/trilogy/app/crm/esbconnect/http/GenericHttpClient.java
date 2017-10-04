package com.trilogy.app.crm.esbconnect.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.http.catalina.Attribute;
import com.trilogy.app.crm.bean.GenericHTTPConnectionConfig;
import com.trilogy.app.crm.bean.GenericHTTPConnectionInfo;

import com.trilogy.app.crm.bean.HttpVersionsEnum;
import com.trilogy.app.crm.bean.HttpProtocolParameters;


public class GenericHttpClient implements HttpConnector, HttpLink, ContextAware
{

    public GenericHttpClient(final Context ctx, final GenericHTTPConnectionConfig httpConnectionConfig)
            throws URISyntaxException
    {
        httpConnectionConfig_ = httpConnectionConfig;
        httpConnectionConfig_.freeze();
        targetURI_ = new URI(httpConnectionConfig_.getHttpConnector().isSecure() ? ("https") : ("http"), null,
                httpConnectionConfig_.getHttpConnector().getHost(), httpConnectionConfig_.getHttpConnector().getPort(),
                httpConnectionConfig_.getHttpConnector().getPath(), null, null);
        setContext(ctx);
    }


    @Override
    public Context getContext()
    {
        return ctx_;
    }


    @Override
    public void setContext(Context ctx)
    {
        ctx_ = ctx;
    }


    @Override
    public String sendCommand(String requestText)
    {
        final HttpContext httpContext = new BasicHttpContext();
        final HttpPost httpRequest = new HttpPost(targetURI_);
        try
        {
            httpRequest.setEntity(new StringEntity(requestText, httpConnectionConfig_.getHttpProtocolParams()
                    .getContentCharSet()));
        }
        catch (UnsupportedEncodingException e1)
        {
            // TODO Auto-generated catch block
            new MinorLogMsg(this, "Operation-Request [" + requestText
                    + "] Failed due choice invalid/un-supported Encoding", e1).log(getContext());
        }
        String response = executeOperation(httpConnectionConfig_.getHttpConnector().getHost(), httpRequest, handler_,
                httpContext);
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(this, "Request[" + requestText + "] completed with Respose [" + "" + "]", null)
                    .log(getContext());
        }
        return response;
    }


    private String executeOperation(String host, HttpRequest httpRequest, ResponseHandler<String> responseHandler,
            HttpContext httpContext)
    {
        String responseText = null;
        try
        {
            responseText = client_.execute(new HttpHost(host), httpRequest, responseHandler, httpContext);
        }
        catch (ClientProtocolException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return responseText;
    }


    @Override
    public void open()
    {
        new InfoLogMsg(this, "Opening Link of Type [" + httpConnectionConfig_.getClass().getSimpleName() + "], URI ["
                + targetURI_ + "]", null).log(getContext());
        HttpParams params = new BasicHttpParams();
        // HTTP protocol params
        final HttpProtocolParameters paramConfig = httpConnectionConfig_.getHttpProtocolParams();
        if (paramConfig.getVersion() == HttpVersionsEnum.HTTP_1_1)
        {
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        }
        else if (paramConfig.getVersion() == HttpVersionsEnum.HTTP_1_0)
        {
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
        }
        else
        {
            // use HTTP 1.1 in case of doubt
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        }
        params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, paramConfig.getElementCharSet());
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, paramConfig.getContentCharSet());
        params.setParameter(CoreProtocolPNames.USER_AGENT, paramConfig.getUserAgent());
        params.setBooleanParameter(CoreProtocolPNames.STRICT_TRANSFER_ENCODING, paramConfig.isStrictTransferEncoding());
        if (httpConnectionConfig_.getHttpProtocolParams().isUseExpectContinue())
        {
            params.setBooleanParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, Boolean.TRUE);
            // the period is configured in milli-seconds
            params.setIntParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, paramConfig.getWaitForContinuePeriod());
        }
        // HTTP Connection Params
        final GenericHTTPConnectionInfo connectorConfig = httpConnectionConfig_.getHttpConnector();
        ConnManagerParams.setMaxTotalConnections(params, connectorConfig.getMaxNumConnections());
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(connectorConfig.getMaxConnectionsPerRoute());
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
        ConnManagerParams.setTimeout(params, connectorConfig.getConnectionTimeout() * 1000);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectorConfig.getConnectionTimeout() * 1000);
        // so-timeout value is fed in milli-seconds
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, connectorConfig.getSocketTimeout());
        // linger value is fed in seconds
        params.setIntParameter(CoreConnectionPNames.SO_LINGER, connectorConfig.getLinger());
        params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, connectorConfig.getNodelay());
        params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, connectorConfig.getSocketBufferSize());
        // there is a thread that explicitly weeds out stale connections.
        // However not the safest, we can have that thread weed out connection instead of
        // checking state of connection on every request
        params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK,
                (connectorConfig.getStaleConnectionPeriod() <= 0));
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpConnectionConfig_
                .getHttpConnector().getPort()));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), connectorConfig.getPort()));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
        final long keepAlivePeriod = connectorConfig.getKeepAlivePeriod() * 1000;
        httpClient.addRequestInterceptor(new HttpRequestInterceptor()
        {

            // add all the configured HTTP Request Headers
            public void process(final HttpRequest httpRequest, final HttpContext context) throws HttpException,
                    IOException
            {
                for (Attribute attribute : (List<Attribute>) connectorConfig.getAttributes())
                {
                    httpRequest.addHeader(attribute.getKey(), attribute.getValue());
                }
            }
        });
        httpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy()
        {

            // set KeepAlive Strategy
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext context)
            {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(httpResponse
                        .headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext())
                {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout"))
                    {
                        try
                        {
                            return Long.parseLong(value) * 1000;
                        }
                        catch (NumberFormatException ignore)
                        {
                            new DebugLogMsg(this, "Invalid tiemout-keep-alive value in header [" + value + "]", null)
                                    .log(getContext());
                            return keepAlivePeriod;
                        }
                    }
                }
                return keepAlivePeriod;
            }
        });
        final int retryAttempts = connectorConfig.getRetryAttemts();
        if (retryAttempts > 0)
        {
            // set retry strategy
            httpClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler()
            {

                public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
                {
                    if (executionCount >= retryAttempts)
                    {
                        new DebugLogMsg(this, "No Retryfor exception [" + exception.getClass().getSimpleName() + "]",
                                exception).log(getContext());
                        // Do not retry if over max retry count
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException)
                    {
                        new DebugLogMsg(this, "Attempting Retry for NoHttpResponseException ["
                                + exception.getClass().getSimpleName() + "]", exception).log(getContext());
                        // Retry if the server dropped connection on us
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException)
                    {
                        new DebugLogMsg(this, "Attempting Retry for SSLHandshakeException ["
                                + exception.getClass().getSimpleName() + "]", exception).log(getContext());
                        // Do not retry on SSL handshake exception
                        return false;
                    }
                    if (retryAttempts > 0)
                    {
                        new DebugLogMsg(this, "Retry for exception [" + exception.getClass().getSimpleName() + "]",
                                exception).log(getContext());
                        // Retry if the request is considered idempotent
                        return true;
                    }
                    return false;
                }
            });
        }
        client_ = httpClient;
    }


    @Override
    public void close()
    {
        new InfoLogMsg(this, "Closing Link of Type [" + httpConnectionConfig_.getClass().getSimpleName() + "], URI ["
                + targetURI_ + "]", null).log(getContext());
        client_.getConnectionManager().shutdown();
    }

    final private ResponseHandler<String> handler_ = new ResponseHandler<String>()
    {

        @Override
        public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException
        {
            final HttpEntity entity = httpResponse.getEntity();
            if (entity != null)
            {
                return EntityUtils.toString(entity);
            }
            else
            {
                new MinorLogMsg(this, "No entity content found in HTTP Response [" + httpResponse.toString() + "]",
                        null).log(getContext());
                return null;
            }
        }
    };
    private final GenericHTTPConnectionConfig httpConnectionConfig_;
    private HttpClient client_;
    private final URI targetURI_;
    private Context ctx_;
}
