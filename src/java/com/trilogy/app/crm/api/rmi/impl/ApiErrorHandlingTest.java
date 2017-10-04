package com.trilogy.app.crm.api.rmi.impl;

import java.security.Permission;
import java.security.Principal;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.api.rmi.impl.MobileNumbersImplTest.FakeAuthSPI;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleTransientHome;
import com.trilogy.app.crm.support.GracefulShutdownSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CRMException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.CompoundException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.DataStoreException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ShutdownException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationException;
import com.trilogy.util.crmapi.wsdl.v2_0.exception.ValidationExceptionEntry;
import com.trilogy.util.crmapi.wsdl.v2_1.exception.ExceptionCode;
import com.trilogy.util.crmapi.wsdl.v3_0.api.AccountServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;

public class ApiErrorHandlingTest extends ContextAwareTestCase 
{
	/**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public ApiErrorHandlingTest(final String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(ApiErrorHandlingTest.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
    	super.setUp();
    	setup(getContext());
    }
    
    public static void setup(Context context)
    {
    	try
    	{
    		impl_ = new AccountsImpl(context);
    	}
    	catch(Exception e)
    	{
    		fail("Failed Impl setup. " + e.getMessage());
    	}
    	
    	// Install Test Authenticator
    	context.put(AuthSPI.class, new FakeAuthSPI());
    	context.put(Principal.class, new User());
    	
    	// Install Account pipeline
    	setupAccountData(context);
    	
    	// Install Bill Cycle pipeline
    	setupBillCycleData(context);
    }
    
    private static void setupAccountData(Context context)
    {
    	Home home = new AccountTransientHome(context);
    	context.put(AccountHome.class, home);
    	
    	try
    	{
            //Setup test Account 
            Account account = (Account) XBeans.instantiate(Account.class, context);
            account.setBAN(BAN);
    		home.create(account);
    	}
    	catch (Exception e)
    	{
    		fail("Fail Account Setup. " + e.getMessage());
    	}
    }
    
    private static void setupBillCycleData(Context context)
    {
    	Home home = new BillCycleTransientHome(context);
    	context.put(BillCycleHome.class, home);
    	
    	try
    	{
            // Setup test Bill Cycle
            BillCycle bc = (BillCycle) XBeans.instantiate(BillCycle.class, context);
            bc.setIdentifier(BILL_CYCLE);
    		home.create(bc);
    	}
    	catch(Exception e)
    	{
    		fail("Failed Bill Cycle Setup. " + e.getMessage());
    	}
    }
    
    @Override
    public void tearDown()
    {
    	super.tearDown();
    }
    
    private CRMRequestHeader createRequestHeader()
    {
    	CRMRequestHeader header = new CRMRequestHeader ();
    	header.setUsername("rkadm");
    	header.setPassword("rkadm");
    	header.setTransactionID("1234567890");
    	return header;
    }
    
    /**
     * Errors with Mandatory fields results in CRMException. This error handling is acceptable.
     */
    public void testValidationExceptionHandling()
    {
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), "", BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
    	{
    	    CRMException exception = e.getFaultMessage().getCRMException();
    	    System.out.println(e.getMessage());
    	    assertTrue(exception.getCode() == com.redknee.util.crmapi.wsdl.v2_1.exception.ExceptionCode.INVALID_IDENTIFICATION);
    	    System.out.println(exception.getMessage());
    	}
    }
    
    /**
     * Errors encountered prior to Home.store()/create()/remove() operations result in CRMException.  
     * This error handling is acceptable.
     */
    public void testCRMExceptionHandling()
    {
    	try
    	{
    		Home home = (Home) getContext().get(BillCycleHome.class);
    		// Remove all default Bill Cycles
    		home.removeAll();
    	}
    	catch (HomeException e)
    	{
    		fail("Failed removing Bill Cycles. " + e.getMessage());
    	}
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
    	{
            CRMException exception = e.getFaultMessage().getCRMException();
            System.out.println(e.getMessage());
            assertTrue(exception.getCode() == com.redknee.util.crmapi.wsdl.v2_1.exception.ExceptionCode.INVALID_IDENTIFICATION);
            System.out.println(exception.getMessage());
		}
    }
    
    /**
     * Multiple Validation errors are accumulated in to one ValidationException with many entries.  
     * This error handling is acceptable.
     */
    public void testCompoundIllegalStateExceptionHandling()
    {
    	//Test pipeline that throws IllegalPropertyArgumentException
    	Home home = (Home) getContext().get(AccountHome.class);
    	CompoundValidator validator = new CompoundValidator();
    	validator.add(new ThrowCompoundIllegalStateExceptionValidator());
    	home = new ValidatingHome(validator, home);
    	//Install Fake pipeline
    	getContext().put(AccountHome.class, home);
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
    	{
            com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
			assertTrue(crmException instanceof ValidationException);
			{
				System.out.println(e.getMessage());
				final ValidationException exception = (ValidationException) crmException;
				final ValidationExceptionEntry[] entries = exception.getEntries();
				/* Though 3 errors were thrown in a CompoundIllegalStateException,
				 * only 2 errors were proper Validation Exceptions */ 
				assertTrue(entries.length == 2);
				for (int i = 0 ; i<entries.length; i++)
				{
					System.out.println(entries[i].getName());
					assertTrue(entries[i].getName().indexOf(" ") == -1);
					System.out.println(entries[i].getExplanation());
				}
			}
		}
    }
    
    /**
     * Multiple errors (not related to validation) are accumulated into a CompoundException with many entries.
     */
    public void testCompoundIllegalStateExceptionHandling2()
    {
    	//Test pipeline that throws IllegalPropertyArgumentException
    	Home home = (Home) getContext().get(AccountHome.class);
    	CompoundValidator validator = new CompoundValidator();
    	validator.add(new ThrowCompoundIllegalStateExceptionValidator2());
    	home = new ValidatingHome(validator, home);
    	//Install Fake pipeline
    	getContext().put(AccountHome.class, home);
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
        {
    	    com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
			assertTrue(crmException instanceof CompoundException);
			{
				System.out.println(e.getMessage());
				final CompoundException exception = (CompoundException) crmException;
				final com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException[] entries = exception.getCompoundEntries();
				assertTrue(entries.length == 2);
				for (int i = 0 ; i<entries.length; i++)
				{
					System.out.println(entries[i].getMessage());
				}
			}
		}
    }
    
    /**
     * IllegalStateExceptions thrown don't have a particular field that failed the method's action.
     * The Error is reported as CRMException (more specific to store is DataStoreException).
     * This error handling is acceptable.
     */
    public void testIllegalStateExceptionHandling()
    {
    	Home home = (Home) getContext().get(AccountHome.class);
    	CompoundValidator validator = new CompoundValidator();
    	validator.add(new ThrowIllegalStateExceptionValidator());
    	home = new ValidatingHome(validator, home);
    	//Install Fake pipeline
    	getContext().put(AccountHome.class, home);
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
        {
            com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
			assertTrue(crmException instanceof DataStoreException);
			{
				System.out.println(e.getMessage());
				final DataStoreException exception = (DataStoreException) crmException;
				assertNotNull(exception);
				System.out.println(exception.getMessage());
			}
		}
    }
    
    /**
     * HomeException results in DataStoreException. This error handling is acceptable.
     */
    public void testHomeExceptionHandling()
    {
    	Home home = (Home) getContext().get(AccountHome.class);
    	home = new ThrowHomeExceptionHome(home);
    	//Install Fake pipeline
    	getContext().put(AccountHome.class, home);
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
        {
            com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
            assertTrue(crmException instanceof DataStoreException);
			{
				System.out.println(e.getMessage());
				final DataStoreException exception = (DataStoreException) crmException;
				assertFalse(exception.getProfileStored());
				System.out.println(exception.getMessage());
			}
		}
    }
    
    /**
     * HomeInternalException results in DataStoreException. This error handling is acceptable
     */
    public void testHomeInternalExceptionHandling()
    {
    	Home home = (Home) getContext().get(AccountHome.class);
    	home = new ThrowHomeInternalExceptionHome(home);
    	//Install Fake pipeline
    	getContext().put(AccountHome.class, home);
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
        {
            com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
            assertTrue(crmException instanceof DataStoreException);
			{
				System.out.println(e.getMessage());
				final DataStoreException exception = (DataStoreException) crmException;
				assertFalse(exception.getProfileStored());
				System.out.println(exception.getMessage());
			}
		}
    }
    
    /**
     * UnsupportedOperationException results in DataStoreException. This error handling is acceptable
     */
    public void testUnsupportedOperationExceptionHandling()
    {
    	Home home = (Home) getContext().get(AccountHome.class);
    	home = new ThrowUnsupportedOperationExceptionHome(home);
    	//Install Fake pipeline
    	getContext().put(AccountHome.class, home);
    	
    	try
    	{
    		impl_.updateAccountBillCycle(createRequestHeader(), BAN, BILL_CYCLE, null);
    	}
    	catch (CRMExceptionFault e) 
        {
            com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
            assertTrue(crmException instanceof DataStoreException);
			{
				System.out.println(e.getMessage());
				final DataStoreException exception = (DataStoreException) crmException;
				assertFalse(exception.getProfileStored());
				System.out.println(exception.getMessage());
			}
		}
    }
    
    public void testShutdownExceptionHandling()
    {
    	try
    	{
    	    AccountServiceSkeletonInterface proxy = (AccountServiceSkeletonInterface) GracefulShutdownGenericProxy.newInstance(getContext(), impl_);

    		// Set flag for shutdown for API
    		GracefulShutdownSupport.setApiShutdown(getContext(), true);
    		try
    		{
    			proxy.listAccountTypes(createRequestHeader(), 1, true,null);
    		}
    		catch (CRMExceptionFault e) 
            {
                com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
                assertTrue(crmException instanceof ShutdownException);
    			System.out.println(e.getMessage());
    		}

    		// Set flag for shutdown for CRM
    		GracefulShutdownSupport.setApiShutdown(getContext(), false);
    		GracefulShutdownSupport.setCrmShutdown(getContext());
    		try
    		{
    			proxy.listAccountTypes(createRequestHeader(), 1, true,null);
    		}
    		catch (CRMExceptionFault e) 
            {
                com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
    			assertTrue(crmException instanceof ShutdownException);
    			System.out.println(e.getMessage());
    		}
    	}
    	catch (Exception e)
    	{
    		fail("Failed to install the Shutdown Proxy. " + e.getMessage());
    	}
    }
    
    public void testAuthorizationExceptionHandling()
    {
    	//Re-Install AuthSPI
    	getContext().put(AuthSPI.class, new DenyAccessAuthSPI());

    	try
    	{
    		impl_.listAccountTypes(createRequestHeader(), 1, true,null);
    	}
    	catch (CRMExceptionFault e) 
        {
            com.redknee.util.crmapi.wsdl.v2_0.exception.CRMException crmException = e.getFaultMessage().getCRMException();
    		assertTrue(crmException.getCode() == ExceptionCode.AUTHORIZATION_EXCEPTION);
    		System.out.println(e.getMessage());
    	}
    }
    
    /**
     * PRIVATE CLASSES FOR UNIT TESTING
     */
    
    
    /**
     * Throws CompoundIllegalStateException upon executing validate()
     * @author ali
     *
     */
    private class ThrowCompoundIllegalStateExceptionValidator implements Validator
    {
    	public void validate(Context context, Object obj)
			throws IllegalStateException 
		{
			CompoundIllegalStateException el = new CompoundIllegalStateException();
			// Sample error from AccountSystemTypeValidator class
			el.thrown(new IllegalPropertyArgumentException("Account.systemType", "Unsupported type."));
			// Sample error from AccountTypeValidator class
			el.thrown(new IllegalPropertyArgumentException(
                    AccountXInfo.TYPE, 
                    "No license exists for combination of Account Type/Billing Type: pooled/prepaid"));
			// Sample error from AccountSpidAwareValidator class
			el.thrown(new IllegalStateException("Cannot find dealer code home in context"));
			el.throwAll();
		}
    	
    }
    
    private class ThrowCompoundIllegalStateExceptionValidator2 implements Validator
    {
    	public void validate(Context context, Object obj)
			throws IllegalStateException 
		{
			CompoundIllegalStateException el = new CompoundIllegalStateException();
			// Sample error from AccountSpidAwareValidator class
			el.thrown(new IllegalStateException("Cannot find dealer code home in context"));
			el.thrown(new HomeException("Failed to update ECP."));
			el.throwAll();
		}
    	
    }
    
    /**
     * Throws IllegalStateException upon executing validate()
     * @author ali
     *
     */
    private class ThrowIllegalStateExceptionValidator implements Validator
    {
    	public void validate(Context context, Object obj)
    		throws IllegalStateException 
		{
    		throw new IllegalStateException("Cannot find dealer code home in context");
		}
    }
    
    private class ThrowHomeExceptionHome extends HomeProxy
    {
    	ThrowHomeExceptionHome(Home delegate)
    	{
    		super(delegate);
    	}

    	@Override
        public Object create(Context context, Object obj) throws HomeException
    	{
    		// Sample error from AccountProvisioningHome class
    		throw new HomeException("Provisioning Error 9999: Could not write profile to BAS-");
    	}
    	
    	@Override
        public Object store(Context context, Object obj) throws HomeException
    	{
    		// Sample error from AccountProvisioningHome class
    		throw new HomeException("Provisioning Error 9999: Could not write profile to BAS-");
    	}
    	
    	public Object remove() throws HomeException
    	{
    		// Sample error from AccountProvisioningHome class
    		throw new HomeException("Provisioning Error 9999: Could not write profile to BAS-");
    	}
    }
    
    private class ThrowHomeInternalExceptionHome extends HomeProxy
    {
    	ThrowHomeInternalExceptionHome(Home delegate)
    	{
    		super(delegate);
    	}

    	@Override
        public Object create(Context context, Object obj) throws HomeException
    	{
    		// Sample error from SubscriberVoicemailAuxiliaryServiceProvisionHome class
    		throw new HomeException("Auxiliary Service with ID couldn't be found");
    	}
    	
    	@Override
        public Object store(Context context, Object obj) throws HomeException
    	{
    		// Sample error from SubscriberVoicemailAuxiliaryServiceProvisionHome class
    		throw new HomeException("Auxiliary Service with ID couldn't be found");
    	}
    	
    	public Object remove() throws HomeException
    	{
    		// Sample error from SubscriberVoicemailAuxiliaryServiceProvisionHome class
    		throw new HomeException("Auxiliary Service with ID couldn't be found");
    	}
    }
    
    private class ThrowUnsupportedOperationExceptionHome extends HomeProxy
    {
    	ThrowUnsupportedOperationExceptionHome(Home delegate)
    	{
    		super(delegate);
    	}

    	@Override
        public Object create(Context context, Object obj) throws UnsupportedOperationException
    	{
    		// Sample error from SubscriberVoicemailAuxiliaryServiceProvisionHome class
    		throw new UnsupportedOperationException("Unsupported Operation");
    	}
    	
    	@Override
        public Object store(Context context, Object obj) throws UnsupportedOperationException
    	{
    		// Sample error from SubscriberVoicemailAuxiliaryServiceProvisionHome class
    		throw new UnsupportedOperationException("Unsupported Operation");
    	}
    	
    	public Object remove() throws UnsupportedOperationException
    	{
    		// Sample error from SubscriberVoicemailAuxiliaryServiceProvisionHome class
    		throw new UnsupportedOperationException("Unsupported Operation");
    	}
    }
    
    public static class DenyAccessAuthSPI implements AuthSPI
    {
        public void login(final Context ctx, final String username, final String password) throws LoginException
        {
        }

        public void logout(final Context ctx)
        {
        }

        // Always denies access
        public boolean checkPermission(final Context ctx, final Principal principal, final Permission permission)
        {
            return false;
        }

        public void updatePassword(final Context ctx, final Principal principal, final String oldPassword,
                final String newPassword) throws IllegalStateException
        {
        }

        public void validatePrincipal(final Context ctx, final Principal oldValue, final Principal newValue)
            throws IllegalStateException
        {
        }

        @Override
        public void release()
        {
            
        }
    }
    
    private static AccountServiceSkeletonInterface impl_ = null;
    private static final String BAN = "123000";
    private static final long BILL_CYCLE = 12L; 

}
