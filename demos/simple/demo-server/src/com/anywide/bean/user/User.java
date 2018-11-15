package com.anywide.bean.user;

import java.io.Serializable;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;

	private int userid;
	
	private String username;
	
	private int age;
	
	private short sex;


	 
	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public short getSex() {
		return sex;
	}

	public void setSex(short sex) {
		this.sex = sex;
	}
	
	
}
