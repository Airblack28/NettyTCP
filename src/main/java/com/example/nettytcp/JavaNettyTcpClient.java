package com.example.nettytcp;

import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class JavaNettyTcpClient extends ChannelInboundHandlerAdapter {
	
	String serverIP = "127.0.0.1";
	int serverPort = 5061;
	String messageToSend = "This is Client Response";
	int messageCounter = 0;
	ChannelHandlerContext arg0;
	
	public JavaNettyTcpClient() {
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		Bootstrap bootstrap = new Bootstrap();
	    bootstrap.group(workerGroup)
	    	.channel(NioSocketChannel.class)
	    	.handler(new ChannelInitializer<SocketChannel>() {
	   			
	   		@Override
			protected void initChannel(SocketChannel arg0) throws Exception {
				ChannelPipeline p = arg0.pipeline();
				addHandler(p);
			}
	   			
	   	});
		
	    ChannelFuture f = null;
	    	
	    try {
	    		
			f = bootstrap.connect(serverIP, serverPort);
			System.out.println("Connected");
			int messageSendTime = 2000;
			Timer t = new Timer(messageSendTime, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					eventAction();
				}
			});
			t.start();
			f.channel().closeFuture().sync();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} finally {
				workerGroup.shutdownGracefully();
			}
		
	}
	
	public void eventAction() {
		if(arg0 != null) {
			ByteBuf messageSendByteBuf = Unpooled.buffer((messageToSend+messageCounter).length());
			messageSendByteBuf.writeBytes((messageToSend+messageCounter).getBytes());
			System.out.println("Client send: " + (messageToSend+messageCounter));
			arg0.writeAndFlush(messageSendByteBuf);
			messageCounter++;
		}
	};
	
	private void addHandler(ChannelPipeline p) {
		p.addLast(this);
	};
	
	public static void main(String[] args) {
		new JavaNettyTcpClient();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
		ByteBuf rcvd = (ByteBuf)msg;
		byte[] byt = new byte[rcvd.writerIndex()];
		rcvd.readBytes(byt);
		String rcvdMsg = new String(byt);
		System.out.println("Client Bytes Rcvd: "+ byt.length+" "+rcvdMsg);
	}
	
	
	@Override
	public void channelReadComplete(ChannelHandlerContext arg0) throws Exception {
		arg0.flush();
		System.out.println("channelReadComplete");
	}
	
	@Override
	public void channelActive(ChannelHandlerContext arg0) throws Exception {
		System.out.println("channelActive");
		this.arg0 = arg0;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable cause) throws Exception {
		System.out.println("exceptionCaught");
		cause.printStackTrace();
		arg0.close();
	}

}
