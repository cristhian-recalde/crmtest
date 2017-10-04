/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.queryexecutor;

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.rmi.GenericParameterParser;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.AccountState;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.ReadOnlyAccountBankInfo;
import com.trilogy.util.crmapi.wsdl.v2_1.types.account.ReadOnlyAccountProfile;
import com.trilogy.util.crmapi.wsdl.v3_0.types.ExecuteResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.AccountProfileQueryResults;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountBilling;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountDetail;
import com.trilogy.util.crmapi.wsdl.v3_0.types.account.ReadOnlyAccountIdentification;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.IdentificationEntry;

/**
 * 
 * @author Marcio Marques
 * @since 9.1.3
 *
 */
public class TestExecuteResultAdapter extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestExecuteResultAdapter(final String name)
    {
        super(name);
    }
    
    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }   
    
    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestExecuteResultAdapter.class);

        return suite;
    }
    
    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }
    
    public void testAdapter()
    {
        Context ctx = getContext();
        AccountProfileQueryResults bean = new AccountProfileQueryResults();
        
        ReadOnlyAccountBankInfo bankInfo = new ReadOnlyAccountBankInfo();
        bankInfo.setAddress1("Address 1");
        bankInfo.setAddress2("Address 2");
        bankInfo.setBankID("HSBC");
        bean.setBank(bankInfo);
        ReadOnlyAccountDetail accountDetail = new ReadOnlyAccountDetail();
        accountDetail.setSuspensionReason("reason");
        bean.setDetail(accountDetail);
        ReadOnlyAccountBilling billing = new ReadOnlyAccountBilling();
        billing.setBillCycle(1);
        bean.setBilling(billing);
        ReadOnlyAccountProfile profile = new ReadOnlyAccountProfile();
        profile.setState(AccountState.value3);
        bean.setProfile(profile);
        ReadOnlyAccountIdentification identification = new ReadOnlyAccountIdentification();
        IdentificationEntry[] param = new IdentificationEntry[3];
        param[0] = new IdentificationEntry();
        param[0].setExpiry(Calendar.getInstance());
        param[0].setType(0L);
        param[0].setValue("1234");
        param[1] = new IdentificationEntry();
        param[1].setExpiry(Calendar.getInstance());
        param[1].setType(10L);
        param[1].setValue("4321");
        param[2] = new IdentificationEntry();
        param[2].setExpiry(Calendar.getInstance());
        param[2].setType(20L);
        param[2].setValue("5553");
        identification.setIdentification(param);
        bean.setIdentification(identification);

        ExecuteResultAdapter<AccountProfileQueryResults> adapter = new ExecuteResultAdapter<AccountProfileQueryResults>(AccountProfileQueryResults.class);
        ExecuteResult adaptResult = null;
        try
        {
            adaptResult = (ExecuteResult) adapter.adapt(ctx, bean);
            
            if (adaptResult==null)
            {
                fail("Adapt result is null");
            }
            
            GenericParameterParser parser = new GenericParameterParser(adaptResult.getParameters());
            assertEquals("Adapt Bank Address 1 wrong", bankInfo.getAddress1(), parser.getParameter("Bank.Address1", String.class));
            assertEquals("Adapt Bank Address 2 wrong", bankInfo.getAddress2(), parser.getParameter("Bank.Address2", String.class));
            assertEquals("Adapt Bank ID wrong", bankInfo.getBankID(), parser.getParameter("Bank.BankID", String.class));
            assertEquals("Adapt Suspension Reason wrong", accountDetail.getSuspensionReason(), parser.getParameter("Detail.SuspensionReason", String.class));
            assertEquals("Adapt Bill Cycle wrong", (Long) billing.getBillCycle(), parser.getParameter("Billing.BillCycle", Long.class));
            assertEquals("Adapt Account State wrong", (Long) profile.getState().getValue(), parser.getParameter("Profile.State.Value", Long.class));
            assertEquals("Adapt Identification Entry 0 Expiry", param[0].getExpiry().getTimeInMillis(), parser.getParameter("Identification.Identification.0.Expiry", Calendar.class).getTimeInMillis());
            assertEquals("Adapt Identification Entry 1 Expiry", param[1].getExpiry().getTimeInMillis(), parser.getParameter("Identification.Identification.1.Expiry", Calendar.class).getTimeInMillis());
            assertEquals("Adapt Identification Entry 2 Expiry", param[2].getExpiry().getTimeInMillis(), parser.getParameter("Identification.Identification.2.Expiry", Calendar.class).getTimeInMillis());
            assertEquals("Adapt Identification Entry 0 Type", param[0].getType(), parser.getParameter("Identification.Identification.0.Type", Long.class));
            assertEquals("Adapt Identification Entry 1 Type", param[1].getType(), parser.getParameter("Identification.Identification.1.Type", Long.class));
            assertEquals("Adapt Identification Entry 2 Type", param[2].getType(), parser.getParameter("Identification.Identification.2.Type", Long.class));
            assertEquals("Adapt Identification Entry 0 Value", param[0].getValue(), parser.getParameter("Identification.Identification.0.Value", String.class));
            assertEquals("Adapt Identification Entry 1 Value", param[1].getValue(), parser.getParameter("Identification.Identification.1.Value", String.class));
            assertEquals("Adapt Identification Entry 2 Value", param[2].getValue(), parser.getParameter("Identification.Identification.2.Value", String.class));
        }
        catch (Throwable e)
        {
            fail("Exception occurred: " + e.getMessage());
        }
        
        try
        {
            AccountProfileQueryResults unadaptResult = (AccountProfileQueryResults) adapter.unAdapt(ctx, adaptResult);
            if (unadaptResult==null)
            {
                fail("Unadapt result is null");
            }

            assertEquals("Bank Address 1 wrong", bankInfo.getAddress1(), unadaptResult.getBank().getAddress1());
            assertEquals("Bank Address 2 wrong", bankInfo.getAddress2(), unadaptResult.getBank().getAddress2());
            assertEquals("Bank ID wrong", bankInfo.getBankID(), unadaptResult.getBank().getBankID());
            assertEquals("Suspension Reason wrong", accountDetail.getSuspensionReason(), unadaptResult.getDetail().getSuspensionReason());
            assertEquals("Bill Cycle wrong", (Long) billing.getBillCycle(), (Long) unadaptResult.getBilling().getBillCycle());
            assertEquals("Account State wrong", (Long) profile.getState().getValue(), (Long) unadaptResult.getProfile().getState().getValue());
            assertEquals("Identification Entry 0 Expiry", param[0].getExpiry().getTimeInMillis(), unadaptResult.getIdentification().getIdentification()[0].getExpiry().getTimeInMillis());
            assertEquals("Identification Entry 1 Expiry", param[1].getExpiry().getTimeInMillis(), unadaptResult.getIdentification().getIdentification()[1].getExpiry().getTimeInMillis());
            assertEquals("Identification Entry 2 Expiry", param[2].getExpiry().getTimeInMillis(), unadaptResult.getIdentification().getIdentification()[2].getExpiry().getTimeInMillis());
            assertEquals("Identification Entry 0 Type", param[0].getType(), unadaptResult.getIdentification().getIdentification()[0].getType());
            assertEquals("Identification Entry 1 Type", param[1].getType(), unadaptResult.getIdentification().getIdentification()[1].getType());
            assertEquals("Identification Entry 2 Type", param[2].getType(), unadaptResult.getIdentification().getIdentification()[2].getType());
            assertEquals("Identification Entry 0 Value", param[0].getValue(), unadaptResult.getIdentification().getIdentification()[0].getValue());
            assertEquals("Identification Entry 1 Value", param[1].getValue(), unadaptResult.getIdentification().getIdentification()[1].getValue());
            assertEquals("Identification Entry 2 Value", param[2].getValue(), unadaptResult.getIdentification().getIdentification()[2].getValue());
        }
        catch (Throwable e)
        {
            fail("Exception occurred: " + e.getMessage());
        }
        
        
    }

}
