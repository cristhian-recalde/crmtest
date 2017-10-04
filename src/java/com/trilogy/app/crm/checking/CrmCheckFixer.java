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
package com.trilogy.app.crm.checking;

/**
 * @author lxia
 */
public abstract class CrmCheckFixer implements CrmCheck, CrmFix {
	int start = 0;
	int end = Integer.MAX_VALUE; 
	int spid = -1; 
	MessageHandler messager_; 
	
	/**
	 * @return Returns the end.
	 */
	public int getEnd() {
		return end;
	}
	/**
	 * @param end The end to set.
	 */
	public void setEnd(int end) {
		this.end = end;
	}
	/**
	 * @return Returns the start.
	 */
	public int getStart() {
		return start;
	}
	/**
	 * @param start The start to set.
	 */
	public void setStart(int start) {
		this.start = start;
	}

	
	/**
	 * @return Returns the messager_.
	 */
	public MessageHandler getMessager() {
		return messager_;
	}
	/**
	 * @param messager_ The messager_ to set.
	 */
	public void setMessager(MessageHandler messager_) {
		this.messager_ = messager_;
	}
	
	/**
	 * @return Returns the spid.
	 */
	public int getSpid() {
		return spid;
	}
	/**
	 * @param spid The spid to set.
	 */
	public void setSpid(int spid) {
		this.spid = spid;
	}
}
