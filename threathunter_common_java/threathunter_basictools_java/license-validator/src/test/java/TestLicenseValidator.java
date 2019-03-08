import com.threathunter.basictools.licensevalidator.LicenseValidator;
import com.threathunter.config.CommonDynamicConfig;
import org.junit.Test;

/**
 * Created by daisy on 16-2-29.
 */
public class TestLicenseValidator {
    @Test
    public void testValidator() throws Exception {
        CommonDynamicConfig.getInstance().addOverrideProperty("keystore_password", "threathunter319");
        CommonDynamicConfig.getInstance().addOverrideProperty("cipher_param_password", "fdsf343fd5");
        LicenseValidator validator = new LicenseValidator("public.info", "nebulauser.lic");
        validator.verify();
        System.out.println(validator.getLicenseInfo());
    }
}
