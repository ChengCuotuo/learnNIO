package com.nianzuochen.nio;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *  一、通道（Channel）：用于源节点和目标节点的连接。在 Java NIO 中负责缓冲区中数据的传输。
 *                      Channel 本身不存储数据，因此需要配合缓冲区进行传输。
 *  二、通道的主要实现类
 *      java.nio.chanells.Channel 接口
 *          |--FileChannel  操作本地文件数据传输
 *          |--SocketChannel    网络 IO
 *          |--ServerSocketChannel   网络 IO
 *          |--DatagramChannel   网络 IO
 *  三、获取通道
 *  1. Java 针对支持通道的类体用了 getChannel() 方法
 *          本地 IO
 *          FileInputStream/FileOutputStream
 *          RandomAccessFile
 *
 *          网络IO
 *          Socket
 *          ServerSocket
 *          DatagramSocket
 *  2. 在 jdk1.7 中的 NIO.2 （jdk1.7之后对 NIO 的改变统称为 NIO.2）针对各个通道提供了静态方法 open()
 *  3. 在jdk1.7 中的 NIO.2 的 Files 工具类的 newByteChannel()
 *
 *  四、通道之间的数据传输
 *      transferFrom()
 *      transferTo()
 *  五、分散（Scatter）于聚集（Gather）
 *      分散读取（Scattering Read）: 将通道中的数据分散到多个缓冲区，按顺序写入
 *      聚集写入（Gathering Writes)：将多个缓冲区中的数据聚集到通道中，按顺序读取
 *
 *   六、字符集，Charset
 *      编码：字符串 -> 字节数组
 *      解码：字节数组 -> 字符串
 */
public class TestChannel {
    // 6.编码与解码
    @Test
    public void test6() {
        try {
            Charset cs1 = Charset.forName("GBK");
            // 获取编码器
            CharsetEncoder ce = cs1.newEncoder();
            // 获取解码器
            CharsetDecoder cd = cs1.newDecoder();

            CharBuffer cBuf = CharBuffer.allocate(1024);
            cBuf.put("齐齐哈尔");
            cBuf.flip();

            // 编码
            ByteBuffer bBuf = ce.encode(cBuf);

            for (int i = 0; i < 8; i++) {
                System.out.println(bBuf.get());
            }

            // 解码
            bBuf.flip();
            CharBuffer cBuf2 = cd.decode(bBuf);
            System.out.println(cBuf2.toString());

            System.out.println("-------------------");

            Charset cs2 = Charset.forName("UTF-8");
            bBuf.flip();
            CharBuffer cBuf3 = cs2.decode(bBuf);
            System.out.println(cBuf3.toString());   // 用 GBK 编码，使用 UTF-8 解码，得到乱码

        } catch (CharacterCodingException ex) {
            ex.printStackTrace();
        }
    }

    // 5.字符集
    @Test
    public void test5() {
        // 获取并打印支持的字符集
        Map<String, Charset> map = Charset.availableCharsets();
        Set<Entry<String, Charset>> set = map.entrySet();

        for(Entry<String, Charset> entry : set) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    // 4. 分散于聚集，操作缓冲区数组
    @Test
    public void test4() {
        RandomAccessFile raf1 = null;
        RandomAccessFile raf2 = null;
        try {
            raf1 = new RandomAccessFile("1.txt", "rw");
            // 1.获取通道
            FileChannel channel1 = raf1.getChannel();
            //2. 分配指定大小的缓冲区
            ByteBuffer buf1 = ByteBuffer.allocate(100);
            ByteBuffer buf2 = ByteBuffer.allocate(1024);
            // 3.分散读取
            ByteBuffer[] bufs = {buf1, buf2};
            channel1.read(bufs);

            for (ByteBuffer byteBuffer : bufs) {
                byteBuffer.flip();
            }

            System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
            System.out.println("-----------------------------");
            System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));

            // 4. 聚集写入
            raf2 = new RandomAccessFile("2.txt", "rw");
            FileChannel channel2 = raf2.getChannel();
            channel2.write(bufs);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();

        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            if (raf1 != null) {
                try {
                    raf1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(raf2 != null) {
                try {
                    raf2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 3.通道之间的数据传输（直接缓冲区）
    @Test
    public void test3() {
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            try {
                inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
                outChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);

                // inChannel.transferTo(0, inChannel.size(), outChannel);
                outChannel.transferFrom(inChannel, 0, inChannel.size());
            } catch (IOException ex) {
                ex.printStackTrace();
                if (inChannel != null) {
                    try {
                        inChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outChannel != null) {
                    try {
                        outChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

    // 2.使用直接缓冲区完成文件的复制（内存映射文件，只有 ByteBuffer 支持）
    @Test
    public void test2() {
        long start = System.currentTimeMillis(); // 修改为大文件进行测试

        FileChannel inChannel = null;
        FileChannel outChannel = null;

       try {
           inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
           // open  的两个参数，path 表示路径，options 表示操作，options 是一个可变参数
           // Paths.get() 可以打开一条路径
           // StandardOpenOption 是一个枚举类型，里面有读写等操作，StandardOpenOption.CREATE_NEW 表示不存在就创建，存在就报错
           outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE_NEW);

           // 内存映射文件，现在的缓冲区在物理内存中（和 allocateDirect() 一样，只是获取方法不同）
           MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
           // 其中的参数，对映射文件的操作模式，只读，读取从 0 开始，读取的大小为 inChannel.size()
           MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
           // 此时文件直接操作映射文件，就是直接操作硬盘
           // 直接对缓冲区记性数据的读写
           byte[] dest = new byte[inMappedBuf.limit()];
           // 读
           inMappedBuf.get(dest);
           // 写
           outMappedBuf.put(dest);
       } catch (IOException ex) {
           ex.printStackTrace();
       } finally {
           if (inChannel != null) {
               try {
                   inChannel.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }

           if (outChannel != null) {
               try {
                   outChannel.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }

        long end = System.currentTimeMillis();
        System.out.println("耗费：" + (end - start));
    }

    // 1.利用通道完成数据的复制（非直接缓冲区）
    @Test
    public void test1(){
        long start = System.currentTimeMillis();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            fis = new FileInputStream("1.jpg");
            fos = new FileOutputStream("2.jpg");

            // 1.1 获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            // 1.2 分配指定大小的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            // 1.3 将通道中的数据存入缓冲区
            while (inChannel.read(buf) != -1) {
                // 1.4 将缓冲区中的数据写入通道中
                buf.flip();     // 切换成读数据模式，此时 position 的值为 0
                outChannel.write(buf);
                buf.clear();    // 清空缓冲区
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("耗费：" + (end - start));
    }
}
