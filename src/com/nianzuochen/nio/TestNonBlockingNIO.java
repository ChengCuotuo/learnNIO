package com.nianzuochen.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

// 想要 IDEA 接收控制台的输入 需要在 IDEA 的安装文件 idea64.exe.vmoptions 最后添加 -Deditable.java.test.console=true
/**
 * 一、使用 NIO 完成网络通信的三个核心：
 * 1. 通道（Channel）：负责连接
 *      java.nio.channels.Channel 接口
 *          |--SelectableChannel
 *              |--SocketChannel
 *              |--ServerSocketChannel
 *              |--DatagramChannel
 *
 *              |--Pipe.SinkChannel
 *              |--Pipe.SourceChannel
 *
 * 2. 缓冲区（Buffer）：负责数据存取
 * 3. 选择器（Selector）：是 SelectableChannel 的多路复用器，用于监控 SelectableChannel 的 IO 状况
 *
 */

// TCP SocketChannel
public class TestNonBlockingNIO {
    // 客户端
    @Test
    public void client() {
        SocketChannel sChannel = null;
        try {
            // 1. 获取通道
            sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

            // 2. 切换成非阻塞模式
            sChannel.configureBlocking(false);

            // 3. 分配指定大小的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            // 4. 发送数据给服务器，发送时间
//            buf.put(new Date().toString().getBytes());
//            buf.flip();
//            sChannel.write(buf);
//            buf.clear();
            // 4.发送数据给服务器，发送用户输入的信息
            Scanner scan = new Scanner(System.in);
            while(scan.hasNext()) {
                String str = scan.next();
                buf.put((new Date().toString() + "\n" + str).getBytes());
                buf.flip();
                sChannel.write(buf);
                buf.clear();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (sChannel != null) {
                try {
                    sChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 服务端
    @Test
    public void server() {
        ServerSocketChannel ssChannel = null;
        try {
            // 1. 获取通道
            ssChannel = ServerSocketChannel.open();

            // 2. 切换成非阻塞模式
            ssChannel.configureBlocking(false);

            // 3. 绑定连接
            ssChannel.bind(new InetSocketAddress(9898));

            // 4. 获取一个选择器
            Selector selector = Selector.open();

            // 5.将通道注册到选择器上，并且制定监听 接收 事件
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 6. 轮训式地获取选择器上已经“准备就绪”的事件
            while (selector.select() > 0) { // 表示至少有 1 个准备就绪
                // 7.  获取当前选择器中所有注册的“选择键（已就绪的监听事件）”
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while(it.hasNext()) {
                    // 8. 获取准备“就绪”的事件
                    SelectionKey sk = it.next();

                    // 9. 判断具体是什么事件准备就绪
                    if (sk.isAcceptable()) {
                        // 10. 若“接收就绪”，获取客户端连接
                        SocketChannel sChannel = ssChannel.accept();

                        // 11. 切换成非阻塞模式
                        sChannel.configureBlocking(false);

                        // 12. 将该通道注册选择器
                        sChannel.register(selector, SelectionKey.OP_READ);
                    } else if (sk.isReadable()) {
                        // 13. 获取当前选择器上“读就绪”的通道
                        SocketChannel sChannel = (SocketChannel)sk.channel();

                        // 14. 读取数据
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        int len = 0;
                        while((len = sChannel.read(buf)) != -1) {
                            buf.flip();
                            System.out.println(new String(buf.array(), 0, len));
                            buf.clear();
                        }
                    }

                    // 15. 取消选择键 SelectionKey，否则一直有效
                    it.remove();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
