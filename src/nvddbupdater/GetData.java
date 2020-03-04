/**
 /**
 * @file	Get_data.java
 * @date	2018/10/29
 * @author	skt.1519040
 * @brief	This file get data from NVD(National Vulnerability Database)
 * @date
 * 	- 2018. 10. 29	Joh Rang Hyun
 * 	- 2018	12.	14	Joh Rang Hyun	StAX version
 */
package nvddbupdater;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;


/**
 * @class	Get_data
 * @brief	Connect to NVD data feed and download .xml.zip file
 * @warning	URL of NVD data feed should be checked first
 */
public class GetData {
	static ZipTagXml ztx = new ZipTagXml();
	
	/**
	 * @brief	Constructor
	 */
	private GetData() {
		
	}
	
	/**
	 * @brief	get .xml.zip file from nvd website
	 * @param	fpath
	 * 			type: String
	 * 			file path of .xml.zip file
	 * @throws	IOException
	 */
	public static void getData(String fpath) throws IOException{
		
		//try (InputStream in = new URL("https://nvd.nist.gov/feeds/xml/cve/1.2/"+fpath).openStream()) {
		try (InputStream in = new URL("https://nvd.nist.gov/feeds/json/cve/1.1/"+fpath).openStream()) {
		System.out.println("Loc:"+"./original/"+fpath);
	//InputStream in = new java.io.FileInputStream("./original/"+fpath
		//try {
			Files.copy(in, Paths.get("./" + ZipTagXml.original + File.separator+fpath), StandardCopyOption.REPLACE_EXISTING);
			
			String zipfilepath = "./" + ZipTagXml.original +File.separator+fpath;
			String jsonfilepath = "./" + ZipTagXml.nvdcve +File.separator+fpath;
			int json_length = jsonfilepath.length()-4;
			String dest = "./" + ZipTagXml.nvdcve;
			ztx.unzip(zipfilepath, dest);
			Logwriter.writeConsole("11"+jsonfilepath.substring(0, json_length));
			ztx.jsonTOXML(jsonfilepath.substring(0, json_length));
			Logwriter.writeConsole(" Success Getting "+ fpath+" from NVD\n");
		} catch (IOException e) {
			Logwriter.writeConsole(" Get "+ fpath+" from NVD failed\n"+e);
			throw e;
		}
	}
	
	//StAX version
	/**
	 * @brief	translate hierarchical model into relational model
	 * @param	fname
	 * 			type: String
	 * 			file name
	 * @return	true if translate is success. if not, false
	 */
	public static boolean makeTranslatedFile(String fname) {
		MakeDataStructure mds = new MakeDataStructure();
		
		boolean isBaseCreated = mds.saveDocument(fname, MakeDataStructure.baseAtt, MakeDataStructure.baseType);
		
		boolean isRefsCreated = mds.saveDocument(fname, MakeDataStructure.refsAtt, MakeDataStructure.refsType);
		
		boolean isVulnCreated = mds.saveDocument(fname, MakeDataStructure.vulnAtt, MakeDataStructure.vulnType);
		return isBaseCreated && isRefsCreated && isVulnCreated;
		
		
		
	}
}