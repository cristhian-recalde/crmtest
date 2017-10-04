package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.EnumerationConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateConfigHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.app.crm.subscriber.state.SubscriberStateTransitionSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * WebControl for use with any Enum class.
 */
public class SubscriberStateChangeAuxSvcReminderWebControl extends EnumWebControl
{

	/**
	 * Create a new instance of <code>SubscriberStateChangeAuxSvcReminderWebControl</code>.
	 *
	 * @param _enum
	 *            Enum collection.
	 */
	public SubscriberStateChangeAuxSvcReminderWebControl(final EnumCollection _enum)
	{
		super(_enum);
	}


	/**
	 * Create a new instance of <code>SubscriberStateChangeAuxSvcReminderWebControl</code>.
	 *
	 * @param _enum
	 *            Enum collection.
	 * @param autoPreview
	 *            Whether auto preview is enabled.
	 */
	public SubscriberStateChangeAuxSvcReminderWebControl(final EnumCollection _enum, final boolean autoPreview)
	{
		super(_enum);
		autoPreview_ = autoPreview;
	}


	/**
	 * Decorates the toWeb2 method to added special behavior for PREPAID accounts in
	 * CREATE mode. When trying to create a new prepaid subscriber
	 * (Individual/non-Individual), the State field (Mobile Number tab) displays Activated
	 * state by default, this should be set to "Available". In fact, the other two states
	 * in the pulldown Pending and Activated may be removed from the list since prepaid
	 * subscriber cannot go into Activated state upon creation and as in TT 5051418308,
	 * Mike has confirmed that prepaid account should not be manually creatable in Pending
	 * state.
	 */
	@Override
	public void toWeb(Context ctx, final PrintWriter out, final String name, Object obj)
	{
		final int mode = ctx.getInt("MODE", DISPLAY_MODE);
		final Subscriber sub = (Subscriber) ctx.get(AbstractWebControl.BEAN);

		if (mode == CREATE_MODE && sub.isPrepaid() && (!SystemSupport.supportsPrepaidCreationInActiveState(ctx)))
		{
			ctx = ctx.createSubContext();
			ctx.put("MODE", DISPLAY_MODE);

			obj = SubscriberStateEnum.AVAILABLE;
			sub.setState(SubscriberStateEnum.AVAILABLE);
		}

		toWeb2(ctx, out, name, obj);
	}


	@Override
	public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
	{
		Object result = super.fromWeb(ctx, req, name);

		if (result instanceof SubscriberStateEnum)
		{
			SubscriberStateEnum state = (SubscriberStateEnum) result;
			if (SubscriberStateEnum.EXPIRED_INDEX == state.getIndex())
			{
				result = SubscriberStateEnum.ACTIVE;
			}
		}

		return result;
	}


	/**
	 * Display the enum collection.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param out
	 *            Output writer.
	 * @param name
	 *            Name of the field.
	 * @param obj
	 *            Object to be displayed.
	 */
	public void toWeb2(final Context ctx, final PrintWriter out, final String name, Object obj)
	{
		final int mode = ctx.getInt("MODE", DISPLAY_MODE);

		final Subscriber newSubscriber = (Subscriber) ctx.get(AbstractWebControl.BEAN);

		final Subscriber oldSubscriber = getOldSubscriber(ctx, newSubscriber);

		if (oldSubscriber != null )
		{
			newSubscriber.setState(newSubscriber.getStateWithExpired());
			oldSubscriber.setState(oldSubscriber.getStateWithExpired());
			obj = newSubscriber.getState();
		}

		final Enum localEnum = (Enum) obj;

		Collection existingActiveAssociations = new ArrayList();

		String allRemainder = "";

		// so it is not valid to do check when creating a new subscriber
		if (oldSubscriber != null)
		{
			try
			{
				existingActiveAssociations = SubscriberAuxiliaryServiceSupport.getActiveSubscriberAuxiliaryServices(
						ctx, oldSubscriber, new Date());
			}
			catch (final HomeException e)
			{
				e.printStackTrace();
			}

			allRemainder = getAllRemaiderFromActiveAuxSve(ctx, existingActiveAssociations, newSubscriber, oldSubscriber);

			out.print("<input type=\"hidden\" id=\"orig_sub_state\" name=\"orig_sub_state\" value=\"");
			out.print(oldSubscriber.getState().getIndex());
			out.print("\" />");
		}

		outputJavaScript(out, allRemainder);

		switch (mode)
		{
		case EDIT_MODE:
		case CREATE_MODE:
			out.print("<select name=\"");
			out.print(name);
			out.print("\" id=\"");
			out.print(name);
			out.print("\" size=\"");
			out.print(String.valueOf(size_));
			out.print("\"");
			if (mode == EDIT_MODE && allRemainder.length() > 0)
			{
				out.print(" onChange=\"axvSvcWarningOnSuspendDisable('");
				out.print(WebAgents.getDomain(ctx));
				out.print("',event, '");
				if (autoPreview_)
				{
					out.print(name + "');autoPreview('', event)\"");
				}
				else
				{
					out.print(name + "')\"");
				}
			}
			else if (autoPreview_)
			{
				out.print(" onChange=\"autoPreview('', event)\"");
			}

			out.println(">");

			//added to fix TT#9021300011
			Account account = null;
			try {
				if(oldSubscriber != null)
					account =oldSubscriber.getAccount(ctx);
				else if(newSubscriber != null)
					account =newSubscriber.getAccount(ctx);
			} catch (HomeException e1) {
			}

			for (final Iterator i = getPossibleStateIterator(ctx, oldSubscriber, newSubscriber); i.hasNext();)
			{
				final Enum e = (Enum) i.next();
				//added to fix TT#9021300011 - If condition
				if(!AccountStateEnum.ACTIVE.equals(account.getState()) && SubscriberStateEnum.ACTIVE.equals(e))
					continue;
				out.print("<option value=\"");
				out.print(String.valueOf(e.getIndex()));
				out.print("\"");
				if (e.equals(localEnum))
				{
					out.print(" selected=\"selected\"");
				}
				out.print(">");
				out.print(getSubscriberStateLabel(ctx, e));
				out.println("</option>");
			}
			out.println("</select>");
			break;

		case DISPLAY_MODE:
		default:
			out.print(localEnum.getDescription());
		}
	}


