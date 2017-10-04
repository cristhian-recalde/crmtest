/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.urcs;

import java.util.Collection;

import com.trilogy.app.crm.bean.ScreeningTemplate;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.framework.xhome.context.Context;

/**
 * This interface defines the methods expected for the screening template corba client.
 *
 * @author Marcio Marques
 * @since 8.5
 */
public interface ScreeningTemplatesServiceClient
{
    public Collection<ScreeningTemplate> retrieveScreeningTemplates(final Context ctx, final int spid) throws RemoteServiceException;
   
    public Collection<ScreeningTemplate> retrieveActiveScreeningTemplates(final Context ctx, final int spid) throws RemoteServiceException;

    public Collection<ScreeningTemplate> retrieveDeprecatedScreeningTemplates(final Context ctx, final int spid) throws RemoteServiceException;
}
