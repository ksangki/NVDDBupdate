/**
 * @file	DBUploader.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	This file contains a class that have methods to work on DB
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 */
package nvddbupdater;

import java.io.File;
import java.sql.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * @class	DBUploader
 * @brief	A class can interact with NVD DB
 * @warning	Require 'mysql-connector-java-8.0.12,jar' file on classpath
 */
public class DBUploader {
	static ZipTagXml ztx = new ZipTagXml();
	Connection conn = null;
	/**
	 * @brief	This method make query that drop table of 'nvd' database and make a new table
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be '<year>_base.xml'
	 * 			The table name will be created on 'nvd' database is <year>_base
	 */
	public void init_and_upload_base(String fpath) throws Exception {
		if(fpath.isEmpty()) {
			System.out.println(" File_base path is empty.");
		}
		else {
			File fpth = new File(fpath);
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			if (!fpth.exists()) {
				System.out.println(" "+fpath + " does not exist.");
				System.out.println(" If there is " + parser2 + "_base table already, use it.");
				System.out.println(" If not, " + parser2 + "_base table is not created.");
			}
			else if (!fpth.isFile()) {
				System.out.println(" "+fpath + " is not a file.");
				System.out.println(" If there is " + parser2 + "_base table already, use it.");
				System.out.println(" If not, " + parser2 + "_base table is not created.");
			}
			else {
				try {
					setForeignKey(this.conn, 0);
					drop_table(this.conn, parser2+"_base");
					create_base(this.conn, parser2+"_base");
					load_xml_to_table(this.conn, parser2+"_base");
					setForeignKey(this.conn, 1);
					System.out.println(" DROP and CREATE base "+parser2);
				}
				catch (Exception e) {
					System.out.println(" DROP and CREATE base failed "+parser2);
					throw e;
				}
			}
		}
	}
	
	/**
	 * @brief	This method make query that drop table of 'nvd' database and make a new table
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be '<year>_refs.xml'
	 * 			The table name will be created on 'nvd' database is <year>_refs
	 */
	public void init_and_upload_refs(String fpath) throws Exception {
		
		if (fpath.isEmpty()) {
			System.out.println(" File_refs path is empty.");
		}
		else {
			File fpth = new File(fpath);
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			if (!fpth.exists()) {
				System.out.println(" "+fpath + " does not exist.");
				System.out.println(" If there is " + parser2 + "_refs table already, use it.");
				System.out.println(" If not, " + parser2 + "_refs table is not created.");
			}
			else if (!fpth.isFile()) {
				System.out.println(" "+fpath + " is not a file.");
				System.out.println(" If there is " + parser2 + "_refs table already, use it.");
				System.out.println(" If not, " + parser2 + "_refs table is not created.");
			}
			else {
				try {
					drop_table(this.conn, parser2+"_refs");	
					create_refs(this.conn, parser2+"_refs", parser2+"_base");
					load_xml_to_table(this.conn, parser2+"_refs");
					System.out.println(" DROP and CREATE refs "+parser2);
				}
				catch (Exception e) {
					System.out.println(" DROP and CREATE refs failed "+parser2);
					throw e;
				} 
			}
		}
		 
		
	}
	

	/**
	 * @brief	This method make query that drop table of 'nvd' database and make a new table
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be '<year>_vuln.xml'
	 * 			The table name will be created on 'nvd' database is <year>_vuln
	 */
	public void init_and_upload_vuln(String fpath) throws Exception {
		if (fpath.isEmpty()) {
			System.out.println(" File_vuln path is empty.");
		}
		else {
			File fpth = new File(fpath);
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			
			if (!fpth.exists()) {
				System.out.println(" "+fpath + " does not exist.");
				System.out.println(" If there is " + parser2 + "_vuln table already, use it.");
				System.out.println(" If not, " + parser2 + "_vuln table is not created.");
			}
			else if (!fpth.isFile()) {
				System.out.println(" "+fpath + " is not a file.");
				System.out.println(" If there is " + parser2 + "_vuln table already, use it.");
				System.out.println(" If not, " + parser2 + "_vuln table is not created.");
			}
			else {
				try {
					drop_table(this.conn, parser2+"_vuln");	
					create_vuln(this.conn, parser2+"_vuln", parser2+"_base");
					load_xml_to_table(this.conn, parser2+"_vuln");
					System.out.println(" DROP and CREATE vuln "+parser2);
				}
				catch (Exception e) {
					System.out.println(" DROP and CREATE vuln failed "+parser2);
					throw e;
				}  
			}
		}
		
		 
		
	}
	

