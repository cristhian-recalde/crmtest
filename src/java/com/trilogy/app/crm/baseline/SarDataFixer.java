package com.trilogy.app.crm.baseline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class SarDataFixer {
	
	static void processDisk(String id, String nodeName, String date, String orignalFileName, String outFileName)
	   throws Exception 
	   {


		   BufferedReader in = new BufferedReader(new FileReader(orignalFileName));
		   PrintWriter out = new PrintWriter( new FileWriter( outFileName));

		   String text=null; 
		   
		   for(int i =0; i< 5; ++i)
			  text = in.readLine(); 

		   while ( text != null) 
		   {

			   if ( text.trim().length() > 0 && !text.trim().startsWith("Average") )
			   {
				   StringTokenizer tokenizer = new StringTokenizer(text, " ");

			   int i = 0; 
			   out.print(id + "," + nodeName + ","); 

			   while (tokenizer.hasMoreTokens())
			   {
			     String s = tokenizer.nextToken().trim();

			     String time = ""; 
			     if (i==0 )
			     {
			    	 if (s.indexOf(":") != -1)
			    	 {
			    		 time = s; 
			    		 out.print(date + time + ","); 
			    	 }else 
			    		 out.print(date + time + "," + s + ","); 
			     }
			     else
			    	 out.print(s.replaceAll(",", ".") + ","); 


			     ++i;  
			   }


			   out.print("\n"); 

			 }
			 text = in.readLine(); 

			}

			   in.close(); 
			   out.flush(); 
			   out.close(); 
	   }
		
		

	static void processCPU(String id, String nodeName, String date, String orignalFileName, String outFileName)
	   throws Exception 
	   {


		   BufferedReader in = new BufferedReader(new FileReader(orignalFileName));
		   PrintWriter out = new PrintWriter( new FileWriter( outFileName));

		   String text=null; 
		   
		   for(int i =0; i< 4; ++i)
			   text = in.readLine(); 

		   while ( text != null) 
		   {

			   if ( text.trim().length() > 0 && !text.trim().startsWith("Average") )
			   {
				   StringTokenizer tokenizer = new StringTokenizer(text, " ");
				   int i = 0; 
				   out.print(id + ","); 

				   
				   while (tokenizer.hasMoreTokens())
				   {
					   String s = tokenizer.nextToken().trim();
					   if (i == 0)
						   out.print(date + s + ","); 
					   else
						   out.print(s + ","); 


					   ++i;  
				   }


				   out.print("\n"); 

			   	}
			   
			   text = in.readLine(); 

			  }

			  in.close(); 
			  out.flush(); 
			  out.close(); 
	   }
		
	
		static void Main(String[] args)
		{
			if (args.length != 6 )
			System.out.println("Usage: java SarDataFixer  [cpu|disk] [baseline_id] [node_name] [date] [original file name] [output file Name]"); 	
			
			try {
				if (args[0].trim().equals("cpu"))
					processCPU(args[1], args[2], args[3], args[4], args[5]);
				else if (args[0].trim().equals("disk"))
					processDisk(args[1], args[2], args[3], args[4], args[5]);
				else 
					System.out.println("Usage: java SarDataFixer  [cpu|disk] [baseline_id] [node_name] [date] [original file name] [output file Name]"); 	
					
			} catch (Exception e )
			{
				e.printStackTrace(); 
			}
			
		}

}
