import com.threathunter.geo.GeoUtil;
import org.junit.Test;

/**
 * Created by daisy on 2015/11/12.
 */
public class TestPhonecity {
    @Test
    public void testPhone() {
        long time = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            GeoUtil.getPhoneLocation("18801785463");
        }
        System.out.println(System.currentTimeMillis() - time);

        long time2 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            GeoUtil.getCNPhoneCity("18801785463");
        }
        System.out.println(System.currentTimeMillis() - time2);

        long time3 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            GeoUtil.getPhoneLocation("18801785463");
        }
        System.out.println(System.currentTimeMillis() - time3);
    }


}
