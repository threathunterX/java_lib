import com.threathunter.common.Identifier;
import org.junit.Test;

/**
 * created by www.threathunter.cn
 */
public class Main {
    @Test
    public void testUnfolderName() {
        Identifier identifier = Identifier.fromKeys("key1", "key2");
        System.out.println(identifier.getUnfoldedName());
    }
}
