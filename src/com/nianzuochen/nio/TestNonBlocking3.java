package com.nianzuochen.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

// 管道 pipe
public class TestNonBlocking3 {
    @Test
    public void test1() {
        Pipe pipe = null;
        Pipe.SinkChannel sinkChannel = null;
        Pipe.SourceChannel sourceChannel = null;
        try {
            // 1. 获取管道
            pipe = Pipe.open();

            // 可以将发送和接收放在两个线程中
            // 2. 缓冲区的数据写入
            ByteBuffer buf = ByteBuffer.allocate(1024);
            sinkChannel = pipe.sink();
            buf.put("通过单向管道发送数据".getBytes());
            buf.flip();
            sinkChannel.write(buf);

            //3. 读取缓冲区中的数据
            sourceChannel = pipe.source();
            sourceChannel.read(buf);
            buf.flip();
            int len = sourceChannel.read(buf);
            System.out.println(new String(buf.array(), 0, len));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (sourceChannel != null) {
                try {
                    sourceChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (sinkChannel != null) {
                try {
                    sinkChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
