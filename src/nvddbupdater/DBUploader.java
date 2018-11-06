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
	
	/**
	 * @brief	This method make query that drop table of 'nvd' database and make a new table
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be '<year>_base.xml'
	 * 			The table name will be created on 'nvd' database is <year>_base
	 */
	public void initNUploadBase(Connection conn, String fpath) throws Exception {
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
					setForeignKey(conn, 0);
					dropTable(conn, parser2+"_base");
					createBase(conn, parser2+"_base");
					loadXmltoTable(conn, parser2+"_base");
					setForeignKey(conn, 1);
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
	public void initNUploadRefs(Connection conn, String fpath) throws Exception {
		
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
					dropTable(conn, parser2+"_refs");	
					createRefs(conn, parser2+"_refs", parser2+"_base");
					loadXmltoTable(conn, parser2+"_refs");
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
	public void initNUploadVuln(Connection conn, String fpath) throws Exception {
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
					dropTable(conn, parser2+"_vuln");	
					createVuln(conn, parser2+"_vuln", parser2+"_base");
					loadXmltoTable(conn, parser2+"_vuln");
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
	public void uploadModifiedBase (Connection conn, String fpath, boolean IsNewYear, int newyear) throws Exception {
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
						createBase(conn, newyear+"_base");
					}
					else {
						///nothing to do
					}
					setForeignKey(conn, 0);
					dropTable(conn, "modified_base");
					createBase(conn, "modified_base");
					loadXmltoTable(conn, "modified_base");
					setForeignKey(conn, 1);
					NodeList nList = makeNodeList(inputFile);
					int procrate = 5;
					for (int temp = 0; temp < nList.getLength(); temp++) {
						// parse year of CVE
						Element cveName = (Element) nList.item(temp);
						String cveN = cveName.getAttribute("name");
						String cveSeq = cveName.getAttribute("seq");
						String cveType = cveName.getAttribute("type");
						String cvePub = cveName.getAttribute("published");
						String cveMod = cveName.getAttribute("modified");
						String cveCV = cveName.getAttribute("CVSS_vector");
						String cveEx = cveName.getAttribute("CVSS_exploit_subscore");
						String cveImp = cveName.getAttribute("CVSS_impact_subscore");
						String cveBase = cveName.getAttribute("CVSS_base_score");
						String cveVer = cveName.getAttribute("CVSS_version");
						String cveSev = cveName.getAttribute("severity");
						String cveTemp = cveName.getAttribute("desc");
						String cveTempT = cveTemp.replace("\\", "\\"+"\\");
						String cveDesc = cveTempT.replace("'", "\\'");
						String parser1 = cveN.split("-")[1];
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
						if (cveN.isEmpty()) {
							System.out.println(" There is no name of CVE (base_modified)\n" + fpath);
						}
						else if (cveSeq.isEmpty()) {
							System.out.println(" There is no seq of CVE (base_modified)\n" + fpath + ", " + cveN);
						}
						else if (cveType.isEmpty()) {
							System.out.println(" There is no type of CVE (base_modified)\n" + fpath + ", " + cveN);
						}
						else if (cvePub.isEmpty()) {
							System.out.println(" There is no published of CVE (base_modified)\n" + fpath + ", " + cveN);
						}
						else if (cveMod.isEmpty()) {
							System.out.println(" There is no modified of CVE (base_modified)\n" + fpath + ", " + cveN);
						}
						else if (cveDesc.isEmpty()) {
							System.out.println(" There is no `desc` of CVE (base_modified)\n" + fpath + ", " + cveN);
						}
						else {
							query1.append("INSERT INTO "+parser1+"_base " +
									"(name, seq, type, published, modified, " 
									);
							query2.append("VALUES ('"+cveN+"','"+cveSeq+"','"+cveType+"','"+cvePub+"','"+cveMod+"',");
							query3.append("ON DUPLICATE KEY UPDATE modified= '"+cveMod+"', ");
							
								
							
							if (!cveCV.isEmpty()) {
								query1.append("CVSS_vector, ");
								query2.append("'"+cveCV+"', ");
								query3.append("CVSS_vector ='"+cveCV+"', ");
								
							}
							else {
								//nothing to do
							}
							if (!cveEx.isEmpty()) {
								query1.append("CVSS_exploit_subscore, ");
								query2.append(""+cveEx+", ");
								query3.append("CVSS_exploit_subscore ="+cveEx+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cveImp.isEmpty()) {
								query1.append("CVSS_impact_subscore, ");
								query2.append(""+cveImp+", ");
								query3.append("CVSS_impact_subscore ="+cveImp+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cveBase.isEmpty()) {
								query1.append("CVSS_base_score, ");
								query2.append(""+cveBase+", ");
								query3.append("CVSS_base_score ="+cveBase+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cveVer.isEmpty()) {
								query1.append("CVSS_version, ");
								query2.append(""+cveVer+", ");
								query3.append("CVSS_version ="+cveVer+", ");
								
							}
							else {
								//nothing to do
							}
							if (!cveSev.isEmpty()) {
								query1.append("severity, ");
								query2.append("'"+cveSev+"', ");
								query3.append("severity ='"+cveSev+"', ");
								
							}
							else {
								//nothing to do
							}
							query1.append("`desc`) ");
							query2.append("'"+cveDesc+"') ");
							query3.append("`desc` ='"+cveDesc+"';");
							
							
							
							query2.append(query3.toString());
							query1.append(query2.toString());
							
							
							nvdQuery(conn,query1.toString(),null);
							
							
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
	public void uploadModifiedRefs (Connection conn, String fpath, boolean IsNewYear, int newyear) throws Exception {
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
					NodeList nList = makeNodeList(inputFile);
					if(IsNewYear) {
						createRefs(conn, newyear+"_refs",newyear+"_base");
					}
					else {
						//nothing to do
					}
					dropTable(conn, "modified_refs");
					createRefs(conn, "modified_refs", "modified_base");
					loadXmltoTable(conn, "modified_refs");
					String prevName = "";
					int procrate = 5;
					for (int temp = 0; temp < nList.getLength(); temp++) {
						// parse year of CVE
						Element cveEntry = (Element) nList.item(temp);
						String cveName = cveEntry.getAttribute("name");
						String cveSource = cveEntry.getAttribute("source");
						String cveTemp = cveEntry.getAttribute("url");
						String cveUrl = cveTemp.replace("'","\\'");
						if (cveName.isEmpty()) {
							System.out.println(" There is no name of CVE (refs_modified)\n" + fpath);
						}
						else if (cveSource.isEmpty()) {
							System.out.println(" There is no source of CVE (refs_modified)\n" + fpath + ", " + cveName);
						}
						else if (cveUrl.isEmpty()) {
							System.out.println(" There is no url of CVE (refs_modified)\n" + fpath + ", " + cveName);
						}
						else {
							String parser1 = cveName.split("-")[1];
							int parsint = Integer.parseInt(parser1);
							if (parsint < 2002) {
								parser1 = "2002";
							}
							else {
								// nothing to do
							}
							
							if (!prevName.equals(cveName)) {
								deleteData(conn, parser1+"_refs", cveName);
								prevName = cveName;
							}
							else {
								// nothing to do
							}
							
							String refsInsertQuery = "INSERT INTO ? " +
									"(name, source, url) " +
									"VALUES (?,?,?); ";
							String[] input = {parser1+"_refs ",cveName, cveSource, cveUrl};
							try {
								nvdQuery(conn, refsInsertQuery, input);
							} catch (Exception e) {
								System.out.println(" Cannot insert data into "+parser1+"_refs");
								throw e;
							} finally {
								///nothing to do
								
							}
							
							
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
	public void uploadModifiedVuln (Connection conn, String fpath, boolean IsNewYear, int newyear) throws Exception {
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
					NodeList nList = makeNodeList(inputFile);
					if(IsNewYear) {
						createVuln(conn, newyear+"_vuln", newyear+"_base");
					}
					else {
						//nothing to do
					}
					dropTable(conn, "modified_vuln");
					createVuln(conn, "modified_vuln", "modified_base");
					loadXmltoTable(conn, "modified_vuln");
					String prevName = "";
					int procrate = 5;
					for (int temp = 0; temp < nList.getLength(); temp++) {
						// parse year of CVE
						Element cveEntry = (Element) nList.item(temp);
						String cveName = cveEntry.getAttribute("name");
						String cveTemp = cveEntry.getAttribute("prodname");
						String cveProd = cveTemp.replace("'", "\\'");
						String cveVen = cveEntry.getAttribute("vendor");
						String cveNum = cveEntry.getAttribute("num");
						String cveEdi = cveEntry.getAttribute("edition");
						if (cveName.isEmpty()) {
							System.out.println(" There is no name of CVE (vuln_modified)\n" + fpath);
						}
						else if (cveProd.isEmpty()) {
							System.out.println(" There is no prodname of CVE (vuln_modified)\n" + fpath + ", " + cveName);
						}
						else if (cveVen.isEmpty()) {
							System.out.println(" There is no vendor of CVE (vuln_modified)\n" + fpath + ", " + cveName);
						}
						else {
							String parser1 = cveName.split("-")[1];
							StringBuffer query = new StringBuffer();
							StringBuffer query2 = new StringBuffer();
							
							int parsint = Integer.parseInt(parser1);
							if (parsint < 2002) {
								parser1 = "2002";
							}
							else {
								// nothing to do
							}
							if (!prevName.equals(cveName)) {
								deleteData(conn, parser1+"_vuln", cveName);
								prevName = cveName;
							}
							else {
								// nothing to do
							}
							query.append("INSERT INTO "+parser1+"_vuln " +
									"(name, prodname, vendor");
							query2.append("VALUES ('"+cveName+"','"+cveProd+"','"+cveVen+"'");
							
							if (!cveNum.isEmpty()) {
								query.append(", num");
								query2.append(", '"+cveNum+"'");
								
							}
							if (!cveEdi.isEmpty()) {
								query.append(", edition");
								query2.append(", '"+cveEdi+"'");
								
							}
							else {
								//nothing to do
							}
							query.append(") ");
							query2.append(");");
							
							query.append(query2.toString());
							
							nvdQuery(conn,query.toString(),null);
							
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
	public void setTestingTable (Connection conn) throws Exception{
		
		try {
			setForeignKey(conn, 0);
			dropTable(conn, "2999_base");
			createBase(conn, "2999_base");
			dropTable(conn, "test_modified_base");
			createBase(conn, "test_modified_base");
			setForeignKey(conn, 1);
			
			dropTable(conn, "2999_refs");
			dropTable(conn, "2999_vuln");
			
			dropTable(conn, "test_modified_refs");
			dropTable(conn, "test_modified_vuln");
			
			createRefs(conn, "2999_refs","2999_base");
			createVuln(conn, "2999_vuln","2999_base");
			
			createRefs(conn, "test_modified_refs", "test_modified_base");
			createVuln(conn, "test_modified_vuln", "test_modified_base");
			String baseInsertQuery = "INSERT INTO 2999_base VALUES ('CVE-2999-9999', '2999-9999', 'CVE', '2018-10-29', '2018-10-29', '(AV:N/AC:L/Au:N/C:P/I:P/A:P)', 10, 10, 10, 2, 'High', 'Test Description (MySQL)')";
			String refsInsertQuery = "INSERT INTO 2999_refs VALUES ('CVE-2999-9999', 'TESTSRC', 'https://tde.sktelecom.com')";
			String vulnInsertQuery = "INSERT INTO 2999_vuln VALUES ('CVE-2999-9999', 'Testprod', 'Testvendor', 'Testnum', 'Testedition')";
			String mBaseInsertQuery = "INSERT INTO test_modified_base VALUES ('CVE-2999-9999', '2999-9999', 'CVE', '2018-10-29', '2018-10-29', '(AV:N/AC:L/Au:S/C:N/I:N/A:C)', 0, 0, 0, 3, 'Low', 'Modified Description (MySQL)')";
			String mRefsInsertQuery = "INSERT INTO test_modified_refs VALUES ('CVE-2999-9999', 'TESTSRC', 'https://tde.sktelecom.com')";
			String mRefsInsertQuery2 = "INSERT INTO test_modified_refs VALUES ('CVE-2999-9999', 'MODSRC', 'https://thub.sktelecom.com')";
			String mVulnInsertQuery = "INSERT INTO test_modified_vuln VALUES ('CVE-2999-9999', 'Testprod', 'Testvendor', 'Testnum', 'Testedition')";
			String mVulnInsertQuery2 = "INSERT INTO test_modified_vuln VALUES ('CVE-2999-9999', 'Modprod', 'Modvendor', 'Modnum', 'Modedition')";
			nvdQuery(conn, baseInsertQuery, null);
			nvdQuery(conn, refsInsertQuery, null);
			nvdQuery(conn, vulnInsertQuery, null);
			nvdQuery(conn, mBaseInsertQuery, null);
			nvdQuery(conn, mRefsInsertQuery, null);
			nvdQuery(conn, mRefsInsertQuery2, null);
			nvdQuery(conn, mVulnInsertQuery, null);
			nvdQuery(conn, mVulnInsertQuery2, null);
			
		}
		catch (Exception e) {
			
			System.out.println(" DROP and CREATE testing base table failed ");
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void createBase (Connection conn, String nTable) throws Exception{
		String createQuery = "CREATE TABLE ?(\n" +
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
			    
			    ") ";
		String input[] = {nTable};
		nvdQuery(conn, createQuery, input);
	}
	
	public void createRefs(Connection conn, String nTable, String baseTable) throws Exception {
		String createQuery = "CREATE TABLE ?(\n" 
				+ "name char(20) not null,\n"
				+ "source text, \n"
				+ "url text, \n"
				
				+ "foreign key(name) references "
				+ "?(name)\n"
				+ "	on delete cascade)";
		String input[] = {nTable, baseTable};
		nvdQuery(conn, createQuery, input);
	}
	
	public void createVuln (Connection conn, String nTable, String baseTable) throws Exception{
		String createQuery = "CREATE TABLE ?(\n" + "name char(20) not null,\n" + "prodname text, \n"
				+ "vendor text, \n" + "num text, \n" + "edition text, \n" 
				+ "foreign key(name) references " + "?(name)\n" + "	on delete cascade)";
		String input[] = {nTable, baseTable};
		nvdQuery(conn, createQuery, input);
	}
	
	public void loadXmltoTable (Connection conn, String nTable) throws Exception{
		String loadQuery = "LOAD XML LOCAL INFILE './"+ZipTagXml.translated+"/nvdcve-"+nTable+".xml'\n" +
				"INTO TABLE "+nTable+"\n" +
				"ROWS IDENTIFIED BY '<entry>';";
		nvdQuery(conn, loadQuery, null);
		
	} 
	
	public void dropTable (Connection conn, String nTable) throws Exception {
		String dropQuery = "drop table if exists "+nTable;
		nvdQuery(conn, dropQuery, null);
	}
	
	public void deleteData(Connection conn, String nTable, String nData) throws Exception{
		String deleteQuery = "DELETE FROM ? WHERE name= ?;";
		String input[] = {nTable, nData};
		nvdQuery(conn, deleteQuery, input);
	}
	
	public Connection connectToDB(Connection conn) throws Exception{
		
		try {
			if (conn == null) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/nvd?serverTimezone=UTC", ZipTagXml.dbId, ZipTagXml.dbPw);
				
			} else {
				if (conn.isClosed()) {
					Class.forName("com.mysql.cj.jdbc.Driver");
					conn = DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/nvd?serverTimezone=UTC", ZipTagXml.dbId, ZipTagXml.dbPw);
					
				} else {
					System.out.println(" Connection already exists");
					
				}
			}
			return conn;
		} catch (Exception e) {
			System.out.println(" connectToDB failed");
			return null;
		} 
	}
	
	public void disconnectDB(Connection conn) throws Exception {
		try {
			if(!conn.isClosed() && conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			System.out.println(" disconnectDB failed");
			throw e;
		}
	}
	
	public NodeList makeNodeList(File inputFile) throws Exception {
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
		nvdQuery(conn, setKeyQuery, null);

	}
	
	public void nvdQuery (Connection conn, String query, String input[]) throws Exception {
		
		try (PreparedStatement queryCreate = conn.prepareStatement(query)){
			
			if (input == null) {
				///nothing to do
			} else {
				for(int inputIndex = 0; inputIndex < input.length; inputIndex++) {
					queryCreate.setString(inputIndex, input[inputIndex]);
				}
			}
			
			queryCreate.execute();
		} catch (Exception e) {
			System.out.println(" Query Error!");
			throw e;
		} finally {
			///nothing to do
		}
	}
}