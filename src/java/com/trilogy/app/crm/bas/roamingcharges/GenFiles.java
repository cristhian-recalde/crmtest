package com.trilogy.app.crm.bas.roamingcharges;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GenFiles
{
	public static void main(String[] args) {
		char[] cArr = new char[443];
		for (int i=0; i<cArr.length; i++) {
			cArr[i]=' ';
		}
		cArr[442]='\n';
		
		//Call_date_time
//		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
//		String s = f.format(new Date());
		String s = "20020101112233";
		System.arraycopy(s.toCharArray(),0,cArr,0,s.length());
		
		//Call_type 15-16
		cArr[14] = '2';
		
		//duration 17-31 (sec)
		s = "120";
		System.arraycopy(s.toCharArray(),0,cArr,17,s.length());
		
		//rating_party_src_4 32-51
		s = "4168172088";
		System.arraycopy(s.toCharArray(),0,cArr,32,s.length());
		
		//rating_party_src_1 52-71
		s = "4168172088";
		System.arraycopy(s.toCharArray(),0,cArr,52,s.length());
		
		//rating_party_src_3 72-91
		s = "4168172088";
		System.arraycopy(s.toCharArray(),0,cArr,72,s.length());
		
		//rating_party_cgi 92-101		
		
		//rating_party_mscid 102-121
		s = "7878787878";
		System.arraycopy(s.toCharArray(),0,cArr,102,s.length());
		
		//other_party_num 122-141
		s = "9090909090";
		System.arraycopy(s.toCharArray(),0,cArr,122,s.length());
		
		//inc_trunk
		
		//out_trunk
		
		//internal_cause_loc
		
		//msrn
		
		//ton
		
		//npi
		
		//serv_code 217-226
		s = "123";
		System.arraycopy(s.toCharArray(),0,cArr,217,s.length());
		
		//supl_serv_code 227-236		
		
		//charged_amnt 237-251		
		s = "8888";
		System.arraycopy(s.toCharArray(),0,cArr,237,s.length());
		
		//charged_total_amt 252-266
		s = "9999";
		System.arraycopy(s.toCharArray(),0,cArr,252,s.length());
		
		//error 267-271
		
		//actual_duration 272-286
		s = "500";
		System.arraycopy(s.toCharArray(),0,cArr,272,s.length());
		
		//rating_party_loc 287-306
		s = "416";
		System.arraycopy(s.toCharArray(),0,cArr,287,s.length());
		
		//other_party_loc 307-326
		s = "905";
		System.arraycopy(s.toCharArray(),0,cArr,307,s.length());
		
		//other_party_num_loc 327-346
		s = "519";
		System.arraycopy(s.toCharArray(),0,cArr,327,s.length());
		
		//time_zone_ind 
		
		//serving_bid 352-361
		
		//home_bid 362-371
		
		//roaming_ind 372-372
		
		//read_party_lata
		
		//other_party_lata
		
		//curr_xchange_rate
		
		//time_offset
		
		//plmn_code 403-427
		s = "ABC123";
		System.arraycopy(s.toCharArray(),0,cArr,403,s.length());
		
		//tax_amount 428-442
		s = "2222";
		System.arraycopy(s.toCharArray(),0,cArr,428,s.length());
		
		String filename = args[0];
		Writer w=null;
		try {
			FileOutputStream fos = new FileOutputStream(filename+".roam");
			w = new BufferedWriter(new OutputStreamWriter(fos));
			w.write(cArr);
			w.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			try {
				w.close();
			}catch(Exception e) {}			
		}
	}
}
