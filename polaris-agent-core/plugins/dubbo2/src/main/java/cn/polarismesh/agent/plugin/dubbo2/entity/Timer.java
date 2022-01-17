package cn.polarismesh.agent.plugin.dubbo2.entity;

/**
 * 计时器，用于记录服务请求时延数据
 */
public class Timer {
    private long startTimeMilli;
    private long endTimeMilli;
    private long delay;

    public Timer(long startTimeMilli) {
        this.startTimeMilli = startTimeMilli;
    }

    public void setEndTimeMilli(long endTimeMilli) {
        this.endTimeMilli = endTimeMilli;
        this.delay = this.endTimeMilli - this.startTimeMilli;
    }

    public long getStartTimeMilli() {
        return this.startTimeMilli;
    }

    public long getEndTimeMilli() {
        return this.endTimeMilli;
    }

    public long getDelay() {
        return this.delay;
    }
}
