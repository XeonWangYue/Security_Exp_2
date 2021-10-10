package top.xeonwang;

import io.netty.channel.Channel;

/**
 * @author Chen Q.
 */

public class KDCExecutor implements Executor {
    private static volatile KDCExecutor kdcExecutor;
    public static KDCExecutor getInstance(){
        if(kdcExecutor==null){
            synchronized (KDCExecutor.class){
                if(kdcExecutor==null){
                    kdcExecutor = new KDCExecutor();
                }
            }
        }
        return kdcExecutor;
    }

    private KDCExecutor(){

    }
    private volatile int nowstep = 0;
    private Long uidA = null;
    private Long uidB = null;
    private Channel server = null;
    private Channel dist = null;
    private byte[] ks = null;
    private byte[] ka = null;
    private Long N1 = null;
    private Long N2 = null;
    private byte[] content = null;

    public static synchronized void reset(){
        if(kdcExecutor!=null){
            synchronized (KDCExecutor.class){
                if(kdcExecutor!=null){
                    kdcExecutor = new KDCExecutor();
                }
            }
        }
    }

    @Override
    public synchronized boolean execute(int step) {
        if (step != this.nowstep + 1) {
            System.out.println("执行顺序有误");
            return false;
        } else {
            this.nowstep = step;
        }
        switch (nowstep) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            default:
                break;
        }
        return false;
    }
    private void getKs() {
        if (server == null || ka == null) {
            System.out.println("ks获取失败，服务器通信未建立。");
            return;
        }
        MsgProtocol msg = new MsgProtocol();
        msg.setStep(1);
    }

    private void sendKs() {

    }

    private void sendfn2() {

    }

    private void sendContent() {

    }

    public int getNowstep() {
        return nowstep;
    }

    public void setNowstep(int nowstep) {
        this.nowstep = nowstep;
    }

    public Long getUidA() {
        return uidA;
    }

    public void setUidA(Long uidA) {
        this.uidA = uidA;
    }

    public Long getUidB() {
        return uidB;
    }

    public void setUidB(Long uidB) {
        this.uidB = uidB;
    }

    public Channel getServer() {
        return server;
    }

    public void setServer(Channel server) {
        this.server = server;
    }

    public Channel getDist() {
        return dist;
    }

    public void setDist(Channel dist) {
        this.dist = dist;
    }

    public void setKs(byte[] ks) {
        this.ks = ks;
    }

    public byte[] getKa() {
        return ka;
    }

    public void setKa(byte[] ka) {
        this.ka = ka;
    }

    public Long getN1() {
        return N1;
    }

    public void setN1(Long n1) {
        N1 = n1;
    }

    public Long getN2() {
        return N2;
    }

    public void setN2(Long n2) {
        N2 = n2;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
