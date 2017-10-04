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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DepositReasonCodeHome;
import com.trilogy.app.crm.bean.DepositReasonCodeKeyWebControl;
import com.trilogy.app.crm.bean.DepositReasonCodeXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;

public class CustomizedDepositReasonCodeKeyWebControl extends
		DepositReasonCodeKeyWebControl {

	public static final KeyWebControlOptionalValue DEFAULT=new KeyWebControlOptionalValue("--", "-1");
	
	public CustomizedDepositReasonCodeKeyWebControl() {
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize) {
		super(listSize);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview) {
		super(listSize, autoPreview);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview, Class baseClass, String sourceField,
			String targetField) {
		super(listSize, autoPreview, baseClass, sourceField, targetField);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview, boolean isOptional) {
		super(listSize, autoPreview, isOptional);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview, boolean isOptional, boolean allowCustom) {
		super(listSize, autoPreview, isOptional, allowCustom);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue) {
		super(listSize, autoPreview, optionalValue);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue, Class baseClass,
			String sourceField, String targetField) {
		super(listSize, autoPreview, optionalValue, baseClass, sourceField,
				targetField);
		init();
	}

	public CustomizedDepositReasonCodeKeyWebControl(int listSize,
			boolean autoPreview, Object optionalValue, boolean allowCustom) {
		super(listSize, autoPreview, optionalValue, allowCustom);
		init();
	}


	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj) {
		Context subContext = filterHome(ctx);
		super.toWeb(subContext, out, name, obj);
	}

	public Context filterHome(Context context) {
		
		Context subCtx = context.createSubContext();
		Object obj = context.get(AbstractWebControl.BEAN);

		if (obj instanceof CRMSpid) {
			
			CRMSpid crmSpid = (CRMSpid) obj;
			Home depositReasonCodeHome = (Home) subCtx.get(DepositReasonCodeHome.class);
			if(depositReasonCodeHome!=null){
				And and = new And();
				and.add(new EQ(DepositReasonCodeXInfo.SPID, crmSpid.getSpid()));
				depositReasonCodeHome = depositReasonCodeHome.where(context, and);
			}
			subCtx.put(DepositReasonCodeHome.class, depositReasonCodeHome);
		}
		
		return subCtx;
	}

}
