package xnetter.http.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import xnetter.http.annotation.Action;
import xnetter.http.annotation.ParamVariable;
import xnetter.http.annotation.PathVariable;
import xnetter.http.annotation.Request;
import xnetter.http.annotation.Response;

@Action(name="/user")
public class UserAction {
	
	@Request(name="/login/{type:finger|account}", type=Request.Type.POST)
	public String login(@PathVariable(name="type") String type, String account, 
			@ParamVariable(name="passwords") String password) {
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
	
	@Request(name="/ids/map", type=Request.Type.POST)
	public String mapIds(Map<String, Integer> ids, String name, @ParamVariable(name="xx") String name2) {
		return "{result:success}";
	}
}
