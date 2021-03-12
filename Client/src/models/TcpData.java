package models;

import java.io.Serializable;

public class TcpData implements Serializable {
    public String cmd;
    public Object data;

    public TcpData(String cmd, Object data) {
        this.cmd = cmd;
        this.data = data;
    }
}
