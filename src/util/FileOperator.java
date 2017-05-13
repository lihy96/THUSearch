import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileOperator {
	/**
	   *  Get file contents by path ， This method uses a string as a carrier ， In order to read the file correctly （ AuvPlayer ）， Text file can only be read ， Security method ！
	   */
	  public static String readFile(String path){
	    String data = null;
	    //  To determine whether the file exists 
	    File file = new File(path);
	    if(!file.exists()){
	    	System.out.println(file.toString());
	      return data;
	    }
	    //  Get file encoding format 
	    String code = FileEncode.getFileEncode(path);
	    InputStreamReader isr = null;
	    try{
	      //  Parse the file according to the encoding format 
	      if("asci".equals(code)){
	        //  Here used GBK Code ， Environment code format ， Because the default encoding is not equal to the operating system  
	        // code = System.getProperty("file.encoding");
	        code = "GBK";
	      }
	      isr = new InputStreamReader(new FileInputStream(file),code);
	      //  Read the contents of the file 
	      int length = -1 ;
	      char[] buffer = new char[1024];
	      StringBuffer sb = new StringBuffer();
	      while((length = isr.read(buffer, 0, 1024) ) != -1){
	        sb.append(buffer,0,length);
	      }
	      data = new String(sb);
	    }catch(Exception e){
	      e.printStackTrace();
	      // log.info("getFile IO Exception:"+e.getMessage());
	    }finally{
	      try {
	        if(isr != null){
	          isr.close();
	        }
	      } catch (IOException e) {
	        e.printStackTrace();
	        // log.info("getFile IO Exception:"+e.getMessage());
	      }
	    }
	    return data;
	  }
	  
	  /**
	   *  Save the contents of the file in the specified path and encoding format ， This method uses a string as a carrier ， In order to write to the file （ AuvPlayer ）， Can only be written to text content ， Security method 
	   * 
	   * @param data
	   *      Byte data to be written to file 
	   * @param path
	   *      File path , Include file name 
	   * @return boolean 
	   *       Returns when the write is completed. true;
	   */
	  public static boolean writeFile(byte data[], String path , String code){
	    boolean flag = true;
	    OutputStreamWriter osw = null;
	    try{
	      File file = new File(path);
	      if(!file.exists()){
	        file = new File(file.getParent());
	        if(!file.exists()){
	          file.mkdirs();
	        }
	      }
	      if("asci".equals(code)){
	        code = "GBK";
	      }
	      osw = new OutputStreamWriter(new FileOutputStream(path),code);
	      osw.write(new String(data,code));
	      osw.flush();
	    }catch(Exception e){
	      e.printStackTrace();
	      // log.info("toFile IO Exception:"+e.getMessage());
	      flag = false;
	    }finally{
	      try{
	        if(osw != null){
	          osw.close();
	        }
	      }catch(IOException e){
	        e.printStackTrace();
	        // log.info("toFile IO Exception:"+e.getMessage());
	        flag = false;
	      }
	    }
	    return flag;
	  }
	  
	  /**
	   *  To read a file from a specified path into a byte array , This method can be used for some non text format 
	   *      457364578634785634534
	   * @param path
	   *      File path , Include file name 
	   * @return byte[]
	   *        File byte array 
	   *      
	   */
	  public static byte[] getFile(String path) throws IOException {
	    FileInputStream stream=new FileInputStream(path);
	    int size=stream.available();
	    byte data[]=new byte[size];
	    stream.read(data);
	    stream.close();
	    stream=null;
	    return data;
	  }
	 
	 
	 
	  /**
	   *  Write byte content to the corresponding file ， This method can be used for some non text files 。
	   * @param data
	   *       Byte data to be written to file 
	   * @param path
	   *       File path , Include file name 
	   * @return boolean isOK  Returns when the write is completed. true;
	   * @throws Exception
	   */
	  public static boolean toFile(byte data[], String path) throws Exception {
	    FileOutputStream out=new FileOutputStream(path);
	    out.write(data);
	    out.flush();
	    out.close();
	    out=null;
	    return true;
	  }
}
