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
package com.trilogy.app.crm.home.validator;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * @author victor.stratan@redknee.com
 */
public class TestAuxiliaryServiceOneToOneServiceOptionValidator extends ContextAwareTestCase
{
    private static final int SPID = 12;
    private static final long SERVICE_OPTION = 1234L;
    private static final long AUX_SRV_IDENTIFIER = 6543L;

    public TestAuxiliaryServiceOneToOneServiceOptionValidator()
    {
        super();
    }

    public TestAuxiliaryServiceOneToOneServiceOptionValidator(final String name)
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

        final TestSuite suite = new TestSuite(TestAuxiliaryServiceOneToOneServiceOptionValidator.class);

        return suite;
    }

    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();
        this.auxSrvHome_ = new AuxiliaryServiceTransientHome(ctx);
        ctx.put(AuxiliaryServiceHome.class, this.auxSrvHome_);

        this.bean_ = new AuxiliaryService();
        this.bean_.setType(AuxiliaryServiceTypeEnum.URCS_Promotion);
        this.bean_.setSpid(SPID);
        Collection<Extension> extensions = new ArrayList<Extension>();

        URCSPromotionAuxSvcExtension extension = new URCSPromotionAuxSvcExtension();
        extension.setAuxiliaryServiceId(this.bean_.getID());
        extension.setSpid(this.bean_.getSpid());
        extension.setServiceOption(SERVICE_OPTION);
        extensions.add(extension);
    
        this.bean_.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));    

        this.auxSrvTemplate_ = new AuxiliaryService();
        this.auxSrvTemplate_.setIdentifier(AUX_SRV_IDENTIFIER);
        this.auxSrvTemplate_.setType(AuxiliaryServiceTypeEnum.URCS_Promotion);
        this.auxSrvTemplate_.setSpid(SPID);

        this.obj_ = new AuxiliaryServiceOneToOneServiceOptionValidator();
    }

    /**
     * no beans - no exceptions
     */
    public void testValidateWithNoBeansOnCreate()
    {
        final Context ctx = getContext();

        ctx.put(HomeOperationEnum.class, HomeOperationEnum.CREATE);

        try
        {
            this.obj_.validate(ctx, this.bean_);
        }
        catch (IllegalStateException e)
        {
            fail("Validation Exception should NOT be thrown!");
        }
    }

    /**
     * same bean - no exceptions
     */
    public void testValidateWithNoBeansOnStore() throws HomeException
    {
        final Context ctx = getContext();

        ctx.put(HomeOperationEnum.class, HomeOperationEnum.STORE);
        auxSrvHome_.create(ctx, this.bean_);

        try
        {
            this.obj_.validate(ctx, this.bean_);
        }
        catch (IllegalStateException e)
        {
            fail("Validation Exception should NOT be thrown!");
        }
    }

    /**
     * beans not using the same service option - no exceptions
     */
    public void testValidateWithBeansNotUsingServiceOption() throws HomeException
    {
        final Context ctx = getContext();

        ctx.put(HomeOperationEnum.class, HomeOperationEnum.CREATE);
        auxSrvHome_.create(ctx, auxSrvTemplate_);

        try
        {
            this.obj_.validate(ctx, this.bean_);
        }
        catch (IllegalStateException e)
        {
            fail("Validation Exception should NOT be thrown!");
        }
    }

    /**
     * beans are using the same service option  - validation exceptions
     */
    public void testValidateWithBeansUsingServiceOption() throws HomeException
    {
        final Context ctx = getContext();

        ctx.put(HomeOperationEnum.class, HomeOperationEnum.CREATE);
        Collection<Extension> extensions = new ArrayList<Extension>();

        URCSPromotionAuxSvcExtension extension = new URCSPromotionAuxSvcExtension();
        extension.setAuxiliaryServiceId(auxSrvTemplate_.getID());
        extension.setSpid(auxSrvTemplate_.getSpid());
        extension.setServiceOption(SERVICE_OPTION);
        extensions.add(extension);
    
        auxSrvTemplate_.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));    
        auxSrvHome_.create(ctx, auxSrvTemplate_);

        try
        {
            this.obj_.validate(ctx, this.bean_);
            fail("Validation Exception expected but not thrown!");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    /**
     * beans are using the same service option but on different spid - no exceptions
     */
    public void testValidateWithBeansUsingServiceOptionButOnDiferentSPID() throws HomeException
    {
        final Context ctx = getContext();

        ctx.put(HomeOperationEnum.class, HomeOperationEnum.CREATE);
        auxSrvTemplate_.setSpid(this.bean_.getSpid() + 1);
        Collection<Extension> extensions = new ArrayList<Extension>();

        URCSPromotionAuxSvcExtension extension = new URCSPromotionAuxSvcExtension();
        extension.setAuxiliaryServiceId(auxSrvTemplate_.getID());
        extension.setSpid(auxSrvTemplate_.getSpid());
        extension.setServiceOption(SERVICE_OPTION);
        extensions.add(extension);
    
        auxSrvTemplate_.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));    
        auxSrvHome_.create(ctx, auxSrvTemplate_);

        try
        {
            this.obj_.validate(ctx, this.bean_);
        }
        catch (IllegalStateException e)
        {
            fail("Validation Exception should NOT be thrown!");
        }
    }

    /**
     * beans are using the same service option but are closed - no exceptions
     */
    public void testValidateWithBeansUsingServiceOptionButClosed() throws HomeException
    {
        final Context ctx = getContext();

        ctx.put(HomeOperationEnum.class, HomeOperationEnum.CREATE);
        Collection<Extension> extensions = new ArrayList<Extension>();

        URCSPromotionAuxSvcExtension extension = new URCSPromotionAuxSvcExtension();
        extension.setAuxiliaryServiceId(auxSrvTemplate_.getID());
        extension.setSpid(auxSrvTemplate_.getSpid());
        extension.setServiceOption(SERVICE_OPTION);
        extensions.add(extension);
    
        auxSrvTemplate_.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));    
        auxSrvTemplate_.setState(AuxiliaryServiceStateEnum.CLOSED);
        auxSrvHome_.create(ctx, auxSrvTemplate_);

        try
        {
            this.obj_.validate(ctx, this.bean_);
        }
        catch (IllegalStateException e)
        {
            fail("Validation Exception should NOT be thrown!");
        }
    }

    private Validator obj_;
    private AuxiliaryService bean_;
    private AuxiliaryService auxSrvTemplate_;
    private Home auxSrvHome_;
}
