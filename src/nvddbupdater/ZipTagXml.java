/**
 /**
 * @file	ZipTagXml.java
 * @date	2018/11/02
 * @author	skt.1519040
 * @brief	This file defines file path, DB server information and methods affect local storage
 * @date
 * 	- 2018. 11. 02	Joh Rang Hyun
 * 	- 2018.	12.	14	Joh Rang Hyun	StAX version
 */
package nvddbupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @class	ZipTagXml
 * @brief	The fields of this class define file path and DB server information.
 * @warning	Check the file path and DB server information are right.
 */
public class ZipTagXml {
	
		/// file path (directory)
		static String original = "original";
		static String nvdcve = "nvdcve";
		static String translated = "translated";
		static String log = "log";
		
	 
		/// DB server information
		static String host = "172.27.12.7";
		//static String host = "172.27.12.11";
		//static String host = "localhost";
		static String port = "3306";             
		static String dbId = "thub";       
		static String dbPw = "Thub.2545!";
		//static String dbPw = "Thub.8729!";
		//static String dbId = "root";       
		//static String dbPw = "root";

		
		String lineSeparator = System.getProperty("line.separator");
		/**
		 * @brief	check the necessary directory
		 * @return	ture if all directory exist
		 */
		boolean dirExist() {
			File nvdcveDir = new File("./"+ZipTagXml.nvdcve);
			File originalDir = new File("./"+ZipTagXml.original);
			File translatedDir =new File("./"+ZipTagXml.translated);
			return nvdcveDir.exists() && originalDir.exists() && translatedDir.exists();
		}
		
		/**
		 * @brief	make a directory
		 * @param	fpath
		 * 			type: String
		 * 			path of directory
		 */
		void makeOneDir(String fpath) {
			File oneDirectory = new File(fpath);
			Logwriter.writeConsole(" ");
			if(!oneDirectory.exists()){
				if(oneDirectory.mkdir()) {
					Logwriter.writeConsole(" "+fpath+" is successfully created.\n");
				}
				else {
					Logwriter.writeConsole("Fail to create "+fpath+"\n");
					System.exit(1);
				}
			}
			else{
				Logwriter.writeConsole(fpath+" is already created.\n");
			}
			
		}
		
		/**
		 * @brief	Make all necessary directories
		 */
		void makeDir () {
			makeOneDir("./"+ZipTagXml.nvdcve);
			makeOneDir("./"+ZipTagXml.original);
			makeOneDir("./"+ZipTagXml.translated);
			makeOneDir("./"+ZipTagXml.log);
		}
		
		/**
		 * @brief	unzip a .zip file
		 * @param	zipFilePath
		 * 			type: String
		 * 			file path of .zip file
		 * @param	destDir
		 * 			type: String
		 * 			destination file path of unzip file
		 * @throws	IOException
		 */
		void unzip(String zipFilePath, String destDir) throws IOException {
	        File targetDir = new File(destDir);
	        // create output directory if it doesn't exist
	        if(!targetDir.exists()) {
	        	if(targetDir.mkdirs()) {
	        		Logwriter.writeConsole(" "+destDir + " is successfully created.\n");
	        	}
	        	else {
	        		Logwriter.writeConsole(" Fail to create " + destDir+"\n");
	        		System.exit(1);
	        	}
	        }
	        
	        //buffer for read and write data to file
	       
	        
	            
	        try (ZipInputStream zipFiles = new ZipInputStream(new FileInputStream(zipFilePath))){
	        	
	            ZipEntry entries = zipFiles.getNextEntry();
	            while(entries != null){
	            	unzipEntries(zipFiles, entries, destDir);
	                //close this ZipEntry
	                zipFiles.closeEntry();
	                entries = zipFiles.getNextEntry();
	            } 
	            zipFiles.closeEntry();
            } catch (Exception e) {
            	Logwriter.writeConsole(" "+zipFilePath + "unzip failed\n");
            } 
	            //close last ZipEntry
        } 
		
