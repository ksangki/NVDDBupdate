/**
 * @file	DBUploader.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	This file contains a class that have methods to work on DB
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 *  - 2018.	12.	14	Joh Rang Hyun	StAX version
 */
package nvddbupdater;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @class	DBUploader
 * @brief	A class can interact with NVD DB
 * @warning	Require 'mysql-connector-java-8.0.12,jar' file on classpath
 */
public class DBUploader {
	static ZipTagXml ztx = new ZipTagXml();
	//StAX version
	String baseType = "_base";
	String refsType = "_refs";
	String vulnType = "_vuln";
	String modifiedBase = "modified_base";
	String modifiedRefs = "modified_refs";
	String modifiedVuln = "modified_vuln";
	String[] baseCharAtt = {"name", "published", "modified", "seq", "type", "severity",  "CVSS_vector", "`desc`"};
	String[] baseIntAtt = {"CVSS_base_score", "CVSS_exploit_subscore", "CVSS_impact_subscore","CVSS_version"};
	String[] refsAtt = {"name", "source", "url"};
	String[] vulnAtt = {"name", "prodname", "vendor", "num", "edition"};
	String createTable = "CREATE TABLE ";
	
	/**
	 * @brief	Check xml file and write log
	 * @param	fpath
	 * 			type: String
	 * 			File path of xml file
	 * @param	typeTable
	 * 			type: String
	 * 			_base, _refs, _vuln
	 * @return	True if the file exists
	 * 			If not, false
	 */
	public boolean createInitLogMessage (String fpath, String typeTable) {
		if(fpath.isEmpty()) {
			Logwriter.writeConsole(" File_base path is empty.\n");
			return false;
		}
		else {
			File fpth = new File(fpath);
			String parser2 = "";
			String parser1 = fpath.split("-")[1];
			parser2 = parser1.split("_")[0];
			if (!fpth.exists()) {
				Logwriter.writeConsole(" "+fpath + " does not exist.\n");
				Logwriter.writeConsole(" If there is " + parser2 + typeTable + " table already, use it.\n");
				Logwriter.writeConsole(" If not, " + parser2 + typeTable + " table is not created.\n");
				
				return false;
			}
			else if (!fpth.isFile()) {
				Logwriter.writeConsole(" "+fpath + " is not a file.\n");
				Logwriter.writeConsole(" If there is " + parser2 + typeTable + " table already, use it.\n");
				Logwriter.writeConsole(" If not, " + parser2 + typeTable + " table is not created.\n");
				return false;
			}
			else {
				return true;
			}
		}
	}
	
	/**
	 * @brief	check the xml file and write log
	 * @param	fpath
	 * 			type: String
	 * 			file path of xml file
	 * @param	nTable
	 * 			type: String
	 * 			the name of table to update
	 * @return	true if the file exists
	 * 			if not, false
	 */
	public boolean createUpdateLogMessage(String fpath, String nTable) {
		if (fpath.isEmpty()) {
			Logwriter.writeConsole(" "+nTable+" File path is empty.\n");
			return false;
		}
		else {
			File inputFile = new File(fpath);
			if (!inputFile.exists()) {
				Logwriter.writeConsole(" "+fpath + " does not exist.\n");
				Logwriter.writeConsole(" Update "+nTable+" failed.\n");
				return false;
			}
			else if (!inputFile.isFile()) {
				Logwriter.writeConsole(" "+fpath + " is not a file.\n");
				Logwriter.writeConsole(" Update "+nTable+" failed.\n");
				return false;
			}
			else {
				return true;
			}
		}
		
	}
	
