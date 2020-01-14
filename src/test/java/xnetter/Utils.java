package xnetter;

import xnetter.http.core.HttpConf;
import xnetter.http.test.Main;
import xnetter.sock.core.Manager;

public final class Utils {
    private Utils() {

    }

    public static HttpConf makeConf(int port) {
        String keyFile = Utils.class.getResource("/netter.keystore").getPath();
        return new HttpConf(port, true, keyFile,
                "654321", "123456");
    }

    public  static void supportSsl(Manager.Conf conf) {
        String keyFile = Utils.class.getResource("/netter.keystore").getPath();
        conf.supportSsl(keyFile, "654321", "123456");
    }
}
