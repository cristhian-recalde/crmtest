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
package com.trilogy.app.crm.urcs;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.crm.client.urcs.PromotionProvisionClient;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.urcs.promotion.PromotionStatus;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author victor.stratan@redknee.com
 */
public class TestUrcsPromotionProvisionHome extends ContextAwareTestCase
{
    private static final int SPID = 12;
    private static final long SERVICE_OPTION = 1234L;
    private static final long AUX_SRV_IDENTIFIER = 6543L;
    private static final String SUBSCRIBER_ID = "1234-5";

    public TestUrcsPromotionProvisionHome()
    {
        super();
    }

    public TestUrcsPromotionProvisionHome(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     *
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestUrcsPromotionProvisionHome.class);

        return suite;
    }

    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();
        final Home auxSrvHome = new AuxiliaryServiceTransientHome(ctx);
        ctx.put(AuxiliaryServiceHome.class, auxSrvHome);

        final Home subHome = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, subHome);

        client_ = new FakePromotionProvisionClient();
        ctx.put(PromotionProvisionClient.class, client_);

        final AuxiliaryService auxSrv = new AuxiliaryService();
        auxSrv.setIdentifier(AUX_SRV_IDENTIFIER);
        auxSrv.setSpid(SPID);
        auxSrv.setType(AuxiliaryServiceTypeEnum.URCS_Promotion);

        Collection<Extension> extensions = new ArrayList<Extension>();

        URCSPromotionAuxSvcExtension extension = new URCSPromotionAuxSvcExtension();
        extension.setAuxiliaryServiceId(auxSrv.getID());
        extension.setSpid(auxSrv.getSpid());
        extension.setServiceOption(SERVICE_OPTION);
        extensions.add(extension);
    
        auxSrv.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));    
        

        final AuxiliaryService auxSrvBasic = new AuxiliaryService();
        auxSrvBasic.setIdentifier(AUX_SRV_IDENTIFIER + 1);
        auxSrvBasic.setSpid(SPID);
        auxSrvBasic.setType(AuxiliaryServiceTypeEnum.Basic);

        this.bean_ = new SubscriberAuxiliaryService();
        this.bean_.setSubscriberIdentifier(SUBSCRIBER_ID);

        final Subscriber sub = new Subscriber();
        sub.setId(SUBSCRIBER_ID);

        this.obj_ = extension;

        try
        {
            auxSrvHome.create(ctx, auxSrv);
            auxSrvHome.create(ctx, auxSrvBasic);

            subHome.create(ctx, sub);
        }
        catch (HomeException e)
        {
            final String msg = "Test Initialization failed!";
            LogSupport.debug(ctx, this, msg, e);
            fail(msg);
        }
    }

    /**
     * no corresponding Auxiliary service - no exceptions, no calls
     */
    public void testValidateWithNoAuxSrv() throws HomeException
    {
        final Context ctx = getContext();

        // no corresponding Auxiliary service
        this.bean_.setAuxiliaryServiceIdentifier(AUX_SRV_IDENTIFIER - 1);

        try
        {
            this.obj_.associate(ctx, this.bean_);
            assertEquals(0, client_.setSubscriptionPromotionsCallCount);
        }
        catch (Throwable t)
        {
            fail("Exception: " + t.getMessage());
        }
    }

    /**
     * corresponding Auxiliary service is not Promotion - no exceptions, no calls
     */
    public void testValidateWithNonPromotionAuxSrv() throws HomeException
    {
        final Context ctx = getContext();

        // corresponding Auxiliary service is not Promotion
        this.bean_.setAuxiliaryServiceIdentifier(AUX_SRV_IDENTIFIER + 1);

        try
        {
            this.obj_.associate(ctx, this.bean_);
            assertEquals(0, client_.setSubscriptionPromotionsCallCount);
        }
        catch (Throwable t)
        {
            fail("Exception: " + t.getMessage());
        }
    }

    /**
     * provision - check the id
     */
    public void testValidateProvision() throws HomeException
    {
        final Context ctx = getContext();

        this.bean_.setAuxiliaryServiceIdentifier(AUX_SRV_IDENTIFIER);

        try
        {
            this.obj_.associate(ctx, this.bean_);
            assertEquals(1, client_.setSubscriptionPromotionsCallCount);
            assertEquals(1, client_.lastAddOptions.length);
            assertEquals(0, client_.lastRemoveOptions.length);
            assertEquals(SERVICE_OPTION, client_.lastAddOptions[0]);
        }
        catch (Throwable t)
        {
            fail("Exception: " + t.getMessage());
        }
    }

    /**
     * un-provision - check the id
     */
    public void testValidateUnProvision() throws HomeException
    {
        final Context ctx = getContext();

        this.bean_.setAuxiliaryServiceIdentifier(AUX_SRV_IDENTIFIER);

        try
        {
            this.obj_.dissociate(ctx, this.bean_);
            assertEquals(1, client_.setSubscriptionPromotionsCallCount);
            assertEquals(0, client_.lastAddOptions.length);
            assertEquals(1, client_.lastRemoveOptions.length);
            assertEquals(SERVICE_OPTION, client_.lastRemoveOptions[0]);
        }
        catch (Throwable t)
        {
            fail("Exception: " + t.getMessage());
        }
    }

    private URCSPromotionAuxSvcExtension obj_;
    private SubscriberAuxiliaryService bean_;
    private FakePromotionProvisionClient client_;
}

class FakePromotionProvisionClient implements PromotionProvisionClient
{
    public int setSubscriptionPromotionsCallCount;
    long[] lastAddOptions;
    long[] lastRemoveOptions;

    public void setSubscriptionPromotions(final Context ctx, final Subscriber subscription,
            final long[] addOptions, final long[] removeOptions) throws RemoteServiceException
    {
        setSubscriptionPromotionsCallCount++;
        lastAddOptions = addOptions;
        lastRemoveOptions = removeOptions;
    }

    public long[] listSubscriptionPromotions(final Context ctx, final Subscriber subscription)
        throws RemoteServiceException
    {
        return new long[0];
    }

    public PromotionStatus[] listSubscriptionPromotionStatus(final Context ctx, final Subscriber subscription)
        throws RemoteServiceException
    {
        return new PromotionStatus[0];
    }

    public PromotionStatus getSubscriptionPromotionStatus(final Context ctx, final Subscriber subscription,
            final long promotionId) throws RemoteServiceException
    {
        return null;
    }
}