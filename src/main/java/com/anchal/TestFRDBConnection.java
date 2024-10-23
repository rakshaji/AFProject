package com.anchal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public class TestFRDBConnection {
	
	public static void main(String args[]) {
		
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("Enter server name (PRINCE1\\\\RANCELAB2008 is default):");
			String serveNameStr = scanner.nextLine();

			System.out.print("Enter port number(blank is default):");
			String port = scanner.nextLine();

			System.out.print("Enter database name (master is default): ");
			String dbname = scanner.nextLine();

			System.out.print("Do you trust the certificate (Y/N is default): ");
			String isTrustedStr = scanner.nextLine();
			
			SQLServerDataSource ds = new SQLServerDataSource();  
			// set username and password
			ds.setUser("sa");  
			ds.setPassword("");  
			// server name
			if("".equals(serveNameStr)) {
				ds.setServerName("PRINCE1\\RANCELAB2008");//
			} else {
				ds.setServerName(serveNameStr);				
			}
			// port number
			if(port != null && !"".equals(port)) {
				ds.setPortNumber(Integer.parseInt(port));	
			} 
			// database name
			if(dbname != null && !"".equals(dbname)) {
				ds.setDatabaseName(dbname);	
			} else {
				ds.setDatabaseName("master");	
			}
			// is trusted server certificate
			boolean isTrusted = "Y".equalsIgnoreCase(isTrustedStr)? true : false;
			if(isTrusted) {
				ds.setTrustServerCertificate(true);
			} else {
				ds.setTrustServerCertificate(false);
			}
			ds.setIntegratedSecurity(true);
			ds.setSSLProtocol("TLSv1.2");
			
			Connection con = ds.getConnection();
			Statement stm = con.createStatement();
			boolean successs = stm.execute("select count(*) from ProjectChildMaster");
			if(successs) {
				System.out.print("Query ran successfully!");
				
				ResultSet resultSet = stm.executeQuery("select ProductChildID as barcode, Field2 as tinyurl from ProductChildMaster where Field2 != ''");
				resultSet.first();
				System.out.println(resultSet.getString(0));
				System.out.println(resultSet.getString(1));
			} else {
				System.out.print("no record found");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}  

	}

}
