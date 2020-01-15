package xnetter.http.response;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.commons.lang3.StringUtils;
import xnetter.http.core.HttpConf;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URLEncoder;

/**
 * 将文件返回给客户端，默认用FileRegion实现Zero-Copy
 * SSL用ChunkedFile来实现分块发送
 * @author majikang
 * @create 2019-01-15
 */
public final class FileResponser extends Responser {
    private final String rootPath;
    private final HttpConf conf;

    public FileResponser(FullHttpRequest request, ChannelHandlerContext ctx) {
        this(request, ctx, null, null);
    }
    
    public FileResponser(FullHttpRequest request, ChannelHandlerContext ctx, HttpConf conf, String rootPath) {
        super(request, ctx);
        this.conf = conf;
        this.rootPath = rootPath;
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
        // 该编码器可以支持FileRegion、ChunkedFile
       cp.replace(cp.get("encoder"), "encoder", cwHandler);

        // 3 把文件传输给前端
        writeFile(file, raf);

        // 4 还原之前的编码器
        // Encoder is not a @Sharable handler, so can't be added or removed multiple times
        cp.replace(cwHandler, "encoder", new HttpResponseEncoder());
    }

    private void writeResponse(File file) throws UnsupportedEncodingException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);

        //String contentType = "application/octet-stream;charset=UTF-8";
        String contentType = new MimetypesFileTypeMap().getContentType(file.getPath());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, getDisposition(file));
        if (!isClose(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
    }

    private String getDisposition(File file) throws UnsupportedEncodingException {
        String type = "attachment";
        if (conf != null && StringUtils.isNotEmpty(rootPath)
                && conf.displayDirs.contains(rootPath)) {
            type = "inline";
        }

        // 指定文件名编码格式，防止前端文件名显示乱码
        return String.format("%s; filename*=UTF-8''%s",
                type, URLEncoder.encode(file.getName(), "UTF-8"));
    }

    private void writeFile(File file, RandomAccessFile raf) throws IOException {
        ChannelFuture sendFuture;
        ChannelFuture lastFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            // 传输文件使用了 DefaultFileRegion 写入到 NioSocketChannel 中
            sendFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, file.length()),
                    ctx.newProgressivePromise());
            // Write the end marker (LastHttpContent) .
            lastFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            // SSL enabled - cannot use zero-copy file transfer.
            sendFuture = ctx.writeAndFlush(new ChunkedFile(raf),
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
