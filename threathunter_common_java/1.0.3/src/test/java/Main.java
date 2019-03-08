import com.threathunter.common.Identifier;
import org.junit.Test;

/**
 * Created by daisy on 2015/6/30.
 */
public class Main {
    @Test
    public void testUnfolderName() {
        Identifier identifier = Identifier.fromKeys("key1", "key2");
        System.out.println(identifier.getUnfoldedName());
    }
}
