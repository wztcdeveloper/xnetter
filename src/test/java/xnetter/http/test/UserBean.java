package xnetter.http.test;

import xnetter.http.annotation.ParamVariable;

public class UserBean {
	
	private String nickName;
	private String avatarUrl;
	private String userName;
	
	@ParamVariable(name="mytel")
	private String tel;
	
	private String account;
	private String houseName;
	private Double houseLongitude;
	private Double houseLatitude;
	private String houseAddress;
	private String houseAddressBrief;
	
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getHouseName() {
		return houseName;
	}
	public void setHouseName(String houseName) {
		this.houseName = houseName;
	}
	public Double getHouseLongitude() {
		return houseLongitude;
	}
	public void setHouseLongitude(Double houseLongitude) {
		this.houseLongitude = houseLongitude;
	}
	public Double getHouseLatitude() {
		return houseLatitude;
	}
	public void setHouseLatitude(Double houseLatitude) {
		this.houseLatitude = houseLatitude;
	}
	public String getHouseAddress() {
		return houseAddress;
	}
	public void setHouseAddress(String houseAddress) {
		this.houseAddress = houseAddress;
	}
	public String getHouseAddressBrief() {
		return houseAddressBrief;
	}
	public void setHouseAddressBrief(String houseAddressBrief) {
		this.houseAddressBrief = houseAddressBrief;
	}
	
}

