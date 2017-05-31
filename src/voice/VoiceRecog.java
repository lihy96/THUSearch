//package voice;
//
//import java.util.ArrayList;
//
//import org.ansj.app.keyword.Keyword;
//import org.ansj.splitWord.GetWords;
//import org.apache.lucene.search.NRTManager.WaitingListener;
//import org.eclipse.jdt.internal.compiler.ast.MagicLiteral;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import com.iflytek.cloud.speech.RecognizerListener;
//import com.iflytek.cloud.speech.RecognizerResult;
//import com.iflytek.cloud.speech.SpeechConstant;
//import com.iflytek.cloud.speech.SpeechError;
//import com.iflytek.cloud.speech.SpeechRecognizer;
//import com.iflytek.cloud.speech.SpeechUtility;
//import com.sun.javafx.collections.MappingChange.Map;
//
//public class VoiceRecog {
//
//	private String keyword = "";
//	private Message msg;
//	
//	public Object test;
//	
//	public VoiceRecog(Message m) {
//		msg = m;
////		msg = new Message();
////		pause();
//	}
//	
////	public synchronized void pause() {
////		if (test != null) {
////			try {
////				test.wait();
////			} catch (InterruptedException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////		}
////	}
//	
////	public String getKeyword() {
//////		return keyword;
////		return msg.getMsg();
////	}
//	
//	public void after_ok() {
//		System.out.println("keyword: " + keyword);
////		if (test != null) {
////			test.notify();
////		}
//		synchronized (msg) {
//			msg.setMsg(keyword);
//			msg.notify();
//		}
//	}
//		
//	
//	//听写监听器
//	private RecognizerListener mRecoListener = new RecognizerListener(){
//		//听写结果回调接口(返回Json格式结果，用户可参见附录)；
//		//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
//		//关于解析Json的代码可参见MscDemo中JsonParser类；
//		//isLast等于true时会话结束。
//		public void onResult(RecognizerResult results, boolean isLast) {
//			System.out.println("Result:"+results.getResultString ());
//			String json = results.getResultString();
//			JSONObject json_obj = new JSONObject(json);
//			boolean ls = json_obj.getBoolean("ls");
//			if (ls) {
//				after_ok();
////				keyword = "";
//			}
//			JSONArray words = json_obj.getJSONArray("ws");
//			boolean said_search = false;
////			System.out.println("words len " + words.length());
//			for (int i = 0; i < words.length(); i++) {  
//				
////				System.out.println("i " + i );
//				JSONArray cw = words.getJSONObject(i).getJSONArray("cw");  
//				String w = cw.getJSONObject(0).getString("w");
////				System.out.println("w is " + w);
////				if (w.equals("搜索"))
////					said_search = true;
////				if (said_search == false)
////					continue;
//				keyword += w;
//			} 	
//		}
//		
//		//会话发生错误回调接口
//		public void onError(SpeechError error) {
//			error.getErrorDescription(true); //获取错误码描述
//		}
//		//开始录音
//		public void onBeginOfSpeech() {}
//		//音量值0~30
//		public void onVolumeChanged(int volume){}
//		//结束录音
//		public void onEndOfSpeech() {
//			
//		}
//		//扩展用接口
//		public void onEvent(int eventType,int arg1,int arg2,String msg) {}
//		
//	};
//	
//	public void init() {
//		//1.创建SpeechRecognizer对象
//		SpeechRecognizer mIat= SpeechRecognizer.createRecognizer( );
//		//2.设置听写参数，详见《 MSC Reference Manual》 SpeechConstant类
//		mIat.setParameter(SpeechConstant.DOMAIN, "iat");
//		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//		mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
//		//3.开始听写
//		mIat.startListening(mRecoListener);
//	}
//
//}
