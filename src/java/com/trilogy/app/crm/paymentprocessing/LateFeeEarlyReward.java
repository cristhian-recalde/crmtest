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
package com.trilogy.app.crm.paymentprocessing;

import java.io.Serializable;
import java.util.Date;

import com.trilogy.framework.xhome.beans.SafetyUtil;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-03
 */
public class LateFeeEarlyReward implements Serializable,
    Comparable<LateFeeEarlyReward>
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public LateFeeEarlyReward(Date date, int adjustmentType, long amount,
	    ChargedItemTypeEnum chargedItemType, int deadline,
	    LateFeeEarlyRewardConfiguration config)
	{
		date_ = date;
		adjustmentType_ = adjustmentType;
		amount_ = amount;
		chargedItemType_ = chargedItemType;
		deadline_ = deadline;
		config_ = config;
	}

	public int getAdjustmentType()
	{
		return adjustmentType_;
	}

	public long getAmount()
	{
		return amount_;
	}

	public Date getDate()
	{
		return date_;
	}

	public ChargedItemTypeEnum getChargedItemType()
	{
		return chargedItemType_;
	}

	public LateFeeEarlyRewardConfiguration getConfig()
	{
		return config_;
	}

	public int getDeadline()
	{
		return deadline_;
	}

	@Override
	public int hashCode()
	{
		long code = getDate().hashCode();
		code *= 43;
		code += getAmount();
		code *= 43;
		code += getAdjustmentType();
		code *= 43;
		code += getChargedItemType().getIndex();
		code *= 43;
		code += getConfig().getIdentifier();
		code *= 43;
		code += getDeadline();
		return (int) code;
	}

	@Override
	public boolean equals(Object o)
	{
		LateFeeEarlyReward other = (LateFeeEarlyReward) o;
		return other != null
		    && other.getDate().equals(getDate())
		    && other.getAmount() == getAmount()
		    && other.getAdjustmentType() == getAdjustmentType()
		    && SafetyUtil.safeEquals(other.getChargedItemType(),
		        getChargedItemType())
		    && other.getConfig().getIdentifier() == getConfig().getIdentifier()
		    && other.getDeadline() == getDeadline();
	}

	private final int adjustmentType_;
	private final long amount_;
	private final Date date_;
	private final ChargedItemTypeEnum chargedItemType_;
	private final LateFeeEarlyRewardConfiguration config_;
	private final int deadline_;

	@Override
	public int compareTo(LateFeeEarlyReward o)
	{
		int cmp = getDate().compareTo(o.getDate());
		if (cmp != 0)
		{
			return cmp;
		}
		if (getAmount() != o.getAmount())
		{
			return (int) (getAmount() - o.getAmount());
		}
		return (getAdjustmentType() - o.getAdjustmentType());
	}
}
