package voice;

import com.iflytek.cloud.speech.SpeechConstant;

//import org.apache.lucene.search.NRTManagerReopenThread;
//import org.apache.tomcat.jni.Thread;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechUtility;

public class Test{
	private Message msg;

	public Test(Message m) {
		msg = m;
	}
	
	public Test() {
		msg = new Message();
		SpeechUtility.createUtility( SpeechConstant.APPID +"= 59291551 ");

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				VoiceRecog vr = new VoiceRecog(msg); 
				
				vr.init();
			}
		}).start();
		
		doTest();
	}
	
	public static void main(String[] args) {
		Test test = new Test();
	}
	
	public void doTest() {
		synchronized (msg) {	
			try {
				msg.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String res = msg.getMsg();
			System.out.println("laobi de string : " + res);
		}
	}
	
	public void getKeyword() {
	}
}
