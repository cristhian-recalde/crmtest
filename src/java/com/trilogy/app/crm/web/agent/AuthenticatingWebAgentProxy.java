/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.agent;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.auth.web.AuthenticatedRequestServicer;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.web.agent.WebAgentProxy;
import com.trilogy.framework.xhome.webcontrol.NullRequestServicer;

/**
 * Provides authentication and set-up similar to that of the AuthenticatedRequestServicer.
 *
 * @author gary.anderson@redknee.com
 */
public class AuthenticatingWebAgentProxy
    extends WebAgentProxy
{
    /**
     * {@inheritDoc}
     */
    public void execute(final Context parentContext)
        throws AgentException
    {
        final Context context = parentContext.createSubContext();
        context.setName("AuthenticatingWebAgentProxy");

        final AuthenticatedRequestServicer servicer = new AuthenticatedRequestServicer(NullRequestServicer.instance());
      /*  context.put(LoginRequestServicer.class, NullRequestServicer.instance());

        final HttpServletResponse response = ServletBridge.getResponse(context);

        int responseCode = -1;
        String responseMessage = "";

        Context loginContext;
        try
        {
            loginContext = servicer.login(context, ServletBridge.getRequest(context), response);

            if (loginContext != null)
            {
                delegate(loginContext);
            }
            else
            {
                responseCode = HttpURLConnection.HTTP_UNAUTHORIZED;
            }
        }
        catch (final ServletException exception)
        {
            responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
            responseMessage = exception.getMessage();
            throw new AgentException("Servlet exception: " + exception.getMessage(), exception);
        }
        catch (final IOException exception)
        {
            responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
            responseMessage = exception.getMessage();
            throw new AgentException("I/O exception: " + exception.getMessage(), exception);
        }
        catch (final AgentException exception)
        {
            responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
            responseMessage = exception.getMessage();
            throw exception;
        }
        finally
        {
            if (responseCode != -1)
            {
                try
                {
                    response.sendError(responseCode, responseMessage);
                }
                catch (IOException exception)
                {
                    throw new AgentException(
                        "Failed to send response code " + responseCode + ": " + responseMessage,
                        exception);
                }
            }
        }
*/
    }


    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

}
