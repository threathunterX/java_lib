import com.threathunter.basictools.licensecreator.LicenseCreator;
import com.threathunter.config.CommonDynamicConfig;
import org.junit.Test;

import java.text.ParseException;

/**
 * Created by daisy on 16-2-27.
 */
public class TestLicenseCreator {
    @Test
    public void testCreation() throws ParseException {
        CommonDynamicConfig.getInstance().addOverrideProperty("expire_date", "2017.01.05-11:50:00");
        LicenseCreator.main(null);
    }
}
