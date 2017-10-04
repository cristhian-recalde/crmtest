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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.bulkloader;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.GSMPackageImport;
import com.trilogy.app.crm.bean.GenericPackageImport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Adapts a GSMPackageImport to and from GenericPackageImport.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class GSMPackageImportToGenericPackageImportAdapter implements Adapter
{

	/**
	 * Default serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public GSMPackageImportToGenericPackageImportAdapter(int spid,
	    String packageGroup)
	{
		spid_ = Integer.toString(spid);
		packageGroup_ = packageGroup;
		useDefault_ = true;
	}

	public GSMPackageImportToGenericPackageImportAdapter()
	{
		spid_ = null;
		packageGroup_ = null;
		useDefault_ = false;
	}

	/**
	 * @param ctx
	 * @param obj
	 * @return
	 * @throws HomeException
	 * @see com.redknee.framework.xhome.home.Adapter#adapt(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public Object adapt(Context ctx, Object obj)
	{
		GSMPackageImport gsm = (GSMPackageImport) obj;
		GenericPackageImport generic = new GenericPackageImport();
		if (useDefault_)
		{
			generic.setSpid(spid_);
			generic.setPackGroup(packageGroup_);
		}
		else
		{
			generic.setSpid(gsm.getSpid());
			generic.setPackGroup(gsm.getPackageGroup());
		}
		generic.setMinOrIMSI(gsm.getIMSI());
		generic.setMinOrIMSI1(gsm.getIMSI1());
		generic.setMinOrIMSI2(gsm.getIMSI2());
		generic.setSerialNo(gsm.getSerialNo());
		generic.setPIN1OrSubsidyKey(gsm.getPIN1());
		generic.setPUK1OrMassSubsidyKey(gsm.getPUK1());
		generic.setPIN2(gsm.getPIN2());
		generic.setPUK2(gsm.getPUK2());
		generic.setPackId(gsm.getPackId());
		generic.setDealerCode(gsm.getDealer());
		generic.setState(gsm.getState());
		generic.setADM1(gsm.getADM1());
		generic.setKAPPLI(gsm.getKAPPLI());
		generic.setKIorAuthKey(gsm.getKI());
		generic.setTechnology(Integer.toString(TechnologyEnum.GSM_INDEX));
		generic.setAAALogin("");
		generic.setAAAPwd("");
		generic.setNAAAALogin("");
		generic.setNAAAAPwd("");
		generic.setDefaultResource("");
		return generic;
	}

	/**
	 * @param ctx
	 * @param obj
	 * @return
	 * @throws HomeException
	 * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context,
	 *      java.lang.Object)
	 */
	@Override
	public Object unAdapt(Context ctx, Object obj)
	{
		GenericPackageImport generic = (GenericPackageImport) obj;
		GSMPackageImport gsm = new GSMPackageImport();

		if (useDefault_)
		{
			gsm.setSpid(spid_);
			gsm.setPackageGroup(packageGroup_);
		}
		else
		{
			gsm.setSpid(generic.getSpid());
			gsm.setPackageGroup(generic.getPackGroup());
		}
		gsm.setIMSI(generic.getMinOrIMSI());
		gsm.setSerialNo(generic.getSerialNo());
		gsm.setPIN1(generic.getPIN1OrSubsidyKey());
		gsm.setPIN2(generic.getPIN2());
		gsm.setPUK1(generic.getPUK1OrMassSubsidyKey());
		gsm.setPUK2(generic.getPUK2());
		gsm.setPackId(generic.getPackId());
		gsm.setDealer(generic.getDealerCode());
		gsm.setState(generic.getState());
		gsm.setADM1(generic.getADM1());
		gsm.setKAPPLI(generic.getKAPPLI());
		gsm.setKI(generic.getKIorAuthKey());
		gsm.setType(Integer.toString(TechnologyEnum.GSM_INDEX));
		return gsm;
	}

	private final boolean useDefault_;
	private final String spid_;
	private final String packageGroup_;
}
