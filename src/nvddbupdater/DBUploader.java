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
	Logwriter logwriter = new Logwriter();
	String baseType = "_base";
	String refsType = "_refs";
	String vulnType = "_vuln";
	String modifiedBase = "modified_base";
	String modifiedRefs = "modified_refs";
	String modifiedVuln = "modified_vuln";
	/**
	 * @brief	This method make query that drop table of 'nvd' database and make a new table
	 * @param	fpath
	 * 			type: String
	 * 			fpath is file path you will upload on 'nvd' database
	 * 			The file name should be '<year>_base.xml'
	 * 			The table name will be created on 'nvd' database is <year>_base
	 */
	public boolean createInitLogMessage (String fpath, String typeTable) {
		if(fpath.isEmpty()) {
			logwriter.writeConsole(" File_base path is empty.");
			return false;
		}
		else {
			File fpth = new File(fpath);
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			if (!fpth.exists()) {
				logwriter.writeConsole(" "+fpath + " does not exist.");
				logwriter.writeConsole(" If there is " + parser2 + typeTable + " table already, use it.");
				logwriter.writeConsole(" If not, " + parser2 + typeTable + " table is not created.");
				
				return false;
			}
			else if (!fpth.isFile()) {
				logwriter.writeConsole(" "+fpath + " is not a file.");
				logwriter.writeConsole(" If there is " + parser2 + typeTable + " table already, use it.");
				logwriter.writeConsole(" If not, " + parser2 + typeTable + " table is not created.");
				return false;
			}
			else {
				return true;
			}
		}
	}
		
	public boolean createUpdateLogMessage (String fpath, String nTable) {
		if(fpath.isEmpty()) {
			logwriter.writeConsole(" "+nTable+" File path is empty.");
			return false;
		}
		else {
			File inputFile = new File(fpath);
			if (!inputFile.exists()) {
				logwriter.writeConsole(" "+fpath + " does not exist.");
				logwriter.writeConsole(" Update "+nTable+" failed.");
				return false;
			}
			else if (!inputFile.isFile()) {
				logwriter.writeConsole(" "+fpath + " is not a file.");
				logwriter.writeConsole(" Update "+nTable+" failed.");
				return false;
			}
			else {
				return true;
			}
		}
		
	}
	
	public void initNUploadBase(Connection conn, String fpath) throws Exception {
		
		boolean canExecute = createInitLogMessage(fpath, baseType);
		
		if(canExecute) {
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			String nTable = parser2+baseType;
			try {
				setForeignKey(conn, 0);
				dropTable(conn, nTable);
				createBase(conn, nTable);
				loadXmltoTable(conn, nTable);
				setForeignKey(conn, 1);
				logwriter.writeConsole(" DROP and CREATE base "+parser2);
			}
			catch (Exception e) {
				logwriter.writeConsole(" DROP and CREATE base failed "+parser2);
				throw e;
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
		
		boolean canExecute = createInitLogMessage(fpath, refsType);
		
		if(canExecute) {
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			String nTable = parser2 + refsType;
			String baseTable = parser2 + baseType;
			try {
				dropTable(conn, nTable);	
				createRefs(conn, nTable, baseTable);
				loadXmltoTable(conn, nTable);
				logwriter.writeConsole(" DROP and CREATE refs "+parser2);
			}
			catch (Exception e) {
				logwriter.writeConsole(" DROP and CREATE refs failed "+parser2);
				throw e;
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
		String typeTable = vulnType;
		boolean canExecute = createInitLogMessage(fpath, typeTable);
		
		if(canExecute) {
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			String nTable = parser2+typeTable;
			String baseTable = parser2+baseType;
			try {
				dropTable(conn, nTable);	
				createVuln(conn, nTable, baseTable);
				loadXmltoTable(conn, nTable);
				logwriter.writeConsole(" DROP and CREATE vuln "+parser2);
			}
			catch (Exception e) {
				logwriter.writeConsole(" DROP and CREATE vuln failed "+parser2);
				throw e;
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
	 * @param	isNewYear
	 * 			type: boolean
	 * 			IsNewYear notices whether the last update is run on last year or not
	 * 			If the last update is on last year, IsNewYear is true and create <newyear>_base table ex) 2019_base
	 * 			If not, IsNewYear is false and just update tables and create modified_base table
	 * @param	newyear
	 * 			newyear is the year when this method is called
	 * 			If IsNewYear is true, this method create new table referenced by newyear
	 * @throws	Exception
	 */
	public void uploadModifiedBase (Connection conn, String fpath, boolean isNewYear, int newyear) throws Exception {
		
		logwriter.writeConsole(" ");
		boolean canExecute = createUpdateLogMessage(fpath, modifiedBase);
		if (canExecute) {
				
			try {
				// Load translated modified file.
				
				// make node list.
				
				if(isNewYear) {
					String newTable = newyear + baseType;
					createBase(conn, newTable);
				}
				else {
					///nothing to do
				}
				File inputFile = new File(fpath);
				setForeignKey(conn, 0);
				dropTable(conn, modifiedBase);
				createBase(conn, modifiedBase);
				loadXmltoTable(conn, modifiedBase);
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
					StringBuilder query1 = new StringBuilder();
					StringBuilder query2 = new StringBuilder();
					StringBuilder query3 = new StringBuilder();
					
					int parsint = Integer.parseInt(parser1);
					if (parsint < 2002) {
						parser1 = "2002";
					}
					else {
						//nothing to do
					}
					if (cveN.isEmpty()) {
						logwriter.writeConsole(" There is no name of CVE (base_modified)\n" + fpath);
					}
					else if (cveSeq.isEmpty()) {
						logwriter.writeConsole(" There is no seq of CVE (base_modified)\n" + fpath + ", " + cveN);
					}
					else if (cveType.isEmpty()) {
						logwriter.writeConsole(" There is no type of CVE (base_modified)\n" + fpath + ", " + cveN);
					}
					else if (cvePub.isEmpty()) {
						logwriter.writeConsole(" There is no published of CVE (base_modified)\n" + fpath + ", " + cveN);
					}
					else if (cveMod.isEmpty()) {
						logwriter.writeConsole(" There is no modified of CVE (base_modified)\n" + fpath + ", " + cveN);
					}
					else if (cveDesc.isEmpty()) {
						logwriter.writeConsole(" There is no `desc` of CVE (base_modified)\n" + fpath + ", " + cveN);
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
						logwriter.writeConsole(" upload modified base "+procrate+"%");
						procrate = procrate + 5;
					}
				}
				
				logwriter.writeConsole(" upload modified base complete");
			} catch (Exception e) {
				logwriter.writeConsole(" upload modified base failed");
				throw e;
			}  finally {
				///nothing to do
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
	 * @param	isNewYear
	 * 			type: boolean
	 * 			IsNewYear notices whether the last update is run on last year or not
	 * 			If the last update is on last year, IsNewYear is true and create <newyear>_refs table ex) 2019_refs
	 * 			If not, IsNewYear is false and just update tables and create modified_refs table
	 * @param	newyear
	 * 			newyear is the year when this method is called
	 * 			If IsNewYear is true, this method create new table referenced by newyear
	 * @throws	Exception
	 */
	public void uploadModifiedRefs (Connection conn, String fpath, boolean isNewYear, int newyear) throws Exception {
		
		logwriter.writeConsole(" ");
		boolean canExecute = createUpdateLogMessage(fpath, modifiedRefs);
		
		if(canExecute) {
				
			try {
				// Load translated modified file.
				
				// make node list.
				File inputFile = new File(fpath);
				NodeList nList = makeNodeList(inputFile);
				if(isNewYear) {
					createRefs(conn, newyear+refsType,newyear+baseType);
				}
				else {
					//nothing to do
				}
				dropTable(conn, modifiedRefs);
				createRefs(conn, modifiedRefs, modifiedBase);
				loadXmltoTable(conn, modifiedRefs);
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
						logwriter.writeConsole(" There is no name of CVE (refs_modified)\n" + fpath);
					}
					else if (cveSource.isEmpty()) {
						logwriter.writeConsole(" There is no source of CVE (refs_modified)\n" + fpath + ", " + cveName);
					}
					else if (cveUrl.isEmpty()) {
						logwriter.writeConsole(" There is no url of CVE (refs_modified)\n" + fpath + ", " + cveName);
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
							deleteData(conn, parser1+refsType, cveName);
							prevName = cveName;
						}
						else {
							// nothing to do
						}
						
						String refsInsertQuery = "INSERT INTO " + parser1+refsType+
								"(name, source, url) " +
								"VALUES (?,?,?); ";
						String[] input = {cveName, cveSource, cveUrl};
						try {
							nvdQuery(conn, refsInsertQuery, input);
						} catch (Exception e) {
							logwriter.writeConsole(" Cannot insert data into "+parser1+refsType);
							throw e;
						} finally {
							///nothing to do
							
						}
						
						
					}
					if(temp*100/nList.getLength() > procrate) {
						logwriter.writeConsole(" upload modified refs "+procrate+"%");
						procrate = procrate + 5;
					}
					
				}
			
				logwriter.writeConsole(" upload modified refs complete");
			} catch (Exception e) {
				logwriter.writeConsole(" upload modified refs failed");
				throw e;
			}  finally {
				///nothing to do
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
	 * @param	isNewYear
	 * 			type: boolean
	 * 			IsNewYear notices whether the last update is run on last year or not
	 * 			If the last update is on last year, IsNewYear is true and create <newyear>_vuln table ex) 2019_vuln
	 * 			If not, IsNewYear is false and just update tables and create modified_vuln table
	 * @param	newyear
	 * 			newyear is the year when this method is called
	 * 			If IsNewYear is true, this method create new table referenced by newyear
	 * @throws	Exception
	 */
	public void uploadModifiedVuln (Connection conn, String fpath, boolean isNewYear, int newyear) throws Exception {
		
		logwriter.writeConsole(" ");
		boolean canExecute = createUpdateLogMessage(fpath, modifiedVuln);
		if(canExecute) {
		
				
			try {
				// Load translated modified file.
				
				// make node list.
				File inputFile = new File(fpath);
				NodeList nList = makeNodeList(inputFile);
				if(isNewYear) {
					createVuln(conn, newyear+vulnType, newyear+baseType);
				}
				else {
					//nothing to do
				}
				dropTable(conn, modifiedVuln);
				createVuln(conn, modifiedVuln, modifiedBase);
				loadXmltoTable(conn, modifiedVuln);
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
						logwriter.writeConsole(" There is no name of CVE (vuln_modified)\n" + fpath);
					}
					else if (cveProd.isEmpty()) {
						logwriter.writeConsole(" There is no prodname of CVE (vuln_modified)\n" + fpath + ", " + cveName);
					}
					else if (cveVen.isEmpty()) {
						logwriter.writeConsole(" There is no vendor of CVE (vuln_modified)\n" + fpath + ", " + cveName);
					}
					else {
						String parser1 = cveName.split("-")[1];
						StringBuilder query = new StringBuilder();
						StringBuilder query2 = new StringBuilder();
						
						int parsint = Integer.parseInt(parser1);
						if (parsint < 2002) {
							parser1 = "2002";
						}
						else {
							// nothing to do
						}
						if (!prevName.equals(cveName)) {
							deleteData(conn, parser1+vulnType, cveName);
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
						logwriter.writeConsole(" upload modified vuln "+procrate+"%");
						procrate = procrate + 5;
					}
				}
				
				
				
				logwriter.writeConsole(" upload modified vuln complete");
			} catch (Exception e) {
				logwriter.writeConsole(" upload modified vuln failed");
				throw e;
			}  finally {
				///nothing to do
			}
			
		}
		 
	/**
	 * @brief	make testing table on nvd database
	 */
	}
	public void setTestingTable (Connection conn) throws Exception{
		
		try {
			String testBase = "2999_base";
			String testRefs = "2999_refs";
			String testVuln = "2999_vuln";
			String testModBase = "test_modified_base";
			String testModRefs = "test_modified_refs";
			String testModVuln = "test_modified_vuln";
			setForeignKey(conn, 0);
			dropTable(conn, testBase);
			createBase(conn, testBase);
			dropTable(conn, testModBase);
			createBase(conn, testModBase);
			setForeignKey(conn, 1);
			
			dropTable(conn, testRefs);
			dropTable(conn, testVuln);
			
			dropTable(conn, testModRefs);
			dropTable(conn, testModVuln);
			
			createRefs(conn, testRefs,testBase);
			createVuln(conn, testVuln,testBase);
			
			createRefs(conn, testModRefs, testModBase);
			createVuln(conn, testModVuln, testModBase);
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
			
			logwriter.writeConsole(" DROP and CREATE testing base table failed ");
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	public void createBase (Connection conn, String nTable) throws Exception{
		String createQuery = "CREATE TABLE "+nTable+
				"(\n name char(20) not null unique,\n seq text,\n" +
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
		
		nvdQuery(conn, createQuery, null);
	}
	
	public void createRefs(Connection conn, String nTable, String baseTable) throws Exception {
		String createQuery = "CREATE TABLE "+nTable
				+ "(\n name char(20) not null,\n source text, \n"
				+ "url text, \n"
				
				+ "foreign key(name) references "
				+ baseTable
				+ "(name)\n"
				+ "	on delete cascade)";
		
		nvdQuery(conn, createQuery, null);
	}
	
	public void createVuln (Connection conn, String nTable, String baseTable) throws Exception{
		String createQuery = "CREATE TABLE "+nTable+"(\n name char(20) not null,\n prodname text, \n"
				+ "vendor text, \n" + "num text, \n" + "edition text, \n" 
				+ "foreign key(name) references " + baseTable+"(name)\n" + "	on delete cascade)";
		
		nvdQuery(conn, createQuery, null);
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
		String deleteQuery = "DELETE FROM "+nTable+" WHERE name= ?;";
		String[] input = {nData};
		nvdQuery(conn, deleteQuery, input);
	}
	
	public Connection connectToDB() throws Exception{
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/nvd?serverTimezone=UTC", ZipTagXml.dbId, ZipTagXml.dbPw);
		} catch (Exception e) {
			logwriter.writeConsole(" connectToDB failed");
			return null;
		} 
	}
	
	
	
	public NodeList makeNodeList(File inputFile) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			return doc.getElementsByTagName("entry");
		} catch (Exception e) {
			logwriter.writeConsole(" makeNodeList failed");
			throw e;
		}

	}
	
	public void setForeignKey(Connection conn, int key) throws Exception{
		String setKeyQuery = "set foreign_key_checks = "+key;
		nvdQuery(conn, setKeyQuery, null);

	}
	
	public void nvdQuery (Connection conn, String query, String[] input) throws Exception {
		
		try (PreparedStatement queryCreate = conn.prepareStatement(query)){
			
			if (input == null) {
				///nothing to do
			} else {
				int queryIndex = 1;
				for(int inputIndex = 0; inputIndex < input.length; inputIndex++) {
					queryCreate.setString(queryIndex, input[inputIndex]);
					queryIndex = queryIndex + 1;
				}
			}
			
			queryCreate.executeUpdate();
		} catch (Exception e) {
			logwriter.writeConsole(" Query Error!");
			throw e;
		} finally {
			///nothing to do
		}
	}
}