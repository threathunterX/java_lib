import com.threathunter.common.Identifier;
import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class Main {
    @Test
    public void testUnfolderName() {
        Identifier identifier = Identifier.fromKeys("key1", "key2");
        System.out.println(identifier.getUnfoldedName());
    }

    @Test
    public void testReflect() throws IllegalAccessException, NoSuchFieldException {
        String s1 = "hi";
        String s2 = new String("hi");

        System.out.println("s1.value: " + showInternalCharArrayHashCode(s1));
        System.out.println("s2.value: " + showInternalCharArrayHashCode(s2));

        System.out.println("s1: " + System.identityHashCode(s1));
        System.out.println("s2: " + System.identityHashCode(s2));

        System.out.println("s1: " + s1.hashCode());
        System.out.println("s2: " + s2.hashCode());
    }

    @Test
    public void testGson() throws IOException, NoSuchFieldException, IllegalAccessException {
        Gson gson = new Gson();
        String content = "{\"key\": \"127.0.0.1\"}";

        System.out.println("data1");
        Map<String, String> data1 = gson.fromJson(content, Map.class);
        System.out.println(System.identityHashCode(data1.get("key")));
        System.out.println(showInternalCharArrayHashCode(data1.get("key")));
        System.out.println(data1.get("key").hashCode());

        System.out.println("data2");
        Map<String, String> data2 = gson.fromJson(content, Map.class);
        System.out.println(System.identityHashCode(data2.get("key")));
        System.out.println(showInternalCharArrayHashCode(data2.get("key")));
        System.out.println(data2.get("key").hashCode());
    }

    @Test
    public void testFormat() throws NoSuchFieldException, IllegalAccessException {
//        String s1 = String.format("%s@@%s", "app", "name");
//        String s2 = String.format("%s@@%s", "app", "name").intern();
//        String s1 = "app@@name";
//        String s2 = String.format("%s@@%s", "app", "name").intern();

//        showData(s1);
//        showData(s2);

        String one = "abc";
        System.out.println(one);
        showData(one);
        String two = "abc".substring(1);
        System.out.println(two);
        showData(two);

    }

    private static void showData(String s) throws NoSuchFieldException, IllegalAccessException {
//        System.out.println("identity");
        System.out.println(System.identityHashCode(s));
//        System.out.println("internal arrays");
        System.out.println(showInternalCharArrayHashCode(s));
    }

    private static int showInternalCharArrayHashCode(String s) throws IllegalAccessException, NoSuchFieldException {
        final Field value = String.class.getDeclaredField("value");
        value.setAccessible(true);
        return value.get(s).hashCode();
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();
        while (!s.equals("exit")) {
            try {
                System.out.println(System.identityHashCode(s));
                System.out.println(showInternalCharArrayHashCode(s));
                s = br.readLine().intern();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
