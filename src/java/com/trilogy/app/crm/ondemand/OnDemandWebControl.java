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
package com.trilogy.app.crm.ondemand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.home.SynchronizedHome;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.DateTimeWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * A customized web control for OnDemandValue objects.  This class is abstract
 * because it requires the calculate() to be implemented so that it can get the
 * on-demand value when necessary.  This web control caches the values that it
 * presents.
 *
 * When asked to display a value, this web control first checks the URL to see
 * if the value should be (re)calculated.  If so, then it calls calculate(),
 * saves the value to its cache, then presents the value using the given
 * delegate web control.  If not, then it checks to see if a value is in the
 * cache and presents one if found.
 *
 * When presenting a value, the calculation date is shown along with a
 * "Recalculate" link to allow the value to be updated.  When no value is to be
 * presented, then only a "Calculate" link is shown.
 *
 * The cache is a LRU cache, and the OnDemandValues contained within have a
 * timeout after which they are treated as stale.
 *
 * @author gary.anderson@redknee.com
 */
public abstract
class OnDemandWebControl
    implements WebControl
{
    /**
     * The URL request parameter name used to determine when a calculation is
     * required.
     */
    public static final String KEY = "OnDemand_calculate";

    /**
     * Creates a new OnDemandWebControl.
     *
     * @param property Identifies which property of a model this web control
     * loads on-demand.
     * @param identity Used to identify instances of the model for which this
     * web control loads values on-demand.
     * @param delegate The delegate web control used to display the cached
     * values.
     */
    public OnDemandWebControl(
        final String property,
        final IdentitySupport identity,
        final WebControl delegate)
    {
        property_ = property;
        identity_ = identity;
        delegate_ = delegate;

        cache_ =
            new SynchronizedHome(
                new OnDemandCleanupHome(
                    new LRUCachingHome(
                        DEFAULT_LRU_ENTRIES,
                        true,
                        new NullHome())));

        //stringWC_ = new TextFieldWebControl(AS_OF_TEXT_FIELD_WIDTH);
        dateWC_ = new DateTimeWebControl();
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(
        final Context context,
        final ServletRequest req,
        final String name)
    {
        return delegate_.fromWeb(context, req, name);
    }


    /**
     * {@inheritDoc}
     */
    public void fromWeb(
        final Context context,
        final Object obj,
        final ServletRequest req,
        final String name)
    {
        final OnDemandValue value = (OnDemandValue)obj;
        delegate_.fromWeb(context, value.getPayload(), req, name);
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(
        final Context context,
        final PrintWriter out,
        final String name,
        final Object obj)
    {
        final Object bean = context.get(AbstractWebControl.BEAN);

        OnDemandValue value = (OnDemandValue)obj;
        if (value == null)
        {
            value = new OnDemandValue();

            try
            {
               value.setKey(identity_.toStringID(identity_.ID(bean)));
            }
            catch (IllegalArgumentException e)
            {
               return;
            }
        }

        try
        {
            final Object cachedValue = cache_.find(context,value);
            if (cachedValue != null)
            {
                value = (OnDemandValue)cachedValue;
            }
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(this, "Failure during cache look-up.", exception).log(context);
        }

        final boolean shouldLoad;
        final boolean displayMode;
        {
            final HttpServletRequest request = OnDemandSupport.getRequest(context);
            shouldLoad = property_.equals(request.getParameter(KEY));
            displayMode = "display".equals(request.getParameter("mode"));
        }

        if (shouldLoad)
        {
            final Object newValue = calculate(context, bean);

            value.setValid(true);
            value.setCalculationDate(new Date());
            value.setPayload(newValue);

            try
            {
                value = (OnDemandValue)cache_.create(context,value);
            }
            catch (final HomeException exception)
            {
                new MinorLogMsg(this, "Failed to cache value.", exception).log(context);
            }
        }

        if (value.isValid())
        {
            writeRecalculateLink(context, name, value, displayMode, out);
        }
        else
        {
            writeCalculateLink(context, name, displayMode, out);
        }
    }


    /**
     * Gets the value currently cached for the given bean.
     *
     * @param context The operating context.
     * @param bean The bean for which to retrieve the value.
     *
     * @return The value cached for the given bean, or null if no value is
     * cached.
     */
    public Object getValue(final Context context, final Object bean)
    {
        OnDemandValue value = new OnDemandValue();
        value.setKey(identity_.toStringID(identity_.ID(bean)));

        try
        {
            final Object cachedValue = cache_.find(context,value);
            if (cachedValue != null)
            {
                value = (OnDemandValue)cachedValue;
            }
        }
        catch (final HomeException exception)
        {
            new MinorLogMsg(this, "Failure during cache look-up.", exception).log(context);
        }

        final Object payload;
        if (value.isValid())
        {
            payload = value.getPayload();
        }
        else
        {
            payload = null;
        }

        return payload;
    }


    /**
     * Calculate the value to be cached.
     *
     * @param context The operating context.
     * @param bean The bean from which the on-demand property comes.
     * @return The object that should be cached as the on-demand value.
     */
    public abstract Object calculate(final Context context, final Object bean);


    /**
     * Sets the calculate link text.  This value will override the value in the
     * message manager.  If set to null, then the text will be retrieved from
     * the message manager.
     *
     * @param text The calculate link text.
     */
    public final void setCalculateLinkText(final String text)
    {
        calculateLinkText_ = text;
    }


    /**
     * Sets the recalculate link text.  This value will override the value in
     * the message manager.  If set to null, then the text will be retrieved
     * from the message manager.
     *
     * @param text The recalculate link text.
     */
    public final void setRecalculateLinkText(final String text)
    {
        recalculateLinkText_ = text;
    }


    /**
     * Gets the text that should be shown in the "Calculate" link.
     *
     * @param context The operating context.
     * @return The text that should be shown in the "Calculate" link.
     */
    private String getCalculateLinkText(final Context context)
    {
        final String linkLabel;

        if (calculateLinkText_ == null)
        {
            final MessageMgr manager = new MessageMgr(context, this);
            linkLabel =
                manager.get("OnDemandWebControl.calculate.label", DEFAULT_CALCULATE_LINK_TEXT);
        }
        else
        {
            linkLabel = calculateLinkText_;
        }

        return linkLabel;
    }


    /**
     * Gets a String version of the given date in a format meant for displaying
     * in the toWeb() method.
     *
     * @param context The operating context.
     * @param name The domain name of the object.
     * @param date The date to represent.
     *
     * @return A String version of the given date.
     */
    private String getDateToWeb(
        final Context context,
        final String name,
        final Date date)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        dateWC_.toWeb(context, printWriter, name, date);
        printWriter.flush();
        final String dateString = stringWriter.getBuffer().toString();

        return dateString;
    }


    /**
     * Gets a String version of the given payload in a format meant for
     * displaying in the toWeb() method.
     *
     * @param context The operating context.
     * @param name The domain name of the object.
     * @param object The payload of the on-demand cached value.
     *
     * @return A String version of the given payload.
     */
    private String getPayloadToWeb(
        final Context context,
        final String name,
        final Object object)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        delegate_.toWeb(context, printWriter, name, object);
        printWriter.flush();
        final String payload = stringWriter.getBuffer().toString();

        return payload;
    }


    /**
     * Geta the String version of the "Recalculate" link.
     *
     * @param context The operating context.
     * @param displayMode True if the link should cause the page to be refreshed
     * in display-only mode; false otherwise.
     *
     * @return The String version of the "Recalculate" link.
     */
    private String getRecalculateLink(
        final Context context,
        final boolean displayMode)
    {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        final String linkLabel = getRecalculateLinkText(context);
        writeLink(context, printWriter, linkLabel, displayMode);

        printWriter.flush();
        final String link = stringWriter.getBuffer().toString();

        return link;
    }


    /**
     * Gets the text that should be shown in the "Recalculate" link.
     *
     * @param context The operating context.
     * @return The text that should be shown in the "Recalculate" link.
     */
    private String getRecalculateLinkText(final Context context)
    {
        final String linkLabel;

        if (recalculateLinkText_ == null)
        {
            final MessageMgr manager = new MessageMgr(context, this);
            linkLabel =
                manager.get("OnDemandWebControl.recalculate.label", DEFAULT_RECALCULATE_LINK_TEXT);

            return linkLabel;
        }

        linkLabel = recalculateLinkText_;

        return linkLabel;
    }


    /**
     * Writes the calculate link for the on-demand value.
     *
     * @param context The operating context.
     * @param name The domain name of the component.
     * @param displayMode True if the link should cause the page to be refreshed
     * in display-only mode; false otherwise.
     * @param out The PrintWriter to which the link should be written.
     */
    private void writeCalculateLink(
        final Context context,
        final String name,
        final boolean displayMode,
        final PrintWriter out)
    {
        final String linkLabel = getCalculateLinkText(context);
        writeLink(context, out, linkLabel, displayMode);
    }


    /**
     * Writes a calculate or recalculate link.
     *
     * @param context The operating context.
     * @param out The write to which the link is written.
     * @param linkText The user-visible text of the link.
     * @param displayMode True if the link should cause the page to be refreshed
     * in display-only mode; false otherwise.
     */
    private void writeLink(
        final Context context,
        final PrintWriter out,
        final String linkText,
        final boolean displayMode)
    {
        out.print("<a href=\"#\" onclick=\"");

        out.print("document.forms[0].");
        out.print(KEY);
        out.print(".value='");
        out.print(property_);
        out.print("'; ");

        out.print("document.forms[0].elements['PreviewButtonSrc'].value='");
        out.print(property_);
        out.print("'; ");

        if (displayMode)
        {
            out.print("document.forms[0].action=document.forms[0].action+'?mode=display'; ");
        }

        out.print("document.forms[0].submit(); ");

        out.print("\">");
        out.print(linkText);
        out.println("</a>");
    }


    /**
     * Writes the recalculate link for the on-demand value.
     *
     * @param context The operating context.
     * @param name The domain name of the component.
     * @param value The value to be written with the link.
     * @param displayMode True if the link should cause the page to be refreshed
     * in display-only mode; false otherwise.
     * @param out The PrintWriter to which the link should be written.
     */
    private void writeRecalculateLink(
        final Context context,
        final String name,
        final OnDemandValue value,
        final boolean displayMode,
        final PrintWriter out)
    {
        final String payload = getPayloadToWeb(context, name, value.getPayload());
        final String date = getDateToWeb(context, name, value.getCalculationDate());

        final String link = getRecalculateLink(context, displayMode);

        final MessageMgr manager = new MessageMgr(context, this);

        final String description =
            manager.get(
                "OnDemandWebControl.value",
                DEFAULT_RECALCULATE_LINK_FORMAT,
                new String[] { payload, date, link });

        out.print(description);
    }


    /**
     * Teh width of the "as of" message text field.
     */
    //private static final int AS_OF_TEXT_FIELD_WIDTH = 128;

    /**
     * The default number of entries to keep in the LRU cache.
     */
    private static final int DEFAULT_LRU_ENTRIES = 500;

    /**
     * The dafault message format for the ondemand value and recalculate link
     * presentation.
     */
    private static final String DEFAULT_RECALCULATE_LINK_FORMAT = "{0} (as of {1}) {2}";

    /**
     * The default "Calculate" link text.
     */
    private static final String DEFAULT_CALCULATE_LINK_TEXT = "Calculate";

    /**
     * The default "Calculate" link text.
     */
    private static final String DEFAULT_RECALCULATE_LINK_TEXT = "Recalculate";

    /**
     * The calculate link text to use to override the default text.  When null,
     * the default will be taken from the message manager.
     */
    private String calculateLinkText_;

    /**
     * The recalculate link text to use to override the default text.  When
     * null, the default will be taken from the message manager.
     */
    private String recalculateLinkText_;

    /**
     * The name of the property for which this web control operates.
     */
    private final String property_;

    /**
     * Used to get the identity of the bean.
     */
    private final IdentitySupport identity_;

    /**
     * The web control to which we delegate for displaying the payload.
     */
    private final WebControl delegate_;

    /**
     * The cache of values for this webcontrol.
     */
    private final Home cache_;

    /**
     * Used to display the "as of" message.
     */
    //private final TextFieldWebControl stringWC_;

    /**
     * Used to display the date of the "as of" message.
     */
    private final DateTimeWebControl dateWC_;

} // class
