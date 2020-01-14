package xnetter.http.response;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;

/**
 * 将文件返回给客户端，默认用FileRegion实现Zero-Copy
 * SSL用ChunkedFile来实现分块发送
 * @author majikang
 * @create 2019-01-15
 */
public final class FileResponser extends Responser {
    public FileResponser(FullHttpRequest request, ChannelHandlerContext ctx) {
        super(request, ctx);
    }

    public void write(File file) {
        try {
            write(file, new RandomAccessFile(file, "r"));
        } catch (FileNotFoundException e) {
            logger.warn("file {} not found", file.getPath());
            writeError(e);
        } catch (IOException e) {
            writeError(e);
        }
    }

    private void write(File file, RandomAccessFile raf) throws IOException {
        //1 把响应告诉前端
        writeResponse(file);

        ChannelPipeline cp = ctx.channel().pipeline();
        ChannelHandler cwHandler = new ChunkedWriteHandler();

        // 2 移除之前的编码器
        cp.replace(cp.get("encoder"), "encoder", cwHandler);

        // 3 把文件传输给前端
        writeFile(file, raf);

        // 4 还原之前的编码器
        // Encoder is not a @Sharable handler, so can't be added or removed multiple times
        cp.replace(cwHandler, "encoder", new HttpResponseEncoder());
    }
    private void writeResponse(File file) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);

        //String contentType = "application/octet-stream;charset=UTF-8";
        String contentType = new MimetypesFileTypeMap().getContentType(file.getPath());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION,
               String.format("attachment; filename=\"%s\"", file.getName()));
        if (!isClose(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
    }

    private void writeFile(File file, RandomAccessFile raf) throws IOException {
        ChannelFuture sendFuture;
        ChannelFuture lastFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            // 传输文件使用了 DefaultFileRegion 进行写入到 NioSocketChannel 中
            sendFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, file.length()),
                    ctx.newProgressivePromise());
            // Write the end marker (LastHttpContent) .
            lastFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            // SSL enabled - cannot use zero-copy file transfer.
            sendFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf)),
                    ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker for us.
            lastFuture = sendFuture;
        }

        sendFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) {
                    logger.warn("download file progress: {}, {}.", file.getName(), progress);
                } else {
                    logger.debug("download file progress: {}, {}/{}.", file.getName(), progress, total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                if (future.isSuccess()) {
                    logger.info("download file completed: {}", file.getName());
                } else {
                    logger.error(String.format("file %s transfer exception.", file.getName()),
                            future.cause());
                }
            }
        });

        if (isClose(request)) {
            lastFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

}