	/**
	 * @brief	This method make query that update <year>_base tables of 'nvd' database and make a new table named modified_base
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be 'modified_base.xml'
	 * 			The table name will be created on 'nvd' database is modified_base
	 * @param	IsNewYear
	 * 			type: boolean
	 * 			IsNewYear notices whether the last update is run on last year or not
	 * 			If the last update is on last year, IsNewYear is true and create <newyear>_base table ex) 2019_base
	 * 			If not, IsNewYear is false and just update tables and create modified_base table
	 * @param	newyear
	 * 			newyear is the year when this method is called
	 * 			If IsNewYear is true, this method create new table referenced by newyear
	 * @throws	Exception
	 */
	public void upload_base_modified (String fpath, boolean IsNewYear, int newyear) throws Exception {
		System.out.println(" ");
		if(fpath.isEmpty()) {
			System.out.println(" File_base_modified path is empty.");
		}
		else {
			File inputFile = new File(fpath);
			if (!inputFile.exists()) {
				System.out.println(" "+fpath + " does not exist.");
				System.out.println(" Update base failed.");
			}
			else if (!inputFile.isFile()) {
				System.out.println(" "+fpath + " is not a file.");
				System.out.println(" Update base failed.");
			}
			else {
				
				try {
					// Load translated modified file.
					
					// make node list.
					
					if(IsNewYear) {
						create_base(this.conn, newyear+"_base");
					}
					else {
						///nothing to do
					}
					setForeignKey(this.conn, 0);
					drop_table(this.conn, "modified_base");
					create_base(this.conn, "modified_base");
					load_xml_to_table(this.conn, "modified_base");
					setForeignKey(this.conn, 1);
					NodeList nList = make_nodelist(inputFile);
					int procrate = 5;
					for (int temp = 0; temp < nList.getLength(); temp++) {
						// parse year of CVE
						Element cve_name = (Element) nList.item(temp);
						String cve_n = cve_name.getAttribute("name");
						String cve_s = cve_name.getAttribute("seq");
						String cve_t = cve_name.getAttribute("type");
						String cve_p = cve_name.getAttribute("published");
						String cve_m = cve_name.getAttribute("modified");
						String cve_cv = cve_name.getAttribute("CVSS_vector");
						String cve_ce = cve_name.getAttribute("CVSS_exploit_subscore");
						String cve_ci = cve_name.getAttribute("CVSS_impact_subscore");
						String cve_cb = cve_name.getAttribute("CVSS_base_score");
						String cve_cvs = cve_name.getAttribute("CVSS_version");
						String cve_sv = cve_name.getAttribute("severity");
						String cve_d = cve_name.getAttribute("desc");
						String cve_ddd = cve_d.replace("\\", "\\"+"\\");
						String cve_dd = cve_ddd.replace("'", "\\'");
						String parser1 = cve_n.split("-")[1];
						StringBuffer query1 = new StringBuffer();
						StringBuffer query2 = new StringBuffer();
						StringBuffer query3 = new StringBuffer();
						
						int parsint = Integer.parseInt(parser1);
						if (parsint < 2002) {
							parser1 = "2002";
						}
						else {
							//nothing to do
						}
						if (cve_n.isEmpty()) {
							System.out.println(" There is no name of CVE (base_modified)\n" + fpath);
						}
						else if (cve_s.isEmpty()) {
							System.out.println(" There is no seq of CVE (base_modified)\n" + fpath + ", " + cve_n);
						}
						else if (cve_t.isEmpty()) {
							System.out.println(" There is no type of CVE (base_modified)\n" + fpath + ", " + cve_n);
						}
						else if (cve_p.isEmpty()) {
							System.out.println(" There is no published of CVE (base_modified)\n" + fpath + ", " + cve_n);
						}
						else if (cve_m.isEmpty()) {
							System.out.println(" There is no modified of CVE (base_modified)\n" + fpath + ", " + cve_n);
						}
						else if (cve_dd.isEmpty()) {
							System.out.println(" There is no `desc` of CVE (base_modified)\n" + fpath + ", " + cve_n);
						}
						else {
							query1.append("INSERT INTO "+parser1+"_base " +
									"(name, seq, type, published, modified, " 
									);
							query2.append("VALUES ('"+cve_n+"','"+cve_s+"','"+cve_t+"','"+cve_p+"','"+cve_m+"',");
							query3.append("ON DUPLICATE KEY UPDATE modified= '"+cve_m+"', ");
							
								
							
							if (!cve_cv.isEmpty()) {
								query1.append("CVSS_vector, ");
								query2.append("'"+cve_cv+"', ");
								query3.append("CVSS_vector ='"+cve_cv+"', ");
								
							}
							else {
								//nothing to do
							}
							if (!cve_ce.isEmpty()) {
								query1.append("CVSS_exploit_subscore, ");
								query2.append(""+cve_ce+", ");
								query3.append("CVSS_exploit_subscore ="+cve_ce+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cve_ci.isEmpty()) {
								query1.append("CVSS_impact_subscore, ");
								query2.append(""+cve_ci+", ");
								query3.append("CVSS_impact_subscore ="+cve_ci+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cve_cb.isEmpty()) {
								query1.append("CVSS_base_score, ");
								query2.append(""+cve_cb+", ");
								query3.append("CVSS_base_score ="+cve_cb+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cve_cvs.isEmpty()) {
								query1.append("CVSS_version, ");
								query2.append(""+cve_cvs+", ");
								query3.append("CVSS_version ="+cve_cvs+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cve_sv.isEmpty()) {
								query1.append("severity, ");
								query2.append("'"+cve_sv+"', ");
								query3.append("severity ='"+cve_sv+"', ");
								
							}
							else {
								//nothing to do
							}
							query1.append("`desc`) ");
							query2.append("'"+cve_dd+"') ");
							query3.append("`desc` ='"+cve_dd+"';");
							
							
							
							query2.append(query3.toString());
							query1.append(query2.toString());
							
							
							this.conn.createStatement().executeUpdate(query1.toString());
							
							
						}
						if(temp*100/nList.getLength() > procrate) {
							System.out.println(" upload modified base "+procrate+"%");
							procrate = procrate + 5;
						}
					}
					
					System.out.println(" upload modified base complete");
				} catch (Exception e) {
					System.out.println(" upload modified base failed");
					throw e;
				}  finally {
					///nothing to do
				}
				
			}
		}
		 
		
	}
	
	

	/**
	 * @brief	This method make query that update <year>_refs tables of 'nvd' database and make a new table named modified_refs
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be 'modified_refs.xml'
	 * 			The table name will be created on 'nvd' database is modified_refs
	 * @param	IsNewYear
	 * 			type: boolean
	 * 			IsNewYear notices whether the last update is run on last year or not
	 * 			If the last update is on last year, IsNewYear is true and create <newyear>_refs table ex) 2019_refs
	 * 			If not, IsNewYear is false and just update tables and create modified_refs table
	 * @param	newyear
	 * 			newyear is the year when this method is called
	 * 			If IsNewYear is true, this method create new table referenced by newyear
	 * @throws	Exception
	 */
	public void upload_refs_modified (String fpath, boolean IsNewYear, int newyear) throws Exception {
		System.out.println(" ");
		if(fpath.isEmpty()) {
			System.out.println(" File_refs_modified path is empty.");
		}
		else {
			File inputFile = new File(fpath);
			if (!inputFile.exists()) {
				System.out.println(" "+fpath + " does not exist.");
				System.out.println(" Update refs failed.");
			}
			else if (!inputFile.isFile()) {
				System.out.println(" "+fpath + " is not a file.");
				System.out.println(" Update refs failed.");
			}
			else {
				
				try {
					// Load translated modified file.
					
					// make node list.
					NodeList nList = make_nodelist(inputFile);
					if(IsNewYear) {
						create_refs(this.conn, newyear+"_refs",newyear+"_base");
					}
					else {
						//nothing to do
					}
					drop_table(this.conn, "modified_refs");
					create_refs(this.conn, "modified_refs", "modified_base");
					load_xml_to_table(this.conn, "modified_refs");
					String prev_name = "";
					int procrate = 5;
					for (int temp = 0; temp < nList.getLength(); temp++) {
						// parse year of CVE
						Element cve_name = (Element) nList.item(temp);
						String cve_n = cve_name.getAttribute("name");
						String cve_s = cve_name.getAttribute("source");
						String cve_uu = cve_name.getAttribute("url");
						String cve_u = cve_uu.replace("'","\\'");
						if (cve_n.isEmpty()) {
							System.out.println(" There is no name of CVE (refs_modified)\n" + fpath);
						}
						else if (cve_s.isEmpty()) {
							System.out.println(" There is no source of CVE (refs_modified)\n" + fpath + ", " + cve_n);
						}
						else if (cve_u.isEmpty()) {
							System.out.println(" There is no url of CVE (refs_modified)\n" + fpath + ", " + cve_n);
						}
						else {
							String parser1 = cve_n.split("-")[1];
							int parsint = Integer.parseInt(parser1);
							if (parsint < 2002) {
								parser1 = "2002";
							}
							else {
								// nothing to do
							}
							if (!prev_name.equals(cve_n)) {
								try {
									String refsDeleteQuery = "DELETE FROM "+parser1+"_refs WHERE name= '"+cve_n+"';";
									this.conn.createStatement().executeUpdate(refsDeleteQuery);
								} catch (Exception e) {
									e.printStackTrace();
									throw e;
								}
								prev_name = cve_n;
							}
							else {
								// nothing to do
							}
							
							String refsInsertQuery = "INSERT INTO "+parser1+"_refs " +
									"(name, source, url) " +
									"VALUES ('"+cve_n+"','"+cve_s+"','"+cve_u+"'); ";
							conn.createStatement().executeUpdate(refsInsertQuery);
							
							
						}
						if(temp*100/nList.getLength() > procrate) {
							System.out.println(" upload modified refs "+procrate+"%");
							procrate = procrate + 5;
						}
						
					}
				
					System.out.println(" upload modified refs complete");
				} catch (Exception e) {
					System.out.println(" upload modified refs failed");
					throw e;
				}  finally {
					///nothing to do
				}
			}
			
		}
		
	}
	

	/**
	 * @brief	This method make query that update <year>_vuln tables of 'nvd' database and make a new table named modified_vuln
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be 'modified_vuln.xml'
	 * 			The table name will be created on 'nvd' database is modified_vuln
	 * @param	IsNewYear
	 * 			type: boolean
	 * 			IsNewYear notices whether the last update is run on last year or not
	 * 			If the last update is on last year, IsNewYear is true and create <newyear>_vuln table ex) 2019_vuln
	 * 			If not, IsNewYear is false and just update tables and create modified_vuln table
	 * @param	newyear
	 * 			newyear is the year when this method is called
	 * 			If IsNewYear is true, this method create new table referenced by newyear
	 * @throws	Exception
	 */
	public void upload_vuln_modified (String fpath, boolean IsNewYear, int newyear) throws Exception {
		System.out.println(" ");
		if(fpath.isEmpty()) {
			System.out.println(" File_vuln_modified path is empty.");
		}
		else {
			File inputFile = new File(fpath);
			if (!inputFile.exists()) {
				System.out.println(" "+fpath + " does not exist.");
				System.out.println(" Update vuln failed.");
			}
			else if (!inputFile.isFile()) {
				System.out.println(" "+fpath + " is not a file.");
				System.out.println(" Update vuln failed.");
			}
			else {
				
				try {
					// Load translated modified file.
					
					// make node list.
					NodeList nList = make_nodelist(inputFile);
					if(IsNewYear) {
						create_vuln(this.conn, newyear+"_vuln", newyear+"_base");
					}
					else {
						//nothing to do
					}
					drop_table(this.conn, "modified_vuln");
					create_vuln(this.conn, "modified_vuln", "modified_base");
					load_xml_to_table(this.conn, "modified_vuln");
					String prev_name = "";
					int procrate = 5;
					for (int temp = 0; temp < nList.getLength(); temp++) {
						// parse year of CVE
						Element cve_name = (Element) nList.item(temp);
						String cve_n = cve_name.getAttribute("name");
						String cve_p = cve_name.getAttribute("prodname");
						String cve_pp = cve_p.replace("'", "\\'");
						String cve_v = cve_name.getAttribute("vendor");
						String cve_num = cve_name.getAttribute("num");
						String cve_e = cve_name.getAttribute("edition");
						if (cve_n.isEmpty()) {
							System.out.println(" There is no name of CVE (vuln_modified)\n" + fpath);
						}
						else if (cve_pp.isEmpty()) {
							System.out.println(" There is no prodname of CVE (vuln_modified)\n" + fpath + ", " + cve_n);
						}
						else if (cve_v.isEmpty()) {
							System.out.println(" There is no vendor of CVE (vuln_modified)\n" + fpath + ", " + cve_n);
						}
						else {
							String parser1 = cve_n.split("-")[1];
							StringBuffer query = new StringBuffer();
							StringBuffer query2 = new StringBuffer();
							
							int parsint = Integer.parseInt(parser1);
							if (parsint < 2002) {
								parser1 = "2002";
							}
							else {
								// nothing to do
							}
							if (!prev_name.equals(cve_n)) {
								String vulnDeleteQuery = "DELETE FROM "+parser1+"_vuln WHERE name= '"+cve_n+"';";
								conn.createStatement().executeUpdate(vulnDeleteQuery);
								prev_name = cve_n;
							}
							else {
								// nothing to do
							}
							query.append("INSERT INTO "+parser1+"_vuln " +
									"(name, prodname, vendor");
							query2.append("VALUES ('"+cve_n+"','"+cve_pp+"','"+cve_v+"'");
							
							if (!cve_num.isEmpty()) {
								query.append(", num");
								query2.append(", '"+cve_num+"'");
								
							}
							if (!cve_e.isEmpty()) {
								query.append(", edition");
								query2.append(", '"+cve_e+"'");
								
							}
							else {
								//nothing to do
							}
							query.append(") ");
							query2.append(");");
							
							query.append(query2.toString());
							
							conn.createStatement().executeUpdate(query.toString());
							
						}
						if(temp*100/nList.getLength() > procrate) {
							System.out.println(" upload modified vuln "+procrate+"%");
							procrate = procrate + 5;
						}
					}
					
					
					
					System.out.println(" upload modified vuln complete");
				} catch (Exception e) {
					System.out.println(" upload modified vuln failed");
					throw e;
				}  finally {
					///nothing to do
				}
			}
		}
		 
	/**
	 * @brief	make testing table on nvd database
	 */
	}
	public void set_testing_table () throws Exception{
		
		try {
			setForeignKey(this.conn, 0);
			drop_table(this.conn, "2999_base");
			create_base(this.conn, "2999_base");
			drop_table(this.conn, "test_modified_base");
			create_base(this.conn, "test_modified_base");
			setForeignKey(this.conn, 1);
			
			drop_table(this.conn, "2999_refs");
			drop_table(this.conn, "2999_vuln");
			
			drop_table(this.conn, "test_modified_refs");
			drop_table(this.conn, "test_modified_vuln");
			
			create_refs(this.conn, "2999_refs","2999_base");
			create_vuln(this.conn, "2999_vuln","2999_base");
			
			create_refs(this.conn, "test_modified_refs", "test_modified_base");
			create_vuln(this.conn, "test_modified_vuln", "test_modified_base");
			String baseInsertQuery = "INSERT INTO 2999_base VALUES ('CVE-2999-9999', '2999-9999', 'CVE', '2018-10-29', '2018-10-29', '(AV:N/AC:L/Au:N/C:P/I:P/A:P)', 10, 10, 10, 2, 'High', 'Test Description (MySQL)')";
			String refsInsertQuery = "INSERT INTO 2999_refs VALUES ('CVE-2999-9999', 'TESTSRC', 'https://tde.sktelecom.com')";
			String vulnInsertQuery = "INSERT INTO 2999_vuln VALUES ('CVE-2999-9999', 'Testprod', 'Testvendor', 'Testnum', 'Testedition')";
			String mBaseInsertQuery = "INSERT INTO test_modified_base VALUES ('CVE-2999-9999', '2999-9999', 'CVE', '2018-10-29', '2018-10-29', '(AV:N/AC:L/Au:S/C:N/I:N/A:C)', 0, 0, 0, 3, 'Low', 'Modified Description (MySQL)')";
			String mRefsInsertQuery = "INSERT INTO test_modified_refs VALUES ('CVE-2999-9999', 'TESTSRC', 'https://tde.sktelecom.com')";
			String mRefsInsertQuery2 = "INSERT INTO test_modified_refs VALUES ('CVE-2999-9999', 'MODSRC', 'https://thub.sktelecom.com')";
			String mVulnInsertQuery = "INSERT INTO test_modified_vuln VALUES ('CVE-2999-9999', 'Testprod', 'Testvendor', 'Testnum', 'Testedition')";
			String mVulnInsertQuery2 = "INSERT INTO test_modified_vuln VALUES ('CVE-2999-9999', 'Modprod', 'Modvendor', 'Modnum', 'Modedition')";
			this.conn.createStatement().executeUpdate(baseInsertQuery);
			this.conn.createStatement().executeUpdate(refsInsertQuery);
			this.conn.createStatement().executeUpdate(vulnInsertQuery);
			this.conn.createStatement().executeUpdate(mBaseInsertQuery);
			this.conn.createStatement().executeUpdate(mRefsInsertQuery);
			this.conn.createStatement().executeUpdate(mRefsInsertQuery2);
			this.conn.createStatement().executeUpdate(mVulnInsertQuery);
			this.conn.createStatement().executeUpdate(mVulnInsertQuery2);
			
		}
		catch (Exception e) {
			
			System.out.println(" DROP and CREATE testing base table failed ");
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void create_base (Connection conn, String n_table) throws Exception{
		try {
			conn.createStatement().execute(
					"CREATE TABLE "+n_table+"(\n" +
				    "name char(20) not null unique,\n" +
				    "seq text,\n" +
				    "type text,\n" +
				    "published date not null,\n" +
				    "modified date not null,\n" +
				    "CVSS_vector text,\n" +
				    "CVSS_exploit_subscore double,\n" +
				    "CVSS_impact_subscore double,\n" +
				    "CVSS_base_score double,\n" +
				    "CVSS_version double,\n" +
				    "severity text,\n" +
				    "`desc` mediumtext\n" +
				    
				    ") "
					);
		} catch (Exception e) {
			System.out.println(" createBase failed "+n_table);
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void create_refs(Connection conn, String n_table, String base_table) throws Exception {
		String createQuery = "CREATE TABLE "+n_table+"(\n" 
				+ "name char(20) not null,\n"
				+ "source text, \n"
				+ "url text, \n"
				
				+ "foreign key(name) references "
				+ base_table + "(name)\n"
				+ "	on delete cascade)";
		try {
			conn.createStatement().execute(createQuery);
		} catch (Exception e) {
			System.out.println(" createRefs failed "+n_table);
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void create_vuln (Connection conn, String n_table, String base_table) throws Exception{
		String createQuery = "CREATE TABLE " + n_table +"(\n" + "name char(20) not null,\n" + "prodname text, \n"
				+ "vendor text, \n" + "num text, \n" + "edition text, \n" 
				+ "foreign key(name) references " + base_table + "(name)\n" + "	on delete cascade)";
		try {
			conn.createStatement().execute(createQuery);
		} catch (Exception e) {
			System.out.println(" createVuln failed "+n_table);
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void load_xml_to_table (Connection conn, String n_table) throws Exception{
		String loadQuery = "LOAD XML LOCAL INFILE './"+ZipTagXml.translated+"/nvdcve-"+n_table+".xml'\n" +
				"INTO TABLE "+n_table+"\n" +
				"ROWS IDENTIFIED BY '<entry>';";
		try {
			conn.createStatement().execute(loadQuery);
		} catch (Exception e) {
			System.out.println(" loadXmlToTable failed "+n_table);
			throw e;
		} finally {
			///nothing to do
		}
	} 
	
	public void drop_table (Connection conn, String n_table) throws Exception {
		String dropQuery = "drop table if exists "+n_table;
		try {
			conn.createStatement().execute(dropQuery);
		} catch (Exception e) {
			System.out.println(" dropTable failed "+n_table);
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void connect_to_DB() throws Exception{
		try {
			if (this.conn == null) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				this.conn = DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/nvd?serverTimezone=UTC", ZipTagXml.db_id, ZipTagXml.db_pw);
			} else {
				if (this.conn.isClosed()) {
					Class.forName("com.mysql.cj.jdbc.Driver");
					this.conn = DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/nvd?serverTimezone=UTC", ZipTagXml.db_id, ZipTagXml.db_pw);
				} else {
					System.out.println(" Connection already exists");
				}
			}
		} catch (Exception e) {
			System.out.println(" connectToDB failed");
			throw e;
		} 
	}
	
	public void disconnect_DB() throws Exception {
		try {
			if(!this.conn.isClosed() && this.conn != null) {
				this.conn.close();
			}
		} catch (Exception e) {
			System.out.println(" disconnectDB failed");
			throw e;
		}
	}
	
	public NodeList make_nodelist(File inputFile) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("entry");
			return nList;
		} catch (Exception e) {
			System.out.println(" makeNodeList failed");
			throw e;
		}

	}
	
	public void setForeignKey(Connection conn, int key) throws Exception{
		String setKeyQuery = "set foreign_key_checks = "+key;
		try {
			conn.createStatement().execute(setKeyQuery);
		} catch (Exception e) {
			throw e;
		} finally {
			///nothing to do
		}
	}
}