	/**
	 * Gets the Subscriber as it currently exists in the database.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param newSubscriber
	 *            The updated Subscriber for which we want the old subscriber.
	 * @return The Subscriber as it currently exists in the database.
	 */
	private Subscriber getOldSubscriber(final Context ctx, final Subscriber newSubscriber)
	{
		try
		{
			final Home home = (Home) ctx.get(SubscriberHome.class);
			return (Subscriber) home.find(ctx, newSubscriber.getId());
		}
		catch (final HomeException e)
		{
			final String err = "failed to query subscriber " + newSubscriber.getId() + e;
			new DebugLogMsg(this, err, e);
			final IllegalStateException exp = new IllegalStateException(err);
			exp.initCause(e);
			throw exp;
		}
	}


	private String getAllRemaiderFromActiveAuxSve(final Context ctx, final Collection existingActiveAssociations,
			final Subscriber newSubscriber, final Subscriber subscriber)
	{
		final StringBuilder buf = new StringBuilder();
		final String subId = subscriber.getId();
		// Set allActiveAuxSvc = new HashSet();

		//   Collection allActiveAuxSvcs = new ArrayList();
		try
		{
			PPSMSupporterSubExtension extension = PPSMSupporterSubExtension.getPPSMSupporterSubscriberExtension(ctx, subscriber.getId());
			if (extension!=null)
			{
				extension.setContext(ctx);
				int numOfSupportedSubscribers = extension.getSupportedSubscribers(ctx).size();
				buf.append("Subscriber is a PPSM Supporter for ");
				buf.append(numOfSupportedSubscribers);
				if (numOfSupportedSubscribers==1)
				{
					buf.append(" subscriber;  ");
				}
				else
				{
					buf.append(" subscribers;  ");
				}
			}
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, this, "Unable to retrieve PPSM Supporter extension for subscriber '" + subscriber.getId() + "': " + e.getMessage(), e);
		}

		for (final Iterator<SubscriberAuxiliaryService> i = existingActiveAssociations.iterator(); i.hasNext();)
		{
			final SubscriberAuxiliaryService association = i.next();
			try
			{
				final AuxiliaryService service = association.getAuxiliaryService(ctx);

				if (service.getWarningOnSuspendDisable() != null && service.getWarningOnSuspendDisable().length() > 0)
				{
					// buf.append(service.getWarningOnSuspendDisable() + "&#10;");
					// buf.append(service.getWarningOnSuspendDisable() + "&#13;");
					buf.append(service.getWarningOnSuspendDisable()).append(";  ");
				}
				else if (service.isPrivateCUG(ctx))
				{
					ClosedUserGroup cug = ClosedUserGroupSupport.getCUG(ctx, association.getSecondaryIdentifier(), service.getSpid());
					if (cug != null)
					{
						if (cug.getOwnerMSISDN().equals(subscriber.getMSISDN()))
						{
							buf.append("Subscriber is owner of Private CUG ");
							buf.append(cug.getID());
							buf.append(";  ");
						}
						else
						{
							buf.append("Subscriber is member of Private CUG ");
							buf.append(cug.getID());
							buf.append(";  ");
						}
					}
					else
					{
						LogSupport.minor(ctx,
								this,
								"Subscriber " + subId + " is associated to cug " + association.getSecondaryIdentifier() + ", but this cug could not be found.");
					}
				}

			}
			catch (final HomeException e)
			{
				new MinorLogMsg(
						this,
						"Encountered problem when converting retrieving auxiliary service information for SubscriberAuxiliaryService " + association.getIdentifier() + ": ",
						e).log(ctx);
			}
		}