	/**
	 * @brief	drop base tables and upload base xml files
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	fpath
	 * 			type: String
	 * 			file path of base xml file
	 * @throws	SQLException
	 */
	public void initNUploadBase(Connection conn, String fpath) throws SQLException {
		
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
				Logwriter.writeConsole(" DROP and CREATE base "+parser2+"\n");
			}
			catch (SQLException e) {
				Logwriter.writeConsole(" DROP and CREATE base failed "+parser2+"\n");
				throw e;
			}
		}
		
	}
	
	/**
	 * @brief	drop refs tables and upload refs xml files
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	fpath
	 * 			type: String
	 * 			file path of refs xml file
	 * @throws	SQLException
	 */
	public void initNUploadRefs(Connection conn, String fpath) throws SQLException {
		
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
				Logwriter.writeConsole(" DROP and CREATE refs "+parser2+"\n");
			}
			catch (SQLException e) {
				Logwriter.writeConsole(" DROP and CREATE refs failed "+parser2+"\n");
				throw e;
			} 
			
		}
		 
		
	}
	

	/**
	 * @brief	drop vuln tables and upload vuln xml files
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	fpath
	 * 			type: String
	 * 			file path of vuln xml file
	 * @throws	SQLException
	 */
	public void initNUploadVuln(Connection conn, String fpath) throws SQLException {
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
				Logwriter.writeConsole(" DROP and CREATE vuln "+parser2+"\n");
			}
			catch (SQLException e) {
				Logwriter.writeConsole(" DROP and CREATE vuln failed "+parser2+"\n");
				throw e;
			}  
			
		}
		
	}
	
	/**
	 * @brief	initialize a Map with array
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			the Map to be initialized
	 * @param	cveAtt
	 * 			type: String[]
	 * 			contains attributes of map
	 */
	public void initMap (Map<String, String> cveMap, String[] cveAtt) {
		cveMap.clear();
		for (int i = 0; i < cveAtt.length; i++) {
			cveMap.put(cveAtt[i], "");
		}
		
	}
	
	/**
	 * @brief	read XMLEvent and map the values into cveMap
	 * @param	event
	 * 			type: XMLEvent
	 * 			start of element, end of element, etc.
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			save the values of attributes of element
	 */
	@SuppressWarnings("unchecked")
	public void mappingAtts (XMLEvent event, Map<String, String> cveMap) {
            Iterator<Attribute> attributes = event.asStartElement().getAttributes();
            while (attributes.hasNext()){
	              Attribute attE = attributes.next();
	              if (attE.getName().toString().equals("desc")) {
	            	  cveMap.put("`desc`", attE.getValue());
	              } else {
	            	  cveMap.put(attE.getName().toString(), attE.getValue());
	              }
	              
            }
	}
	
	/**
	 * @brief	update data with cveMap into already created table
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	parsedTableName
	 * 			type: String
	 * 			the name of table to be updated
	 * @param	cveMap
	 * 			type: Map<String, String>
	 * 			attributes to be reference for update
	 * @param	cveAtt
	 * 			type: String[]
	 * 			name of columns of table to be updated (DB String value)
	 * @param	cveAtt2
	 * 			type: String[]
	 * 			name of columns of table to be updated (DB integer value)
	 * @param	isBaseTable
	 * 			type: boolean
	 * 			true if the table should be updated is base table. if not, false
	 * @param	needToDelete
	 * 			type: boolean
	 * 			true if original data should be deleted. if not, false
	 * @return
	 */
	public boolean updateQuery (Connection conn, String parsedTableName, Map<String, String> cveMap, String[] cveAtt, String[] cveAtt2, boolean isBaseTable, boolean needToDelete) {
		StringBuilder query1 = new StringBuilder();
		StringBuilder query2 = new StringBuilder();
		StringBuilder query3 = new StringBuilder();
		query1.append("INSERT INTO "+parsedTableName+" ( ");
		query2.append("VALUES ( ");
		query3.append("ON DUPLICATE KEY UPDATE ");
		for (int i = 0 ; i < cveAtt.length ; i++) {
			if (i > 0) {
				query1.append(", ");
				query2.append(", ");
				query3.append(", ");
			}
			query1.append(cveAtt[i]);
			query2.append("\""+cveMap.get(cveAtt[i]).replace("\\", "\\"+"\\").replaceAll("'", "\\'").replaceAll("&","&amp;").replaceAll("\"","&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")+"\"");
 		   if (isBaseTable) {
 			   query3.append(cveAtt[i]+"=\""+cveMap.get(cveAtt[i]).replace("\\", "\\"+"\\").replaceAll("'", "\\'").replaceAll("&","&amp;").replaceAll("\"","&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")+"\"");
 		   } 
 	   }
		if (isBaseTable) {
			for (int i = 0 ; i < cveAtt2.length ; i++) {
				query1.append(", ");
				query2.append(", ");
				query3.append(", ");
				query1.append(cveAtt2[i]);
				query2.append(cveMap.get(cveAtt2[i]));
	 			query3.append(cveAtt2[i]+"="+cveMap.get(cveAtt2[i]));
	 		   
	 	   }
		}
 	   query1.append(") ");
 	   query2.append(") ");
 	   query1.append(query2.toString());
 	   try {
	 	   if (isBaseTable) {
	 		   query1.append(query3.toString());
	 	   } else {
	 		   if (needToDelete) {
	 			   deleteData(conn, parsedTableName, cveMap.get("name"));
	 		   }
	 	   }
	 	  nvdQuery(conn,query1.toString(),null);
	 	  
	 	  return true;
 	   } catch (Exception e) {
		  return false;
	   }
	}
	
	/**
	 * @brief	check in which table cve should be input
	 * @param	cveName
	 * 			type: String
	 * 			cve name
	 * @param	tableType
	 * 			type: String
	 * 			_base, _refs, _vuln
	 * @return	String parsedTableName
	 */
	public String parseTableName (String cveName, String tableType) {
		String parsedTableName = "";
		//System.out.println("cveName="+cveName);
		String parser1 = cveName.split("-")[1];
		int parsint = Integer.parseInt(parser1);
		if (parsint < 2002) {
			parser1 = "2002";
		}
		parsedTableName = parser1+tableType;
		return parsedTableName;
		
	}
	
	/**
	 * @brief	update table with xml file
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	tableType
	 * 			type: String
	 * 			_base, _refs, _vuln
	 * @param	xmlFilePath
	 * 			type: String
	 * 			file path of xml file
	 * @param	isBaseTable
	 * 			type: boolean
	 * 			true if the table is base table. if not, false
	 * @param	cveAtt
	 * 			type: String[]
	 * 			name of columns of table to be updated (DB String value)
	 * @param	cveAtt2
	 * 			type: String[]
	 * 			name of columns of table to be updated (DB integer value)
	 * @return	true if update is success. if not, false
	 */
	public boolean updateTable(Connection conn, String tableType ,String xmlFilePath, boolean isBaseTable ,String[] cveAtt,String[] cveAtt2) {
		XMLEventReader eventReader = new MakeDataStructure().getEventReader(xmlFilePath);
		Map<String, String> cveMap = new HashMap<>();
		String prevName = ""; 
		int proc = 0;
		int done = 5000;
		initMap(cveMap, cveAtt);
		//System.out.println("updateTable ");
		try {
			while(eventReader.hasNext()) {
		        XMLEvent event = eventReader.nextEvent();
		        if(event.getEventType() == XMLStreamConstants.START_ELEMENT) {
		        	if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase("entry")) {
		        		   mappingAtts(event, cveMap);
		        	   }
		        } else if(event.getEventType() == XMLStreamConstants.END_ELEMENT) {
		        	EndElement endElement = event.asEndElement();
		              
			           if(endElement.getName().getLocalPart().equalsIgnoreCase("entry")) {
			        	   if(!cveMap.get("name").equals("null"))
				        	   if (!updateQuery (conn, parseTableName(cveMap.get("name"),tableType), cveMap, cveAtt,cveAtt2, isBaseTable, !prevName.equals(cveMap.get("name")))) {
				        		   return false;
				        	   }
			        	   prevName = cveMap.get("name");
			        	   initMap(cveMap, cveAtt);
			        	   proc = proc+1;
			        	   if (proc >= done) {
			        		   Logwriter.writeConsole(" update other tables with modified"+tableType+": "+proc+" rows\n");
			        		   done = done + 5000;
			        	   }
			           }
		        }
		       
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("XML Error"+e);
			return false;
		}
		
	}
	/**
	 * @brief	This method make query that update <year>_base tables of 'nvd' database and make a new table named modified_base
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
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
	 * @throws	SQLException
	 */
	public void uploadModifiedBase (Connection conn, String fpath, boolean isNewYear, int newyear) throws SQLException {
		
		Logwriter.writeConsole(" \n");
		boolean canExecute = createUpdateLogMessage(fpath, modifiedBase);
		if (canExecute) {
				
			try {
				
				
				if(isNewYear) {
					String newTable = newyear + baseType;
					createBase(conn, newTable);
				}
				else {
					///nothing to do
				}
				
				setForeignKey(conn, 0);
				dropTable(conn, modifiedBase);
				createBase(conn, modifiedBase);
				loadXmltoTable(conn, modifiedBase);
				setForeignKey(conn, 1);
				updateTable(conn, baseType, fpath, true ,baseCharAtt, baseIntAtt);
				Logwriter.writeConsole(" upload modified base complete\n");
			} catch (SQLException e) {
				Logwriter.writeConsole(" upload modified base failed\n"+e);
				throw e;
			}  finally {
				///nothing to do
			}
				
			
		}
		 
		
	}
	
	

	/**
	 * @brief	This method make query that update <year>_refs tables of 'nvd' database and make a new table named modified_refs
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
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
	 * @throws	SQLException
	 */
	public void uploadModifiedRefs (Connection conn, String fpath, boolean isNewYear, int newyear) throws SQLException {
		
		Logwriter.writeConsole(" uploadModifiedRefs \n");
		boolean canExecute = createUpdateLogMessage(fpath, modifiedRefs);
		
		if(canExecute) {
			try {
				
				if(isNewYear) {
					createRefs(conn, newyear+refsType,newyear+baseType);
				}
				else {
					//nothing to do
				}
				dropTable(conn, modifiedRefs);
				createRefs(conn, modifiedRefs, modifiedBase);
				loadXmltoTable(conn, modifiedRefs);
				updateTable(conn, refsType, fpath, false ,refsAtt, null);
				
				Logwriter.writeConsole(" upload modified refs complete\n");
			} catch (Exception e) {
				Logwriter.writeConsole(" upload modified refs failed\n"+e);
				throw e;
			}
			
			
		}
		
	}
	

	/**
	 * @brief	This method make query that update <year>_vuln tables of 'nvd' database and make a new table named modified_vuln
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
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
	 * @throws	SQLException
	 */
	public void uploadModifiedVuln (Connection conn, String fpath, boolean isNewYear, int newyear) throws SQLException {
		
		Logwriter.writeConsole(" \n");
		boolean canExecute = createUpdateLogMessage(fpath, modifiedVuln);
		if(canExecute) {
		
				
			try {
				// Load translated modified file.
				
				
				if(isNewYear) {
					createVuln(conn, newyear+vulnType, newyear+baseType);
				}
				else {
					//nothing to do
				}
				dropTable(conn, modifiedVuln);
				createVuln(conn, modifiedVuln, modifiedBase);
				loadXmltoTable(conn, modifiedVuln);
				updateTable(conn, vulnType, fpath, false ,vulnAtt, null);
				
				Logwriter.writeConsole(" upload modified vuln complete\n");
			} catch (SQLException e) {
				Logwriter.writeConsole(" upload modified vuln failed\n");
				throw e;
			} 
			
		}
		
	}
	
	/**
	 * @brief	create tables for test
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @throws	SQLException
	 */
	public void setTestingTable (Connection conn) throws SQLException{
		
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
		catch (SQLException e) {
			
			Logwriter.writeConsole(" DROP and CREATE testing base table failed\n");
			throw e;
		} finally {
			///nothing to do
		}
	}
	
	/**
	 * @brief	create query that create base table
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	nTable
	 * 			the name of base table
	 * @throws	SQLException
	 */
	public void createBase (Connection conn, String nTable) throws SQLException{
		String createQuery = createTable+nTable+
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
		
		//nvdQuery(conn, createQuery, null);
	}
	
	/**
	 * @brief	create query that create refs table
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	nTable
	 * 			type: String
	 * 			the name of refs table
	 * @param	baseTable
	 * 			type: String
	 * 			the name of base table that is relational to refs table
	 * @throws	SQLException
	 */
	public void createRefs(Connection conn, String nTable, String baseTable) throws SQLException {
		String createQuery = createTable+nTable
				+ "(\n name char(20) not null,\n source text, \n"
				+ "url text, \n"
				
				+ "foreign key(name) references "
				+ baseTable
				+ "(name)\n"
				+ "	on delete cascade)";
		
		//nvdQuery(conn, createQuery, null);
	}
	
	/**
	 * @brief	create query that create vuln table
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	nTable
	 * 			type: String
	 * 			the name of vuln table
	 * @param	baseTable
	 * 			type: String
	 * 			the name of base table that is relational to vuln table
	 * @throws	SQLException
	 */
	public void createVuln (Connection conn, String nTable, String baseTable) throws SQLException{
		String createQuery = createTable+nTable+"(\n name char(20) not null,\n prodname text, \n"
				+ "vendor text, \n" + "num text, \n" + "edition text, \n" 
				+ "foreign key(name) references " + baseTable+"(name)\n" + "	on delete cascade)";
		
		//nvdQuery(conn, createQuery, null);
	}
	
	/**
	 * @brief	load xml file to particular table of DB
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	nTable
	 * 			type: String
	 * 			the name of table
	 * @throws	SQLException
	 */
	public void loadXmltoTable (Connection conn, String nTable) throws SQLException{
		String loadQuery = "LOAD XML LOCAL INFILE './"+ZipTagXml.translated+"/nvdcve-"+nTable+".xml'\n" +
				"INTO TABLE "+nTable+"\n" +
				"ROWS IDENTIFIED BY '<entry>';";
		nvdQuery(conn, loadQuery, null);
	} 
	
	/**
	 * @brief	drop a table of nvd database
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	nTable
	 * 			type: String
	 * 			the name of table should be dropped
	 * @throws	SQLException
	 */
	public void dropTable (Connection conn, String nTable) throws SQLException {
		String dropQuery = "delete from "+nTable;
		nvdQuery(conn, dropQuery, null);
	}
	
	/**
	 * @brief	delete data that has nData as its cve name from a table 
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	nTable
	 * 			type: String
	 * 			the name of table
	 * @param	nData
	 * 			type: String
	 * 			the name of cve should be deleted
	 * @throws	SQLException
	 */
	public void deleteData(Connection conn, String nTable, String nData) throws SQLException{
		String deleteQuery = "DELETE FROM "+nTable+" WHERE name= ?;";
		String[] input = {nData};
		nvdQuery(conn, deleteQuery, input);
	}
	
	/**
	 * @brief	connect to DB server and return that connection
	 * @return	Connection
	 */
	public Connection connectToDB() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/thub_prd_db?serverTimezone=UTC&allowLoadLocalInfile=true", ZipTagXml.dbId, ZipTagXml.dbPw);
		} catch (ClassNotFoundException|SQLException e) {
			Logwriter.writeConsole(" connectToDB failed\n");
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * @brief	set the foreign key option of DB by 'key'
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	key
	 * 			type: int
	 * 			option value of foreign key check option
	 * @throws	SQLException
	 */
	public void setForeignKey(Connection conn, int key) throws SQLException{
		String setKeyQuery = "set foreign_key_checks = "+key;
		nvdQuery(conn, setKeyQuery, null);

	}
	
	/**
	 * @brief	create query
	 * @param	conn
	 * 			type: Connection
	 * 			connection to DB server
	 * @param	query
	 * 			type: String
	 * 			query string
	 * @param	input
	 * 			type: String[]
	 * 			values of query string
	 * @throws	SQLException
	 */
	public void nvdQuery (Connection conn, String query, String[] input) throws SQLException {
		
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
			
		} catch (SQLException e) {
			
			Logwriter.writeConsole(" Query Error!\n"+query);
			e.printStackTrace();
			throw e;
		} 
	}
	
	/**
	 * @brief	Delete temporary content of wordpress database
	 */
	public void deleteTempPost() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			Logwriter.writeConsole(" Cannot find mysql driver\n");
			return;
		}
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://"+ZipTagXml.host+":"+ZipTagXml.port+"/thub_prd_db?serverTimezone=UTC&allowLoadLocalInfile=true", ZipTagXml.dbId, ZipTagXml.dbPw)){
			String deleteString = "DELETE FROM wp_temp_content WHERE date < now() - interval 1 day";
			nvdQuery(conn, deleteString, null);
		} catch (Exception e) {
			Logwriter.writeConsole(" Cannot delete wp_temp_content\n");
		} 
	}
}
