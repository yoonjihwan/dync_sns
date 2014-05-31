package dync.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import dync.model.Issue;

public class IssuePersistentManager extends ConnectDB {
	
	
	public boolean insertIssue(Issue issue){
		connect();
		String sql = "insert into issue(ISSUE_ID,USER_ID,TYPE,SUBJECT,CONTENTS,DISPLAY,RECOMMAND,REG_DATE,UPLOAD)" +
					"values(?,?,?,?,?,?,?,?,?)";
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, issue.getIssue_id());
			pstmt.setInt(2, issue.getUser_id());
			pstmt.setString(3, issue.getType());
			pstmt.setString(4, issue.getSubject());
			pstmt.setString(5, issue.getContents());
			if(issue.isDisplay()) pstmt.setInt(6, 1);
			else pstmt.setInt(6, 0);
			pstmt.setInt(7, issue.getRecommand());
			pstmt.setTimestamp(8, Timestamp.valueOf(issue.getReg_date()));
			pstmt.setString(9, issue.getUpload());
			pstmt.executeUpdate();
		}catch(SQLException e){
			System.out.println(pstmt.toString());
			e.printStackTrace();
			return false;
		}finally{
			disconnect();
		}
		return true;
	}
	
	public boolean deleteIssue(String columnName, int columnValue){
		connect();
		
		String sql = "delete from issue where " + columnName + "=?";
		
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, columnValue);
			pstmt.executeUpdate();
			
			System.out.println(pstmt.toString());
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}finally{
			disconnect();
		}
		return true;
	}
	
	public Issue getIssue(int issue_id){
		connect();
		
		String sql = "select * from issue where ISSUE_ID=?";
		Issue issue = new Issue();
		
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, issue_id);
			ResultSet rs = pstmt.executeQuery();
			
			rs.next();
			issue.setIssue_id(rs.getInt("ISSUE_ID"));
			issue.setUser_id(rs.getInt("USER_ID"));
			issue.setType(rs.getString("TYPE"));
			issue.setSubject(rs.getString("SUBJECT"));
			issue.setContents(rs.getString("CONTENTS"));
			issue.setDisplay(rs.getBoolean("DISPLAY"));
			issue.setRecommand(rs.getInt("RECOMMAND"));
			issue.setReg_date(rs.getString("REG_DATE"));
			issue.setUpload(rs.getString("UPLOAD"));
			
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
		
		
		return issue;
	}
	
	public boolean updateIssue(Issue issue){
		connect();
		
		String sql = "update issue set USER_ID=?,TYPE=?,SUBJECT=?,CONTENTS=?,DISPLAY=?,RECOMMAND=?,REG_DATE=?,UPLOAD=? where ISSUE_ID=?";
		
		
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, issue.getUser_id());
			pstmt.setString(2, issue.getType());
			pstmt.setString(3, issue.getSubject());
			pstmt.setString(4, issue.getContents());
			if(issue.isDisplay()) pstmt.setInt(5, 1);
			else pstmt.setInt(5, 0);
			pstmt.setInt(6, issue.getRecommand());
			pstmt.setTimestamp(7, Timestamp.valueOf(issue.getReg_date()));
			pstmt.setString(8, issue.getUpload());
			pstmt.setInt(9, issue.getIssue_id());
			pstmt.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			disconnect();
		}
		
		return true;
	}
	
	public ArrayList<Issue> getIssueList(){
		connect();
		ArrayList<Issue> issueList = new ArrayList<Issue>();
		
		String sql = "select * from issue order by REG_DATE desc";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				Issue issue = new Issue();
				
				issue.setIssue_id(rs.getInt("ISSUE_ID"));
				issue.setUser_id(rs.getInt("USER_ID"));
				issue.setType(rs.getString("TYPE"));
				issue.setSubject(rs.getString("SUBJECT"));
				issue.setContents(rs.getString("CONTENTS"));
				if(rs.getInt("DISPLAY")==1) issue.setDisplay(true);
				else issue.setDisplay(false);
				issue.setRecommand(rs.getInt("RECOMMAND"));
				issue.setReg_date(rs.getString("REG_DATE"));
				issue.setUpload(rs.getString("UPLOAD"));
				issueList.add(issue);
			}
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
		return issueList;
		
	}
	
	
	
}