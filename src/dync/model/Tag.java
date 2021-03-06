package dync.model;
/**
 * File : Tag.java 
 * Description : Dync 프로그램 DO 클래스
 * 
 * @author JangHun
 * Version : 0.1
 * 제작일 : 2014-05-22
 */
public class Tag {
	public static final String USER_ID			= "USER_ID";
	public static final String TAG_NAME 		= "TAG_NAME";
	public static final String ISSUE_ID 		= "ISSUE_ID";

	private int user_id;
	private String tag_name;
	private int issue_id;

	public Tag() {
		// TODO Auto-generated constructor stub
	}

	public Tag(int user_id, String tag_name) {
		super();
		this.user_id = user_id;
		this.tag_name = tag_name;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getTag_name() {
		return tag_name;
	}

	public void setTag_name(String tag_name) {
		this.tag_name = tag_name;
	}

	public void setIssue_id(int autoId) {
		// TODO Auto-generated method stub
		this.issue_id = autoId;
	}
	public int getIssue_id() {
		return issue_id;
	}

}
