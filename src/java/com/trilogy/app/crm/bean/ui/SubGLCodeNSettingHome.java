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
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
/**
 * This will implement AccountZipCodeFilterCriteria implementation of methods from com.redknee.app.crm.invoice.bean.BillRunFilterCriteria.
 * @author bhushan.deshmukh@redknee.com
 * @since 10.3.6
 */
public class SubGLCodeNSettingHome extends HomeProxy {
	
	private static final long serialVersionUID = 1L;


	
    public SubGLCodeNSettingHome(Context context,Home delegate)
    {
        super(context, delegate);
    }

    
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        final SubGLCodeVersionN subglCodeVersionN = (SubGLCodeVersionN) obj;
  
        	final long code = getNextIdentifier(ctx);
        	if(subglCodeVersionN.getVersionId() ==0)
        	{
        
        	   subglCodeVersionN.setVersionId(code);
        	
        	}
        return super.create(ctx,subglCodeVersionN);
    }


   
    private long getNextIdentifier(final Context ctx)
            throws HomeException
        {
            IdentifierSequenceSupportHelper.get(ctx).ensureSequenceExists(
                ctx,
                IdentifierEnum.SUB_GL_CODE_VERSION_ID,
                1,
                Long.MAX_VALUE);
           
            return IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx,IdentifierEnum.SUB_GL_CODE_VERSION_ID,null);
        }

}
