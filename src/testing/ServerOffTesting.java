package testing;

import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import static junit.framework.TestCase.assertTrue;

public class ServerOffTesting {

    @Test
    void test1() throws Exception { // Checks if the ip of the server is reachable
        InetAddress server = InetAddress.getByName("localhost");
        assertTrue(server.isReachable(5000));
    }
}
