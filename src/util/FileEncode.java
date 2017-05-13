import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FileEncode {
	public static String getFileEncode(String path) {
	    String charset ="asci";
	    byte[] first3Bytes = new byte[3];
	    BufferedInputStream bis = null;
	    try {
	      boolean checked = false;
	      bis = new BufferedInputStream(new FileInputStream(path));
	      bis.mark(0);
	      int read = bis.read(first3Bytes, 0, 3);
	      if (read == -1)
	        return charset;
	      if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
	        charset = "Unicode";//UTF-16LE
	        checked = true;
	      } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
	        charset = "Unicode";//UTF-16BE
	        checked = true;
	      } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
	        charset = "UTF8";
	        checked = true;
	      }
	      bis.reset();
	      if (!checked) {
	        int len = 0;
	        int loc = 0;
	        while ((read = bis.read()) != -1) {
	          loc++;
	          if (read >= 0xF0)
	            break;
	          if (0x80 <= read && read <= 0xBF) // Appear alone BF Following ， Also considered GBK
	            break;
	          if (0xC0 <= read && read <= 0xDF) {
	            read = bis.read();
	            if (0x80 <= read && read <= 0xBF) 
	            // Double byte  (0xC0 - 0xDF) (0x80 - 0xBF), May also be GB In coding 
	              continue;
	            else
	              break;
	          } else if (0xE0 <= read && read <= 0xEF) { // There may be an error ， But less likely 
	            read = bis.read();
	            if (0x80 <= read && read <= 0xBF) {
	              read = bis.read();
	              if (0x80 <= read && read <= 0xBF) {
	                charset = "UTF-8";
	                break;
	              } else
	                break;
	            } else
	              break;
	          }
	        }
	        //TextLogger.getLogger().info(loc + " " + Integer.toHexString(read));
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    } finally {
	      if (bis != null) {
	        try {
	          bis.close();
	        } catch (IOException ex) {
	        }
	      }
	    }
	    return charset;
	  }
	 
	  private static String getEncode(int flag1, int flag2, int flag3) {
	    String encode="";
	    // txt At the beginning of the file will be more than a few bytes ， Namely FF、FE（Unicode）,
	    // FE、FF（Unicode big endian）,EF、BB、BF（UTF-8）
	    if (flag1 == 255 && flag2 == 254) {
	      encode="Unicode";
	    }
	    else if (flag1 == 254 && flag2 == 255) {
	      encode="UTF-16";
	    }
	    else if (flag1 == 239 && flag2 == 187 && flag3 == 191) {
	      encode="UTF8";
	    }
	    else {
	      encode="asci";// ASCII code 
	    }
	    return encode;
	  }
}
