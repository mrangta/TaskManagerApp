
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.remote.DesiredCapabilities;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;

public class AutomationTest_g22 {

	static DesiredCapabilities dc = new DesiredCapabilities();
	protected static AppiumDriver <MobileElement> driver = null;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		dc.setCapability( "deviceName" , "MY PHONE");
		dc.setCapability( "udid", getUDID()); //Give Device ID of your mobile phone
		
		dc.setCapability("unicodeKeyboard", true); 
		dc.setCapability("resetKeyboard", true);		
		
		dc.setCapability( "platformName" , "Android" );
		dc.setCapability( "platformVersion", "8.0" );
		
		dc.setCapability( "appPackage", "com.mcc.g22" );
		dc.setCapability( "appActivity", ".HomeActivity" );
		
		dc.setCapability( "noReset", "true" );
				
		//Instantiate Appium Driver
		
		try {
				
			  driver = new AndroidDriver <MobileElement> (new URL( "http://0.0.0.0:4723/wd/hub" ), dc );
			
		} catch ( MalformedURLException e) {
			
			System.out.println( e.getMessage() );
		}
				
		//Added 4 seconds wait so that the app loads completely 
		try {
			Thread.sleep( 4000 );
			
		} catch ( InterruptedException e ) {
			
			e.printStackTrace();
		}
		
		Thread.sleep( 1000 );
		
		
	
	}
	private static void checkLogin(AppiumDriver<MobileElement> driver2) throws InterruptedException {

				
		
}
	
	private static String getUDID() throws IOException {

		Process p = Runtime.getRuntime().exec("adb devices");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		String s = stdInput.readLine();
		s = stdInput.readLine();
		System.out.println(s);
	
		return(s.split("\t")[0]);
		    
	 }

}
