package net.codejava.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.sql.*;
import com.asprise.ocr.Ocr;
import java.io.*;
/**
 * A servlet that takes message details from user and send it as a new e-mail
 * through an SMTP server. The e-mail message may contain attachments which
 * are the files uploaded from client.
 * 
 * @author www.codejava.net
 * 
 */
@WebServlet("/SendMailAttachServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, 	// 2MB
				maxFileSize = 1024 * 1024 * 10, 		// 10MB
				maxRequestSize = 1024 * 1024 * 50)		// 50MB
public class SendMailAttachServlet extends HttpServlet {
	private String host;
	private String port;
	private String user;
	private String pass;
boolean resu=true;
	public void init() {
		// reads SMTP server setting from web.xml file
		ServletContext context = getServletContext();
		host = context.getInitParameter("host");
		port = context.getInitParameter("port");
		user = context.getInitParameter("user");
		pass = context.getInitParameter("pass");
	}

	/**
	 * handles form submission
	 */
String resultMessage = "";
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		List<File> uploadedFiles = saveUploadedFiles(request);
		
		String recipient = request.getParameter("recipient");
		String subject = request.getParameter("subject");
		String content = request.getParameter("content");

		

		try {
			EmailUtility.sendEmailWithAttachment(host, port, user, pass,
					recipient, subject, content, uploadedFiles);
			
			resultMessage = "The e-mail was sent successfully";
		} catch (Exception ex) {
			ex.printStackTrace();
			resultMessage = "There were an error: " + ex.getMessage();
		} finally {
			deleteUploadFiles(uploadedFiles);
			request.setAttribute("message", resultMessage);
			getServletContext().getRequestDispatcher("/Result.jsp").forward(
					request, response);
		}
	}

	/**
	 * Saves files uploaded from the client and return a list of these files
	 * which will be attached to the e-mail message.
	 */
	private List<File> saveUploadedFiles(HttpServletRequest request)
			throws IllegalStateException, IOException, ServletException {
		List<File> listFiles = new ArrayList<File>();
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		Collection<Part> multiparts = request.getParts();
		if (multiparts.size() > 0) {
			for (Part part : request.getParts()) {
				// creates a file to be saved
				String fileName = extractFileName(part);
try{
	
String fnn="C:\\"+fileName;	
 resu=Spam(fnn);
			

	
		}catch(Exception e){System.out.println(e);}


				if (fileName == null || fileName.equals("")) {

					// not attachment part, continue
					continue;}

if(resu==true)

{
   resultMessage="Spam Detetected...Attachement failed";

}


else{

			File saveFile = new File(fileName);



				System.out.println("saveFile: " + saveFile.getAbsolutePath());


				FileOutputStream outputStream = new FileOutputStream(saveFile);
				
				// saves uploaded file
				InputStream inputStream = part.getInputStream();
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.close();
				inputStream.close();
				
				listFiles.add(saveFile);}

			}
		}
		return listFiles;
	}

	/**
	 * Retrieves file name of a upload part from its HTTP header
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return null;
	}

 
private boolean Spam(String fn) throws Exception

{


   Ocr.setUp(); // one time setup
Ocr ocr = new Ocr(); // create a new OCR engine
ocr.startEngine("eng", Ocr.SPEED_FASTEST); // English
String s = ocr.recognize(new File[] {new File(fn)}, Ocr.RECOGNIZE_TYPE_ALL, Ocr.OUTPUT_FORMAT_PLAINTEXT);
System.out.println("Result: " + s);

/*File file = new File("C:\\xampp\\tomcat\\webapps\\spam2\\WEB-INF\\classes\\net\\codejava\\mail\\test1.txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(s);*/


Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
Connection  con=DriverManager.getConnection("jdbc:odbc:banu");
Statement st=con.createStatement();
st.executeUpdate("insert into words values('"+s+"')");
boolean chk=chkSpam(s);

System.out.println(chk);
ocr.stopEngine();

 

return chk;
}

public  boolean chkSpam (String s)  { 

try{
Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
Connection  con=DriverManager.getConnection("jdbc:odbc:banu");
Statement st=con.createStatement();
 ResultSet rs=st.executeQuery("select * from spam");
String sp="";
while(rs.next())
{
 sp=rs.getString(1);
 if(s.contains(sp)){
           return true;
 }

      }
}catch(Exception e){System.out.println(e);}

return  false;
}

     
   
	
	/**
	 * Deletes all uploaded files, should be called after the e-mail was sent.
	 */
	private void deleteUploadFiles(List<File> listFiles) {
		if (listFiles != null && listFiles.size() > 0) {
			for (File aFile : listFiles) {
				aFile.delete();
			}
		}
	}
}