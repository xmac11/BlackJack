package testing;

import org.junit.jupiter.api.Test;
import java.net.InetAddress;
import static junit.framework.TestCase.assertTrue;

public class ServerOffTesting {

    @Test
    void test1() throws Exception { // Checks if the ip of the server is reachable
        InetAddress server = InetAddress.getByName("147.188.201.15");
        assertTrue(server.isReachable(5000));
    }
}
