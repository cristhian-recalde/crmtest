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

import com.trilogy.app.crm.bean.GenericPackageImport;
import com.trilogy.app.crm.bean.TDMAPackageImport;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Adapts a TDMAPackageImport to and from GenericPackageImport.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class TDMAPackageImportToGenericPackageImportAdapter implements Adapter
{

	/**
	 * Default serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public TDMAPackageImportToGenericPackageImportAdapter(int spid,
	    String packageGroup, TechnologyEnum tech)
	{
		spid_ = Integer.toString(spid);
		packageGroup_ = packageGroup;
		technology_ = Integer.toString(tech.getIndex());
		useDefault_ = true;
	}

	public TDMAPackageImportToGenericPackageImportAdapter(TechnologyEnum tech)
	{
		spid_ = null;
		packageGroup_ = null;
		technology_ = Integer.toString(tech.getIndex());
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
		TDMAPackageImport tdma = (TDMAPackageImport) obj;
		GenericPackageImport generic = new GenericPackageImport();
		if (useDefault_)
		{
			generic.setSpid(spid_);
			generic.setPackGroup(packageGroup_);
		}
		else
		{
			generic.setSpid(tdma.getSpid());
			generic.setPackGroup(tdma.getPackageGroup());
		}
		generic.setMinOrIMSI(tdma.getMin());
		generic.setSerialNo(tdma.getSerialNo());
		generic.setPIN1OrSubsidyKey(tdma.getSubsidyKey());
		generic.setPUK1OrMassSubsidyKey(tdma.getMassSubsidyKey());
		generic.setESN(tdma.getESN());
		generic.setPackId(tdma.getPackId());
		generic.setDealerCode(tdma.getDealer());
		generic.setState(tdma.getState());
		generic.setKIorAuthKey(tdma.getAuthKey());
		generic.setTechnology(technology_);
		generic.setAAALogin("");
		generic.setAAAPwd("");
		generic.setNAAAALogin("");
		generic.setNAAAAPwd("");
		generic.setDefaultResource("");
		
		generic.setPackageType(tdma.getPackageType());
		generic.setExternalMSID(tdma.getExternalMSID());
		generic.setCustomerOwned(tdma.getCustomerOwned());
		generic.setDescription(tdma.getDescription());
		
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
		TDMAPackageImport gsm = new TDMAPackageImport();

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
		gsm.setMin(generic.getMinOrIMSI());
		gsm.setSerialNo(generic.getSerialNo());
		gsm.setSubsidyKey(generic.getPIN1OrSubsidyKey());
		gsm.setMassSubsidyKey(generic.getPUK1OrMassSubsidyKey());
		gsm.setESN(generic.getESN());
		gsm.setPackId(generic.getPackId());
		gsm.setDealer(generic.getDealerCode());
		gsm.setState(generic.getState());
		gsm.setAuthKey(generic.getKIorAuthKey());
		gsm.setTechnology(technology_);
		return gsm;
	}

	private final boolean useDefault_;
	private final String spid_;
	private final String packageGroup_;
	private final String technology_;
}