		//   allActiveAuxSvcs = SubscriberAuxiliaryServiceSupport.getAuxiliaryServiceCollection(ctx,
		//        existingActiveAssociations, null);


		return buf.toString();
	}


	private void outputJavaScript(final PrintWriter out, final String allMsg)
	{
		out.println("<script type=\"text/javascript\">");
		out.println("function axvSvcWarningOnSuspendDisable(domain, event, selectElemName)");
		out.println("{");
		out.println("var oldStateElem = document.getElementById(\"orig_sub_state\");");
		out.println("var oldState = oldStateElem.value;");
		// out.println("alert(\"oldState value=\" + oldState);");
		out.println("var selectElem = document.getElementById(selectElemName);");
		// out.println("alert(\"newState =\" + selectElem.selectedIndex);");
		out.println("var newState = selectElem.value;");
		out.println("if (  (oldState == 3 && newState == 2 ) || ");
		out.println("			(oldState == 1 && newState == 3 ) || ");
		out.println(" 					( oldState == 1 && newState == 2 ) )");
		out.println("{");
		// out.println(" return confirm(\"" + allMsg + "\");");
		out.println("	alert(\"" + allMsg + "\");");
		out.println("}");
		out.println("}");
		out.println("</script>");
	}


	/**
	 * Return the configured label of an enumeration of SubscriberStateEnum.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param enumeration
	 *            The given enumeration of SubscriberStateEnum.
	 * @return The label associated with the given enumeration of SubscriberStateEnum.
	 */
	private String getSubscriberStateLabel(final Context ctx, final Enum enumeration)
	{
		final Home home = (Home) ctx.get(SubscriberStateConfigHome.class);

		String label = null;
		try
		{
			final EnumerationConfig enumConfig = (EnumerationConfig) home.find(ctx, Short.valueOf(enumeration.getIndex()));

			if (enumConfig != null)
			{
				label = enumConfig.getLabel();
			}
		}
		catch (final HomeException e)
		{
			// Empty
		}

		if (label == null || label.trim().length() == 0)
		{
			label = enumeration.getDescription();
		}

		return label;
	}


	/**
	 * Return the iterator of all states to which state transition is possible (i.e., all
	 * states to which state transition is permitted, plus the current state) for the
	 * given subscriber, or simply the iterator of all states if the subscriber is null.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param oldSubscriber
	 *            The given subscriber.
	 * @return The iterator of all states possible for state transition.
	 */
	private Iterator getPossibleStateIterator(final Context ctx, final Subscriber oldSub, final Subscriber newSub)
	{
		// FIXME, we should not allow change type and state at the same time
		final Subscriber typeOfSub;
		if (oldSub == null)
		{
			typeOfSub = newSub;
		}
		else
		{
			typeOfSub = oldSub;
		}

		//		added to fix TT#9021300011
		EnumCollection enumcollectiontemp = null;
		EnumCollection enumcollection = null;
		Account account = null;
		try {
			if(oldSub != null)
				account =oldSub.getAccount(ctx);
			if(account == null)
				account =newSub.getAccount(ctx);
		} catch (HomeException e1) {
		}
		enumcollectiontemp = SubscriberStateTransitionSupport.instance(ctx, typeOfSub).getPossibleManualStateEnumCollection(ctx,
				oldSub);
		enumcollection = enumcollectiontemp;
		enumcollection.removeAllElements();
		Enum e = null;
		for(short i = 0; i < enumcollectiontemp.size(); i++){
			e = (Enum) enumcollectiontemp.getByIndex(i);
			if(!AccountStateEnum.ACTIVE.equals(account.getState()) && SubscriberStateEnum.ACTIVE.equals(e))
				continue;
			enumcollection.addElement(enumcollectiontemp.getElementAt(1));
		}
		return enumcollection.iterator();
	}
}