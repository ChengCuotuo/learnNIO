package com.nianzuochen.nio;

import java.nio.ByteBuffer;

import org.junit.Test;


/**
 * 一、缓冲区(Buffer) ：在 Java NIO  中负责数据的存储。缓冲区底层是现实数组，用于存储不同数据类型的数据
 *		根据数据类型（boolean 以外）不同，提供了对应类型的缓冲区。
 *		ByteBuffer、CharBuffer、ShortBuffer、IntBuffer、LongBuffer、FloatBuffer、DoubleBuffer
 *		这些缓冲区的管理方式都是一样的，使用 allocate() 获取缓冲区。
 *
 *	二、缓冲区存取数据的两个核心方法
 *		put() 存入数据到缓冲区
 *		get() 读取缓冲区的数据
 *
 *  三、缓冲区的四个核心属性：
 *      capacity ： 容量，表示缓冲区中最大存储数据的容量，一旦生命不可改变。
 *      limit : 界限，表示缓冲区中的可以操作数据的大小，limit 后面的数据不能进行读写
 *      position ： 位置，表示缓冲区中正在操作数据的位置
 *
 *      mark : 标记，表示记录当前 position 的位置，可以通过 reset() 恢复到 mark 位置
 *
 *      0 <= mark <= position <= limit <= capacity
 *
 * 四、直接缓冲区和非直接缓冲区
 *      非直接缓冲区：通过 allocate() 方法分配缓冲区，将缓冲区建立在 JVM 的内存中
 *      直接缓冲区：通过 allocateDirect() 方法分配缓冲区，将缓冲区建立在物理内存中，可以特高效率
 *
 */
public class TestBuffer {
    @Test
    public void test3() {
        // 分配直接缓冲区
        ByteBuffer  buf = ByteBuffer.allocateDirect(1024);
        System.out.println(buf.isDirect());  // true
    }

    @Test
    public  void test2() {
        String str = "abcde";
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(str.getBytes());
        buf.flip();
        byte[] dest = new byte[buf.limit()];
        buf.get(dest, 0, 2);
        System.out.println(new String(dest, 0, 2));  // ab
        System.out.println(buf.position()); // 2

        // mark() 标记
        buf.mark();

        buf.get(dest, 2, 2);
        System.out.println(new String(dest, 2, 2)); // cd
        System.out.println(buf.position()); //  4

        // reset() 恢复到 mark() 的位置
        buf.reset();
        System.out.println(buf.position());  // 2

        // 判断缓冲区是否有剩余
        // hasRemaining()
        if (buf.hasRemaining()) {
            //获取缓冲区中的剩余
            // remaining()
            System.out.println(buf.remaining()); // 3
        }
    }

    @Test
    public void test1() {
        String str = "abcde";
        // 1. 分配一个指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);
        System.out.println("----------------------allocate()----------------------");
        System.out.println(buf.position());     // 0
        System.out.println(buf.limit());        // 1024
        System.out.println(buf.capacity());     // 1024

        // 2. 利用 put() 方法存入数据到缓冲区中
        buf.put(str.getBytes());

        System.out.println("----------------------put()----------------------");
        System.out.println(buf.position());     // 5
        System.out.println(buf.limit());        // 1024
        System.out.println(buf.capacity());     // 1024

        // 3. 切换成读取数据的模式
        buf.flip();  // flip 快速跳转，将 limit 设置为当前的 position，position 跳转到缓冲区头

        System.out.println("----------------------flip()----------------------");
        System.out.println(buf.position());     // 0
        System.out.println(buf.limit());        // 5
        System.out.println(buf.capacity());     // 1024

        // 4. 利用 get() 方法读取缓冲区的数据
        byte[] dest = new byte[buf.limit()];
        buf.get(dest);
        System.out.println(new String(dest, 0, dest.length));  // abcde

        System.out.println("----------------------get()----------------------");
        System.out.println(buf.position());     // 5
        System.out.println(buf.limit());        // 5
        System.out.println(buf.capacity());     // 1024

        // 5. rewind()
        buf.rewind();   // 进行重读，也就是将 position 返回上一个状态
        System.out.println("----------------------rewind()----------------------");
        System.out.println(buf.position());     // 0
        System.out.println(buf.limit());        // 5
        System.out.println(buf.capacity());     // 1024

        // 6. clear() 情况缓冲区
        buf.clear();    // 但是缓冲区中的数据依然存在，只是出于一种“被遗忘”状态
                        // 主要是 position 和 limit 处于初始状态，无法明确知道数据的长度
        System.out.println("----------------------clear()----------------------");
        System.out.println(buf.position());     // 0
        System.out.println(buf.limit());        // 1024
        System.out.println(buf.capacity());     // 1024

        System.out.println((char)buf.get()); // a
    }
}
