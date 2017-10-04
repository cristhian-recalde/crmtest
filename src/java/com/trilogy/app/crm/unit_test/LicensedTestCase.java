package com.trilogy.app.crm.unit_test;

import com.trilogy.framework.license.DefaultLicenseMgr;
import com.trilogy.framework.license.License;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

public class LicensedTestCase extends 
ContextAwareTestCase
{
	String[] license_keys;
	public LicensedTestCase(String name, String[] keys) {
		super(name); 
		license_keys = keys;
	}
	
    // INHERIT
    @Override
    protected void setUp()
    {
    	super.setUp(); 
    	
    	if ( license_keys != null && license_keys.length > 0 ){
    	    Home home = new TransientFieldResettingHome(getContext(), new com.redknee.framework.license.LicenseTransientHome(getContext()));
    		for ( int i =0; i < license_keys.length; ++i){
    			createLicense(home, license_keys[i]); 
    		}	
    		getContext().put(com.redknee.framework.license.LicenseHome.class, home);
    		getContext().put(LicenseMgr.class,
                new DefaultLicenseMgr(getContext()));
    	}	
    }
	
    public void createLicense(Home home, String key){
        License myLicense = new License();
        myLicense.setName(key);
        myLicense.setKey(key);
        myLicense.setEnabled(true);
        try
        {
            home.create(getContext(), myLicense);
        }
        catch (Exception e)
        {
            fail("Unexpected exception installing license into license manager " + e.getMessage());
        }
    }

	public String[] getLicensKeys() {
		return license_keys;
	}

	public void setLicenseKeys(String[] license_keys) {
		this.license_keys = license_keys;
	}
    
    
}
