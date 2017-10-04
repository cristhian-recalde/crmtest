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
package com.trilogy.app.crm.duplicatedetection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.msp.SpidHome;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionForm;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.web.border.DuplicateAccountDetectionRedirectBorder;

/**
 * This agent sets the identification groups for the duplicate account search.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-28
 */
public class DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent
    extends ContextAgentProxy
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for
	 * DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent.
	 */
	public DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent()
	{
	}

	/**
	 * Constructor for
	 * DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent.
	 * 
	 * @param delegate
	 */
	public DuplicateAccountDetectionCriteriaIdentificationGroupSettingAgent(
	    ContextAgent delegate)
	{
		super(delegate);
	}

	@Override
	public void execute(final Context context) throws AgentException
	{
		/*
		 * Updates the default identification groups.
		 */
		DuplicateAccountDetectionForm criteria =
		    (DuplicateAccountDetectionForm) SearchBorder.getCriteria(context);
		if (context
		    .getBoolean(
		        DuplicateAccountDetectionSearchAgentV2.UPDATE_IDENTIFICATION_GROUPS,
		        false))
		{
			setDefaultIdentificationGroups(context, criteria);
		}
		delegate(context);
	}

	/**
	 * Sets the identification groups for the selected SPID in the criteria. If
	 * no SPID is currently selected, use the one to be displayed first in the
	 * drop-down, because it would appear as selected in the GUI.
	 * 
	 * @param context
	 *            The operating context.
	 * @param criteria
	 *            The search criteria.
	 */
	private void setDefaultIdentificationGroups(final Context context,
	    DuplicateAccountDetectionForm criteria)
	{
		int spid = DuplicateAccountDetectionForm.DEFAULT_SPID;
		boolean idGroupNotSet =
		    criteria != null
		        && (criteria.getIdentificationGroupList() == null || criteria
		            .getIdentificationGroupList().isEmpty());
		boolean spidUpdated =
		    (criteria != null && criteria.getSpid() != context
		        .getInt(DuplicateAccountDetectionRedirectBorder.PARAMETER_PREVIOUS_DETECTION_SPID));
		if (idGroupNotSet || spidUpdated)
		{
			LogSupport
			    .info(
			        context,
			        this,
			        "Identification Group List for this search criteria is not set; setting to default list");

			spid = criteria.getSpid();

			// find first SPID
			if (spid == DuplicateAccountDetectionForm.DEFAULT_SPID)
			{
				Home home = (Home) context.get(SpidHome.class);
				if (home != null)
				{
					try
					{
						Comparator<Spid> cmp = new Comparator<Spid>()
						{

							@Override
							public int compare(Spid o1, Spid o2)
							{
								return o1.getId() - o2.getId();
							}
						};

						home = new SortingHome(home, cmp);
						Collection col = home.selectAll(context);
						if (col != null && !col.isEmpty())
						{
							spid =
							    ((SpidAware) col.iterator().next()).getSpid();
						}
					}
					catch (HomeException exception)
					{
						LogSupport
						    .info(
						        context,
						        this,
						        "Exception caught when looking up first available SPID for current user",
						        exception);
					}
				}
			}
			criteria.setIdentificationGroupList(getEmptyIdentificationGroups(
			    context, spid));
		}
	}

	private List getEmptyIdentificationGroups(Context ctx, int spid)
	{
		SpidIdentificationGroups idGroups =
		    getSpidIdentificationGroups(ctx, spid);

		if (idGroups != null)
		{
			return AccountSupport.createEmptyAccountIdentificationGroupsList(
			    ctx, idGroups);

		}

		return new ArrayList();
	}

	private SpidIdentificationGroups getSpidIdentificationGroups(Context ctx,
	    int spid)
	{
		try
		{
			Home home = (Home) ctx.get(SpidIdentificationGroupsHome.class);
			SpidIdentificationGroups idGroups =
			    (SpidIdentificationGroups) home.find(new EQ(
			        SpidIdentificationGroupsXInfo.SPID, Integer.valueOf(spid)));
			return idGroups;
		}
		catch (Exception e)
		{
			if (LogSupport.isDebugEnabled(ctx))
			{
				LogSupport
				    .debug(
				        ctx,
				        this,
				        "Exception caught while attempting to populate account ID list",
				        e);
			}
			return null;
		}
	}
}