		void jsonTOXML(String jsonfilepath) throws IOException {
			
			JSONParser parser = new JSONParser();
			
			Logwriter.writeConsole("222"+jsonfilepath + ":\n");
			
			Object obj;
			
			
			// xml 생성 내용 저장 객체
			StringBuilder tempString = new StringBuilder();
			
			tempString.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"+lineSeparator);
			tempString.append("<nvd>"+lineSeparator);

			
			
			try {
				obj = parser.parse(new FileReader(jsonfilepath));
				
				JSONObject jsonObject = (JSONObject) obj;
				//System.out.println("First :: " +jsonObject.toString());
				String type = (String) jsonObject.get("CVE_data_type"); 
				System.out.println("CVE_data_type :: " +type);
				
				
				//Map<String, Object> map = new HashMap<String, Object>(); 
				
				JSONArray jsonCVE = (JSONArray) jsonObject.get("CVE_Items"); 
				System.out.println("Size:"+jsonCVE.size());
				
				for(int j = 0; j < jsonCVE.size(); j++) {
					JSONObject json = (JSONObject) jsonCVE.get(j);
					//System.out.println( j+" Object:"+json.toString());
			
					JSONObject cve = (JSONObject) json.get("cve");
					JSONObject CVE_data_meta = (JSONObject) cve.get("CVE_data_meta");
					JSONObject impact = (JSONObject) json.get("impact");
					
					JSONObject description = (JSONObject) cve.get("description");
					JSONArray description_data = (JSONArray) description.get("description_data");
					JSONObject description_array = (JSONObject) description_data.get(0);
					//System.out.println("description_data Size:"+description_data.size());
					
//ref					
					JSONObject references = (JSONObject) cve.get("references");
					JSONArray reference_data = (JSONArray) references.get("reference_data");
//vuln					
					JSONObject configurations = (JSONObject) json.get("configurations");
					JSONArray nodes = (JSONArray) configurations.get("nodes");


					String name = (String) CVE_data_meta.get("ID");
					String published = (String) json.get("publishedDate");
					String modified = (String) json.get("lastModifiedDate");

					
					if (nodes.size() != 0) {
						JSONObject baseMetricV2 = (JSONObject) impact.get("baseMetricV2");
						JSONObject cvssV2 = (JSONObject) baseMetricV2.get("cvssV2");
							
						String severity = (String) baseMetricV2.get("severity");
						String CVSS_version = (String) cvssV2.get("version");
						Double CVSS_score = (Double) cvssV2.get("baseScore");
						Double CVSS_impact_subscore = (Double) baseMetricV2.get("impactScore");
						Double CVSS_exploit_subscore = (Double) baseMetricV2.get("exploitabilityScore");
						String CVSS_vector = (String) cvssV2.get("vectorString");
						
						tempString.append("<entry type=\""+type+"\" name=\""+name+"\" seq=\""+name.substring(4,name.length())
						+"\" published=\""+ published.substring(0,10)+"\" modified=\""+modified.substring(0,10)
						+"\" severity=\""+severity
						+"\" CVSS_version=\""+CVSS_version
						+"\" CVSS_score=\""+CVSS_score.toString()
						+"\" CVSS_base_score=\""+CVSS_score.toString()
						+"\" CVSS_impact_subscore=\""+CVSS_impact_subscore.toString()
						+"\" CVSS_exploit_subscore=\""+CVSS_exploit_subscore.toString()
						+"\" CVSS_vector=\""+CVSS_vector+"\">"
						+lineSeparator);

					} else {
						/*
						tempString.append("<entry type=\""+type+"\" name=\""+name+"\" seq=\""+name.substring(4,name.length())
						+"\" published=\""+ published.substring(0,10)+"\" modified=\""+modified.substring(0,10)
						+"\" s>"
						+lineSeparator);
						*/
					}
					
					String description_value = (String) description_array.get("value");
			
					/*
					System.out.println("cve :: " +cve.toString());
					System.out.println("CVE_data_meta :: " +CVE_data_meta.toString());
					System.out.println("name :: " +name);
					System.out.println("published :: " +published);
					System.out.println("modified :: " +modified);
					System.out.println("severity :: " +severity);
					System.out.println("CVSS_version :: " +CVSS_version);
					System.out.println("CVSS_score :: " +CVSS_score.toString());
					System.out.println("CVSS_impact_subscore :: " +CVSS_impact_subscore.toString());
					System.out.println("CVSS_exploit_subscore :: " +CVSS_exploit_subscore.toString());
					System.out.println("CVSS_vector :: " +CVSS_vector);
		
					System.out.println("description_value :: " +description_value);
		*/	
					if (nodes.size() != 0) {
						description_value = description_value.replaceAll("<!--", "");
						description_value = description_value.replaceAll("&", "&amp;");
						description_value = description_value.replaceAll("<", "&lt;");
						description_value = description_value.replaceAll(">", "&gt;");
						
						tempString.append("<desc>"+lineSeparator);
						tempString.append("<descript source=\"cve\">"+description_value);
						tempString.append("</descript>"+lineSeparator);
						tempString.append("</desc>"+lineSeparator);
					}
					
					if (reference_data.size() != 0 ) {
						tempString.append("<refs>"+lineSeparator);
						for(int k=0; k < reference_data.size();k++) {
							JSONObject ref = (JSONObject) reference_data.get(k);
							
							String url = (String) ref.get("url");
							String ref_name = (String) ref.get("name");
							String refsource = (String) ref.get("refsource");
							
							url = url.replaceAll("&", "&amp;");
							url = url.replaceAll("<", "&lt;");
							url = url.replaceAll(">", "&gt;");
							ref_name = ref_name.replaceAll("&", "&amp;");
							ref_name = ref_name.replaceAll("<", "&lt;");
							ref_name = ref_name.replaceAll(">", "&gt;");							
							
							
							tempString.append("<ref source=\""+refsource+"\" url=\""+url+"\" >"+ref_name+"</ref>"+lineSeparator);
							
						}
						tempString.append("</refs>"+lineSeparator);
					}
				
				
					if (nodes.size() != 0 ) {
						tempString.append("<vuln_soft>"+lineSeparator);
						for(int i=0; i < nodes.size();i++) {
							JSONObject node = (JSONObject) nodes.get(i);
						
							JSONArray childrens = (JSONArray) node.get("children");
							
							String c_prodname = null;
							String c_vendor = null;

							
							int chk = 0;
							int vulnerable_chk = 0;
							
							if (childrens != null) {
								for(int l=0; l < childrens.size();l++) {
									JSONObject children = (JSONObject) childrens.get(l);
						
									JSONArray ch_cpe_matchs = (JSONArray) children.get("cpe_match");
									for(int k=0; k < ch_cpe_matchs.size();k++) {
										JSONObject ch_cpe_match = (JSONObject) ch_cpe_matchs.get(k);
									
										Boolean vulnerable = (Boolean) ch_cpe_match.get("vulnerable");
										
										if(vulnerable) {
											String cpe23Uri = (String) ch_cpe_match.get("cpe23Uri");
											//System.out.println("ch|"+vulnerable+"cpe23Uri:"+cpe23Uri);
											
											String[] vulnStr = cpe23Uri.split(":");
											String prodname = vulnStr[4];
											String vendor = vulnStr[3];
											String version = vulnStr[5];
											String edition = vulnStr[6];
											
											prodname = prodname.replaceAll("&", "&amp;");
											prodname = prodname.replaceAll("\"", "&quot;");
											
											vendor = vendor.replaceAll("&", "&amp;");
											version = version.replaceAll("&", "&amp;");
							
											//System.out.println("ch|prodname="+prodname+"|vendor="+vendor);
											//System.out.println("ch|version="+version+"|edition="+edition);
											
											if (!((prodname.equals(c_prodname)) && vendor.equals(c_vendor))) {
												if(chk!=0) tempString.append("</prod>"+lineSeparator);
												tempString.append("<prod name=\""+prodname+"\" vendor=\""+vendor+"\">"+lineSeparator);
											}
											
											if (edition.equals("*"))
												tempString.append("<vers num=\""+version+"\" />"+lineSeparator);
											else
												tempString.append("<vers num=\""+version+"\" edition=\""+edition+"\"/>"+lineSeparator);
											// 다음 분기 떄 비교용
											c_prodname = prodname;
											c_vendor = vendor;

											chk++;
											vulnerable_chk++;
										}										
									}									
								}
								
							} else {
								JSONArray cpe_matchs = (JSONArray) node.get("cpe_match");
																
								
								for(int k=0; k < cpe_matchs.size();k++) {
									JSONObject cpe_match = (JSONObject) cpe_matchs.get(k);
								
									Boolean vulnerable = (Boolean) cpe_match.get("vulnerable");
									if(vulnerable) {
										String cpe23Uri = (String) cpe_match.get("cpe23Uri");
										//System.out.println(vulnerable+"cpe23Uri:"+cpe23Uri);
										
										String[] vulnStr = cpe23Uri.split(":");
										String prodname = vulnStr[4];
										String vendor = vulnStr[3];
										String version = vulnStr[5];
										String edition = vulnStr[6];
										
										prodname = prodname.replaceAll("&", "&amp;");
										prodname = prodname.replaceAll("\"", "&quot;");
										
										vendor = vendor.replaceAll("&", "");
										version = version.replaceAll("&", "&amp;");
						
									//	System.out.println("prodname="+prodname+"|vendor="+vendor);
									//	System.out.println("version="+version+"|edition="+edition);
									//	tempString.append("<prod name=\""+prodname+"\" vendor=\\"+vendor+"\"/>"+lineSeparator);
									//	tempString.append("<vers num=\""+version+"\" edition=\\"+edition+"\"/>"+lineSeparator);
										
										if (!((prodname.equals(c_prodname)) && vendor.equals(c_vendor))) {
											if(chk!=0) tempString.append("</prod>"+lineSeparator);
											tempString.append("<prod name=\""+prodname+"\" vendor=\""+vendor+"\">"+lineSeparator);
										}
										
										if (edition.equals("*"))
											tempString.append("<vers num=\""+version+"\" />"+lineSeparator);
										else
											tempString.append("<vers num=\""+version+"\" edition=\""+edition+"\"/>"+lineSeparator);
								
										// 다음 분기 떄 비교용
										c_prodname = prodname;
										c_vendor = vendor;
									
										chk++;
										vulnerable_chk++;
									}								
								}																
							}
							if(vulnerable_chk !=0)
								tempString.append("</prod>"+lineSeparator);							
						}
						tempString.append("</vuln_soft>"+lineSeparator);
					}					
					if(nodes.size() != 0)
							tempString.append("</entry>"+lineSeparator);
				}
				tempString.append("</nvd>");
				
		// XNK  생성 부분
				String filename = null;
				if (jsonfilepath.length() > 32)
					filename = jsonfilepath.substring(0,16) + jsonfilepath.substring(20,28) + ".xml";
				else
					filename = jsonfilepath.substring(0,16) + jsonfilepath.substring(20,24) + ".xml";
				File file = new File(filename);
		//		System.out.println("jsonfilepath"+jsonfilepath+jsonfilepath.length());
	//			System.out.println("filename"+filename);
				FileWriter fw = null;
				try {
					fw = new FileWriter(file);
					fw.write(tempString.toString());
				}catch(IOException e) {
					e.printStackTrace();
				}finally {
					if(fw != null) try {fw.close();}catch(IOException e){}
				}
				
				//new FileWriter(jsonfilepath
				     
				/*
				Iterator<String> iterator = jsonCVE.iterator(); 
				while (iterator.hasNext()) { 
					
					Object value = (String)jsonCVE.get("cve"); 
					
					System.out.println(key + "ksangki:"+value);
						
					
					//Logwriter.writeConsole("jsonCVE :: " + iterator.next());
				}
			
				
				
				JSONArray jsonObject2 = (JSONArray) jsonObject.get("CVE_Items");
				
				Iterator<String> keysItr = jsonObject2.keySet().iterator(); 
				while (keysItr.hasNext()) 
				{ 
					String key = keysItr.next(); 
					Object value = jsonObject2.get(key); 
					
					System.out.println(key + "ksangki:"+value);
				}
				*/
					
					
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				Logwriter.writeConsole(" "+jsonfilepath + ":   jsonTOXML failed\n");
				e1.printStackTrace();
			}
		     
		}
		
		
		/**
		 * @brief	unzip each entry of .zip file
		 * @param	zipFiles
		 * 			type: ZipInputStream
		 * 			.zip file data
		 * @param	entries
		 * 			type: ZipEntry
		 * 			entry of Zip file that will be unzipped
		 * @param	destDir
		 * 			type: String
		 * 			destination directory of unzipped file
		 */
		void unzipEntries (ZipInputStream zipFiles, ZipEntry entries, String destDir) {
			String fileName = entries.getName();
	        File newFile = new File(destDir + File.separator + fileName);
	        byte[] buffer = new byte[1024];
	         //create directories for sub directories in zip
	        new File(newFile.getParent()).mkdirs();
	        try (FileOutputStream outputStream = new FileOutputStream(newFile)){
	            
	            int len;
	            while ((len = zipFiles.read(buffer)) > 0) {
	            	outputStream.write(buffer, 0, len);
	            }
	        } catch (Exception e) {
	        	Logwriter.writeConsole(" FileOutputStream error\n");
	        } 
	        
	        //close this ZipEntry
	        
		}
}