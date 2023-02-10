package hello.typeconverter.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class IpPort {

    private String Ip;
    private int port;

    public IpPort(String ip, int port) {
        Ip = ip;
        this.port = port;
    }
}
