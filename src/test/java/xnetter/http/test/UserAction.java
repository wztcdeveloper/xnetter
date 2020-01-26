package xnetter.http.test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.hibernate.validator.constraints.NotEmpty;
import xnetter.http.annotation.Action;
import xnetter.http.annotation.ParamVariable;
import xnetter.http.annotation.PathVariable;
import xnetter.http.annotation.Request;
import xnetter.http.annotation.Response;

@Action(name="/user")
public class UserAction {
	
	@Request(name="/login/{type:finger|account}", type=Request.Type.POST)
	public String login(@PathVariable(name="type") String type,
			@NotEmpty(message="账号不能为空") String account,
			@ParamVariable(name="passwords")
			@NotEmpty(message="密码不能为空") String password) {
		return "{result:success}";
	}
	
	@Request(name="/list/{unikey}", type=Request.Type.POST)
	public @Response(Response.Type.XML) Object list(@PathVariable(name="unikey") String unikey) {
		return "{result:success}";
	}
	
	@Request(name="/logout", type=Request.Type.POST)
	public String logout() {
		return "{result:success}";
	}

	@Request(name="/file/upload", type=Request.Type.POST)
	public String upload(FullHttpRequest request, FileUpload file2,
						 FileUpload file3, List<FileUpload> files) {
		return "{result:success}";
	}

	@Request(name="/file/download", type=Request.Type.GET)
	public File download(FullHttpRequest request) {
		return new File("D:\\work\\svnrepos\\wztc_work\\上课.txt");
	}

	@Request(name="/ids/bean", type=Request.Type.POST)
	public String bean(UserBean user) {
		return "{result:success}";
	}
	
	@Request(name="/ids/list", type={Request.Type.GET, Request.Type.POST})
	public String listIds(List<Integer> ids) {
		return "{result:success}";
	}
	
	@Request(name="/ids/array", type=Request.Type.POST)
	public String arrayIds(Integer[] ids) {
		return "{result:success}";
	}
	
	@Request(name="/ids/set", type=Request.Type.POST)
	public String setIds(Set<Integer> ids) {
		return "{result:success}";
	}
	
	@Request(name="/list/all/{unikey}", type={Request.Type.GET, Request.Type.POST})
	public String listAll(@PathVariable(name="unikey") String unikey,
						 Integer[] arrayIds,
						 List<Integer> listIds,
						 Set<Integer> setIds,
						 Map<String, Integer> mapIds,
						 UserBean user, String name,
						 @ParamVariable(name="xx") String name2) {
		return "{result:success}";
	}
}
