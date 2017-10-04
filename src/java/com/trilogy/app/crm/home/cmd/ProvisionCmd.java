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
package com.trilogy.app.crm.home.cmd;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.support.Command;

/**
 * @author jchen
 */
public class ProvisionCmd implements Command
{
	public ProvisionCmd(Subscriber oldSub, Subscriber newSub, boolean chargeUnprovision, boolean chargeProvision)
	{
		oldSub_ = oldSub;
		newSub_ = newSub;
		chargeUnprovision_ = chargeUnprovision;
		chargeProvision_ = chargeProvision;
	}
	
	Subscriber oldSub_;
	Subscriber newSub_;
	boolean chargeUnprovision_;
	boolean chargeProvision_;
	
	/**
	 * @return Returns the chargeProvision_.
	 */
	public boolean isChargeProvision() {
		return chargeProvision_;
	}
	/**
	 * @return Returns the chargeUnprovision_.
	 */
	public boolean isChargeUnprovision() {
		return chargeUnprovision_;
	}
	/**
	 * @return Returns the newSub_.
	 */
	public Subscriber getNewSub() {
		return newSub_;
	}
	/**
	 * @return Returns the oldSub_.
	 */
	public Subscriber getOldSub() {
		return oldSub_;
	}
}
