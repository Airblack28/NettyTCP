package com.example.nettytcp;

import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.logging.LogLevel;


public class JavaNettyTcpServer extends ChannelInboundHandlerAdapter {
	
	String serverIP = "127.0.0.1";
	int serverPort = 5061;
	String messageToSend = "This is server Request";
	int messageCounter = 0;
	
	public JavaNettyTcpServer() {
		
		EventLoopGroup eventGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
	    bootstrap.group(eventGroup, workerGroup)
	    	.channel(NioServerSocketChannel.class)
	    	.handler(new LoggingHandler(LogLevel.INFO))
	   		.childHandler(new ChannelInitializer<SocketChannel>() {
	   			
		   		@Override
				protected void initChannel(SocketChannel arg0) throws Exception {
					ChannelPipeline p = arg0.pipeline();
					addHandler(p);
				}
	   			
	   	});
		
	    ChannelFuture f = null;
	    	
	   	try {
	    		
			f = bootstrap.bind(serverPort).sync().channel().closeFuture().sync();
				
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
				
		} finally {
			eventGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	    	
	}
	
	private void addHandler(ChannelPipeline p) {
		p.addLast(this);
	};
	
	public static void main(String[] args) {
		new JavaNettyTcpServer();
	}
	
	public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
		ByteBuf rcvd = (ByteBuf)msg;
		byte[] byt = new byte[rcvd.writerIndex()];
		rcvd.readBytes(byt);
		String rcvdMsg = new String(byt);
		System.out.println("Server Bytes Rcvd: "+ byt.length+" "+rcvdMsg);
		
		ByteBuf messageSendByteBuf = Unpooled.buffer((messageToSend+messageCounter).length());
		messageSendByteBuf.writeBytes((messageToSend+messageCounter).getBytes());
		System.out.println("Server send: " + (messageToSend+messageCounter));
		arg0.writeAndFlush(messageSendByteBuf);
		messageCounter++;
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext arg0) throws Exception {
		arg0.flush();
		System.out.println("channelReadComplete");
	}
	
	@Override
	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		System.out.println("channelActive");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable cause) throws Exception {
		System.out.println("exceptionCaught");
		cause.printStackTrace();
		arg0.close();
	}
	

}
