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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.DuplicateAccountDetectionActionEnum;
import com.trilogy.app.crm.bean.DuplicateAccountDetectionMethodEnum;
import com.trilogy.app.crm.bean.IdentificationGroup;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsHome;
import com.trilogy.app.crm.bean.SpidIdentificationGroupsXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.AccountIdentification;
import com.trilogy.app.crm.bean.account.AccountIdentificationGroup;
import com.trilogy.app.crm.bean.account.AccountIdentificationXInfo;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionForm;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResult;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResultHome;
import com.trilogy.app.crm.bean.duplicatedetection.DuplicateAccountDetectionResultTransientHome;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteria;
import com.trilogy.app.crm.bean.duplicatedetection.IdentificationDetectionCriteriaProperty;
import com.trilogy.app.crm.bean.duplicatedetection.NameDOBDetectionCriteria;
import com.trilogy.app.crm.support.DuplicateAccountDetectionSupport;
import com.trilogy.app.crm.support.IdentificationSupport;
import com.trilogy.app.crm.web.border.DuplicateAccountDetectionRedirectBorder;

/**
 * Search agent for duplicate account detection.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-09-27
 */
public class DuplicateAccountDetectionSearchAgentV2 extends ContextAgentProxy
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 2L;

	public static final String SEARCH_PERFORMED =
	    "DuplicateAccountDetection.searchPerformed";
	public static final String UPDATE_IDENTIFICATION_GROUPS =
	    "DuplicateAccountDetection.updateIdentificationGroups";
	public static final String DISPLAY_CONTINUE_BUTTON =
	    "DuplicateAccountDetection.displayContinueButton";
	public static final String DISPLAY_RESULT_MESSAGE =
	    "DuplicateAccountDetection.displayResultMessage";
	public static final String DISPLAY_ERROR =
	    "DuplicateAccountDetection.displayError";

	/**
	 * Constructor for DuplicateAccountDetectionSearchAgent.
	 */
	public DuplicateAccountDetectionSearchAgentV2()
	{
		super();
	}

	/**
	 * Constructor for DuplicateAccountDetectionSearchAgent.
	 * 
	 * @param delegate
	 */
	public DuplicateAccountDetectionSearchAgentV2(final ContextAgent delegate)
	{
		super(delegate);
	}

	@Override
	public void execute(final Context context) throws AgentException
	{
		try
		{
			DuplicateAccountDetectionForm criteria =
			    (DuplicateAccountDetectionForm) SearchBorder
			        .getCriteria(context);

			// Create transient home for the results.
			Home home =
			    new SortingHome(context,
			        new DuplicateAccountDetectionResultTransientHome(context),
			        DuplicateAccountDetectionResultComparator.instance());
			context.put(DuplicateAccountDetectionResultHome.class, home);

			// Get search SPID.
			CRMSpid spid =
			    DuplicateAccountDetectionSupport.getCurrentSearchSpid(context,
			        criteria);

			int prevSpid =
			    context
			        .getInt(
			            DuplicateAccountDetectionRedirectBorder.PARAMETER_PREVIOUS_DETECTION_SPID,
			            DuplicateAccountDetectionForm.DEFAULT_SPID);

			int prevBillingType =
			    context
			        .getInt(
			            DuplicateAccountDetectionRedirectBorder.PARAMETER_PREVIOUS_BILLING_TYPE,
			            ((int) SubscriberTypeEnum.PREPAID_INDEX));

			if (SubscriberTypeEnum.PREPAID.equals(criteria.getSystemType()))
			{
				context.put(SEARCH_PERFORMED, false);
				context.put(UPDATE_IDENTIFICATION_GROUPS, false);
				context.put(DISPLAY_ERROR, false);
				context.put(DISPLAY_CONTINUE_BUTTON, true);
			}
			else if (spid == null)
			{
				context.put(SEARCH_PERFORMED, false);
				context.put(UPDATE_IDENTIFICATION_GROUPS, true);
				context.put(DISPLAY_ERROR, false);
				context.put(DISPLAY_CONTINUE_BUTTON, false);
			}
			else if (criteria.getSpid() == DuplicateAccountDetectionForm.DEFAULT_SPID)
			{
				/*
				 * SPID not set in search criteria. This means the first time
				 * the
				 * duplicate search screen has appeared in this search sequence.
				 * In
				 * this case, no validation or search will be performed. The
				 * "Continue" button is displayed if the first SPID to appear in
				 * the
				 * drop down does not have Duplicate Check enabled.
				 */
				context.put(SEARCH_PERFORMED, false);
				boolean enabled =
				    DuplicateAccountDetectionSupport
				        .isDuplicateAccountDetectionEnabled(context, spid);
				if (!enabled)
				{
					context.put(DISPLAY_CONTINUE_BUTTON, true);
					context
					    .put(DISPLAY_RESULT_MESSAGE,
					        "Duplicate Account Detection is not enabled for this Service Provider.");
					context.put(UPDATE_IDENTIFICATION_GROUPS, false);
					context.put(DISPLAY_ERROR, false);
				}
				else
				{
					context.put(UPDATE_IDENTIFICATION_GROUPS, true);
				}
			}
			else if (criteria.getSpid() != prevSpid)
			{
				context.put(UPDATE_IDENTIFICATION_GROUPS, true);
			}
			else if (criteria.getSystemType() != null
			    && prevBillingType == SubscriberTypeEnum.PREPAID_INDEX)
			{
				context.put(UPDATE_IDENTIFICATION_GROUPS, true);
			}
			else
			{
				/*
				 * The typical case: a search is performed based on the criteria
				 * provided.
				 */

				DuplicateAccountDetectionCommand command =
				    NullDuplicateDetectionCommand.instance();

				try
				{
					/*
					 * If the currently selected SPID does not have Duplicate
					 * Check
					 * enabled, createDuplicateDetectionCommand takes care of
					 * that.
					 */
					command =
					    createDuplicateDetectionCommand(context, criteria, spid);
				}
				catch (AgentException exception)
				{
					context.put(DISPLAY_ERROR, true);
					context.put(SEARCH_PERFORMED, false);
					context.put(DISPLAY_CONTINUE_BUTTON, false);
					context
					    .put(DISPLAY_RESULT_MESSAGE,
					        "Duplicate Account Detection has not been performed due to the above error(s).");
					throw exception;
				}

				/*
				 * Search results are returned as a collection, which needs to
				 * be
				 * copied to the transient home for the TableWebControl to
				 * display.
				 */
				Collection results = null;
				try
				{
					results =
					    DuplicateAccountDetectionProcessor.instance().execute(
					        context, command);
					adaptResultsToHome(context, home, results);

					context.put(SEARCH_PERFORMED, true);
					context.put(DISPLAY_ERROR, false);

					/*
					 * Null command means Duplicate Check is disabled for the
					 * selected SPID.
					 */
					if (command instanceof NullDuplicateDetectionCommand)
					{
						context
						    .put(
						        DISPLAY_RESULT_MESSAGE,
						        "Duplicate Account Detection is disabled for the currently selected Service Provider.");
						context.put(DISPLAY_CONTINUE_BUTTON, true);
					}

					/*
					 * Empty results means no match, so regardless of the action
					 * setting, the user can continue to create an account.
					 */
					else if (results.isEmpty())
					{
						context
						    .put(DISPLAY_RESULT_MESSAGE,
						        "Duplicate Account Detection has found no matches for the criteria entered.");
						context.put(DISPLAY_CONTINUE_BUTTON, true);
					}
					else
					{
						/*
						 * Matches are found. The user is only allowed to
						 * continue
						 * if Duplicate Check action is set to Warn in the SPID.
						 */
						context.put(
						    DISPLAY_RESULT_MESSAGE,
						    "Duplicate Account Detection has found "
						        + results.size()
						        + (results.size() == 1 ? " account"
						            : " accounts")
						        + " matching the criteria entered.");

						context.put(DISPLAY_CONTINUE_BUTTON,
						    DuplicateAccountDetectionActionEnum.WARN
						        .equals(DuplicateAccountDetectionSupport
						            .getSpidDetectionAction(context,
						                spid.getSpid())));
					}

				}
				catch (HomeException exception)
				{
					LogSupport.minor(context, this, "Exception caught",
					    exception);
					context.put(DISPLAY_ERROR, true);
					context.put(SEARCH_PERFORMED, false);
					context.put(DISPLAY_CONTINUE_BUTTON, false);
					context
					    .put(DISPLAY_RESULT_MESSAGE,
					        "Internal error(s) encountered while performing Duplicate Account Detection.");
					throw new AgentException(exception);
				}
			}
			context.put(DuplicateAccountDetectionForm.class, criteria);
		}
		catch (AgentException exception)
		{
			HTMLExceptionListener exceptionListener =
			    (HTMLExceptionListener) context.get(ExceptionListener.class);
			if (exceptionListener != null)
			{
				exceptionListener.thrown(exception);
			}
		}
		delegate(context);
	}

	/**
	 * Creates the duplicate detection command.
	 * 
	 * @param context
	 *            The operating context.
	 * @param criteria
	 *            Search criteria.
	 * @param spid
	 *            SPID of the current search.
	 * @return The duplicate detection command.
	 * @throws AgentException
	 *             Thrown if there are problems building the command.
	 */
	private DuplicateAccountDetectionCommand createDuplicateDetectionCommand(
	    final Context context, DuplicateAccountDetectionForm criteria,
	    CRMSpid spid) throws AgentException
	{
		DuplicateAccountDetectionCommand command =
		    NullDuplicateDetectionCommand.instance();

		if (!DuplicateAccountDetectionSupport
		    .isDuplicateAccountDetectionEnabled(context, spid))
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(
				    context,
				    this,
				    "Duplicate Detection is not enabled for SPID "
				        + spid.getSpid() + "; exiting");
			}
			return command;
		}
		DuplicateAccountDetectionMethodEnum method =
		    DuplicateAccountDetectionSupport.getSpidDetectionMethod(context,
		        spid.getSpid());

		if (method == null)
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this,
				    "No duplicate account detection configured for SPID "
				        + spid.getSpid());
			}
		}
		else if (DuplicateAccountDetectionMethodEnum.NAME_DOB.equals(method))
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(
				    context,
				    this,
				    "Name and date of birth search configured for SPID "
				        + spid.getSpid());
			}

			command =
			    new NameDOBDuplicateDetectionCommand(
			        createNameDOBDetectionCriteria(context, criteria, spid));
		}
		else if (DuplicateAccountDetectionMethodEnum.ID.equals(method))
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this,
				    "ID search configured for SPID " + spid.getSpid());
			}

			command =
			    new IdentificationDuplicateDetectionCommand(
			        createIdentificationDetectionCriteria(context, criteria,
			            spid));
		}
		else
		{
			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this,
				    "Duplicate detection method not understood; exiting");
				return NullDuplicateDetectionCommand.instance();
			}
		}
		return command;
	}

	/**
	 * Adapts the search results to the transient home.
	 * 
	 * @param context
	 *            The operating context.
	 * @param home
	 *            The transient home to store the search results.
	 * @param results
	 *            The result set.
	 * @throws HomeException
	 *             Thrown if errors are encountered while storing the results to
	 *             the home.
	 */
	private void adaptResultsToHome(final Context context, Home home,
	    Collection results) throws HomeException
	{
		if (results != null)
		{
			int id = 0;
			for (Object obj : results)
			{
				try
				{
					DuplicateAccountDetectionResult row =
					    (DuplicateAccountDetectionResult) obj;
					row.setId(id++);
					row =
					    (DuplicateAccountDetectionResult) home.create(context,
					        row);
				}
				catch (HomeException exception)
				{
					LogSupport.minor(context, this,
					    "Exception caught while adding the results to home",
					    exception);
					throw exception;
				}
				catch (Exception exception)
				{
					LogSupport.minor(context, this,
					    "Exception caught while adding the results to home",
					    exception);
					throw new HomeException(exception);
				}
			}
		}
	}

	private List createNonNullListReference(List list)
	{
		return (list == null) ? Collections.EMPTY_LIST : list;
	}

	/**
	 * Creates the IdentificationDetectionCriteria object from the search
	 * criteria.
	 * 
	 * @param context
	 *            The operating context.
	 * @param criteria
	 *            The search criteria.
	 * @param spid
	 *            The currently-searched SPID.
	 * @return The IdentificationDetectionCriteria object as created from the
	 *         search criteria.
	 * @throws AgentException
	 *             Thrown if there are problems creating the criteria.
	 */
	private IdentificationDetectionCriteria
	    createIdentificationDetectionCriteria(final Context context,
	        DuplicateAccountDetectionForm criteria, CRMSpid spid)
	        throws AgentException
	{
		IdentificationDetectionCriteria searchCriteria =
		    new IdentificationDetectionCriteria();
		List<IdentificationDetectionCriteriaProperty> list =
		    new ArrayList<IdentificationDetectionCriteriaProperty>();
		CompoundIllegalStateException el = new CompoundIllegalStateException();

		SpidIdentificationGroups idGroups =
		    getSpidIdentificationGroups(context, spid.getSpid());

		if (idGroups == null)
		{
			throw new AgentException("Identification group settings for SPID "
			    + spid.getSpid() + " cannot be found in the system.");
		}

		List<IdentificationGroup> idGroupList =
		    createNonNullListReference(idGroups.getGroups());
		List<AccountIdentificationGroup> criteriaGroupList =
		    createNonNullListReference(criteria.getIdentificationGroupList());

		if (criteriaGroupList.size() != idGroupList.size())
		{
			throw new AgentException(
			    "Number of identification groups configured for SPID "
			        + spid.getSpid()
			        + " and provided by the search criteria were differnet.");
		}

		for (int i = 0; i < criteriaGroupList.size(); i++)
		{
			AccountIdentificationGroup criteriaGroup = criteriaGroupList.get(i);
			IdentificationGroup idGroup =
			    findCorrespondingSpidIdGroup(criteriaGroup, idGroupList, i);

			List<AccountIdentification> groupIdList =
			    createNonNullListReference(criteriaGroup
			        .getIdentificationList());

			if (groupIdList.size() != idGroup.getRequiredNumber())
			{
				throw new AgentException(
				    "Insufficient number of identifications specified in ID group "
				        + idGroup.getName());
			}

			for (AccountIdentification acctId : groupIdList)
			{
				validateId(context, acctId, idGroup, el);

				IdentificationDetectionCriteriaProperty property =
				    (IdentificationDetectionCriteriaProperty) AccountIdentificationDetectionCriteriaPropertyAdapter
				        .instance().adapt(context, acctId);
				list.add(property);
			}
		}

		searchCriteria.setSpid(spid.getSpid());
		searchCriteria.setIdentifications(list);
		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this, "Search criteria = "
			    + searchCriteria.toString());
		}
		return searchCriteria;
	}

	private void validateId(final Context context,
	    AccountIdentification acctId, IdentificationGroup idGroup,
	    CompoundIllegalStateException el) throws AgentException
	{
		validateIdType(acctId, idGroup);

		IdentificationSupport.validateIdNumber(context, acctId.getIdType(),
		    acctId.getIdNumber(), acctId.getExpiryDate(),
		    AccountIdentificationXInfo.ID_TYPE,
		    AccountIdentificationXInfo.ID_NUMBER,
		    AccountIdentificationXInfo.EXPIRY_DATE, el);
		if (el.getSize() > 0)
		{
			throw new AgentException("Idenification \"" + acctId.getIdNumber()
			    + "\" does not conform to the format of ID type "
			    + acctId.getIdType());
		}

	}

	private void validateIdType(AccountIdentification acctId,
	    IdentificationGroup idGroup) throws AgentException
	{
		if (acctId.getIdType() < 0)
		{
			throw new AgentException(
			    "One or more required identifications are missing.");
		}

		if (!idGroup.isAcceptAny())
		{
			String matchedId = null;
			for (String id : (Set<String>) idGroup.getIdentificationList())
			{
				if (SafetyUtil.safeEquals(String.valueOf(acctId.getIdType()),
				    id))
				{
					matchedId = id;
					break;
				}
			}
			if (matchedId == null)
			{
				throw new AgentException("Identification type "
				    + acctId.getIdType() + " is not accepted by ID group "
				    + idGroup.getName());
			}
		}
	}

	private IdentificationGroup findCorrespondingSpidIdGroup(
	    AccountIdentificationGroup criteriaGroup,
	    List<IdentificationGroup> idGroupList, int i)
	{
		IdentificationGroup idGroup = null;
		for (IdentificationGroup elem : idGroupList)
		{
			if (elem.getIdGroup() == criteriaGroup.getIdGroup())
			{
				idGroup = elem;
				break;
			}
		}

		if (idGroup == null)
		{
			idGroup = idGroupList.get(i);
		}
		return idGroup;
	}

	/**
	 * Creates the NameDOBDetectionCriteria object from the search criteria.
	 * 
	 * @param context
	 *            The operating context.
	 * @param criteria
	 *            The search criteria.
	 * @param spid
	 *            The currently-searched SPID.
	 * @return The NameDOBDetectionCriteria object as created from the search
	 *         criteria.
	 * @throws AgentException
	 *             Thrown if not all of the mandatory fields were provided.
	 */
	private NameDOBDetectionCriteria createNameDOBDetectionCriteria(
	    final Context context, DuplicateAccountDetectionForm criteria,
	    CRMSpid spid) throws AgentException
	{
		if (criteria.getFirstName() == null
		    || criteria.getFirstName().trim().isEmpty())
		{
			LogSupport.info(context, this,
			    "First name was not provided for this duplicate search");
			throw new AgentException(
			    "First name was not provided for this duplicate search.");
		}
		else if (criteria.getLastName() == null
		    || criteria.getLastName().trim().isEmpty())
		{
			LogSupport.info(context, this,
			    "Last name was not provided for this duplicate search");
			throw new AgentException(
			    "Last name was not provided for this duplicate search.");
		}
		else if (criteria.getDateOfBirth() == null
		    || criteria.getDateOfBirth().getTime() == 0)
		{
			LogSupport.info(context, this,
			    "Date of birth was not provided for this duplicate search");
			throw new AgentException(
			    "Date of birth was not provided for this duplicate search.");
		}

		NameDOBDetectionCriteria searchCriteria =
		    new NameDOBDetectionCriteria();
		searchCriteria.setSpid(spid.getSpid());
		searchCriteria.setFirstName(criteria.getFirstName());
		searchCriteria.setLastName(criteria.getLastName());
		searchCriteria.setDateOfBirth(criteria.getDateOfBirth());

		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this, "Search criteria = "
			    + searchCriteria.toString());
		}
		return searchCriteria;
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
