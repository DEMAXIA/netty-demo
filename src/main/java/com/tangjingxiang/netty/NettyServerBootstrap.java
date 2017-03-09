package com.tangjingxiang.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;

/**
 * Created by tangjingxiang on 2016/12/14.
 */
public class NettyServerBootstrap {

    private static Logger logger = Logger.getLogger(NettyServerBootstrap.class);

    private int port;

    public NettyServerBootstrap(int port) {
        this.port = port;
        bind();
    }

    private void bind() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024); //连接数
            bootstrap.option(ChannelOption.TCP_NODELAY, true);  //不延迟，消息立即发送
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //长连接
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    // 以("\n")为结尾分割的 解码器
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    // 字符串解码 和 编码
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast("logging", new LoggingHandler());
                    pipeline.addLast("handler", new NettyServerHandler());
                }
            });


            ChannelFuture f = bootstrap.bind(port).sync();
            if (f.isSuccess()) {
                logger.debug("启动Netty服务成功，端口号：" + this.port);
            }

            // 关闭连接
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            logger.error("启动Netty服务异常，异常信息：" + e.getMessage());
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        NettyServerBootstrap bootstrap = new NettyServerBootstrap(19999);
    }

}
