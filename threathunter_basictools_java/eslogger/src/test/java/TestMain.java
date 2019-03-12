import com.threathunter.basictools.eslogger.EsloggerMain;
import org.junit.Test;

/**
 * created by www.threathunter.cn
 */
public class TestMain {
    @Test
    public void testMain() throws InterruptedException {
        String[] ss = {
                "/Users/daisy/Code_threathunter/java_fx/threathunter.basictools/eslogger/src/test/resources"
        };
        long current = System.currentTimeMillis();
        EsloggerMain.main(ss);
        while (System.currentTimeMillis() - current < 60 * 1000 * 60) {
            Thread.sleep(100);
        }
    }
}
