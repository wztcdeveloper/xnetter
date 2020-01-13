package xnetter.http.data.decode;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析from表单数据（Content-Type = x-www-form-urlencoded）
 * @author majikang
 * @create 2019-11-05
 */
public class FormDecoder extends Decoder {
	//  Disk if size exceed
	private static final HttpDataFactory factory =
			new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	public FormDecoder() {
		this(null);
	}
	
	public FormDecoder(Decoder next) {
		super(next);
	}
	
	@Override
	protected void doDecode(FullHttpRequest request) throws IOException {
		// TODO Auto-generated method stub
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request, StandardCharsets.UTF_8);
		for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
            if (data.getHttpDataType() == HttpDataType.Attribute) {
				Attribute attribute = (Attribute) data;
                params.put(attribute.getName(), attribute.getValue());
            } else if (data.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload upload = (FileUpload) data;
				if (upload.isCompleted()) {
					saveToDisk(upload);
					addToParam(upload);
				}
			}
        }
	}

	/**
	 * 添加到参数
	 * 为了用户访问方便，还添加到List里面
	 * @param upload
	 */
	private void addToParam(FileUpload upload) {
		params.put(upload.getName(), upload);

		if (!params.containsKey(Decoder.DEFAULT_KEY)) {
			params.put(Decoder.DEFAULT_KEY, new ArrayList<FileUpload>());
		}

		Object value = params.get(Decoder.DEFAULT_KEY);
		if (value instanceof List) {
			((List<FileUpload>)value).add(upload);
		}
	}

	/**
	 * 根据用户配置决定，是否存入临时文件
	 * @param upload
	 * @throws IOException
	 */
	private void saveToDisk(FileUpload upload) throws IOException {
		if (StringUtils.isNotEmpty(DiskFileUpload.baseDirectory)) {
			StringBuffer sb = new StringBuffer()
					.append(DiskFileUpload.baseDirectory)
					.append(upload.getFilename());
			upload.renameTo(new File(sb.toString()));
		}
	}
}
