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

package com.trilogy.app.crm.bean.ui;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.GLCodeVersionN;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This will implement AccountZipCodeFilterCriteria implementation of methods from com.redknee.app.crm.invoice.bean.BillRunFilterCriteria.
 * @author bhushan.deshmukh@redknee.com
 * @since 10.3.6
 */


public class GLCodeNSettingHome extends HomeProxy {
	
	private static final long serialVersionUID = 1L;


	
    public GLCodeNSettingHome(Context context,Home delegate)
    {
        super(context, delegate);
    }

    
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        final GLCodeVersionN glCodeVersionN = (GLCodeVersionN) obj;
  
              final long code = this.getNextIdentifier(ctx);
        	
              if(glCodeVersionN.getVersionId() == 0)
              {
        		glCodeVersionN.setVersionId(code);
              }
       
        	LogSupport.debug(ctx, this, "GL Code version Id set to: " + glCodeVersionN.getVersionId());
        return super.create(ctx,glCodeVersionN);
    }


   
    private long getNextIdentifier(final Context ctx)
            throws HomeException
        {
            IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(ctx,IdentifierEnum.GL_CODE_VERSION_ID,1,Long.MAX_VALUE);

            
            return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx,IdentifierEnum.GL_CODE_VERSION_ID,null);
        }
}
