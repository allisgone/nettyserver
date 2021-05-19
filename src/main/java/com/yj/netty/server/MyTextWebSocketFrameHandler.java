package com.yj.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

import java.text.SimpleDateFormat;
import java.util.*;


public class MyTextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static Map<String,Channel> channelList = new HashMap();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("服务端收到消息："+msg.text()+"来自:"+ctx.channel().remoteAddress().toString());
        //回复消息
        String received = msg.text();
        if(received.startsWith("login")){
            channelList.put(received.split("-")[1],ctx.channel());
            //广播登录
            channelList.forEach((k,v)->{
                v.writeAndFlush(new TextWebSocketFrame(("服务器当前时间："+ timestamp+"  "+msg.text())));
            });
            return;
        }
        channelList.forEach((k,v)->{
            if(received.startsWith(k)){
                v.writeAndFlush(new TextWebSocketFrame(("已收到信息服务器当前时间："+ timestamp+"  "+msg.text())));
                //ctx.channel().writeAndFlush(new TextWebSocketFrame(("已发送信息服务器当前时间："+ timestamp+"  "+msg.text())));
                return;
            }
        });
        //ctx.channel().writeAndFlush(new TextWebSocketFrame(("服务器当前时间："+ timestamp+"  "+msg.text())));
    }

    //当web客户端连接后， 触发方法
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded 被调用 id" + ctx.channel());
        //id 表示唯一的值，LongText 是唯一的 ShortText 不是唯一
/*        System.out.println("handlerAdded 被调用" + ctx.channel()..id().asLongText());
        System.out.println("handlerAdded 被调用" + ctx.channel().id().asShortText());*/
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channelList.remove(ctx.channel());
        System.out.println("handlerRemoved 被调用" + ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            String eventType = null;
            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读空闲";
                    System.out.println(" 读空闲，关闭连接");
                    ctx.channel().writeAndFlush("you are out");
                    ctx.channel().close();
                    break;
                case WRITER_IDLE:
                    eventType = "写空闲";
                    // 不处理
                    break;
                case ALL_IDLE:
                    eventType = "读写空闲";
                    // 不处理
                    break;
            }
            System.out.println(ctx.channel().remoteAddress() + "超时事件：" + eventType);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("=== " + ctx.channel().remoteAddress() + " is active ===");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常发生 " + cause.getMessage());
        ctx.close(); //关闭连接
    }
}