package dync.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dync.db.PersistentManager;
import dync.model.Issue;

/**
 * Servlet implementation class IssueServlet
 */
public class IssueServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String REQ_ACTION = "action";

	private static final String ACTION_LIST = "list";
	private static final String ACTION_TAG_VIEW = "tag_view";
	private static final String ALL = "all";
	private static final String ACTION_INSERT = "insert";
	private static final String ACTION_EDIT = "edit";
	private static final String ACTION_UPDATE = "update";
	private static final String ACTION_DELETE = "delete";
	
	private PersistentManager pm = new PersistentManager();
	/**
	 * Default constructor.
	 */
    public IssueServlet() 
    {
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request,response);
	}
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	IOException
	{
		System.out.println("IssueServlet 실행");
		String contextPath = getServletContext().getContextPath();
		response.setContentType("text/html; charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String action = request.getParameter(REQ_ACTION);
		
		if(action == null) {
			System.out.println("action = null");
			return;
		}
		
		if(action.equals(ACTION_INSERT))
		{
			System.out.println("insert 요청");
			Issue issue = makeIssueBean(request);
			PrintWriter out = response.getWriter();
			if(pm.insertIssue(issue)){
				String jspPath = "/jsp/dbTest.jsp";
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(jspPath);
				dispatcher.forward(request, response);
			}else {
				throw new ServletException("DB Query Error");
			}
		}
	}
	
	private Issue makeIssueBean(HttpServletRequest request)
	{
		String strIssue_id = request.getParameter(Issue.ISSUE_ID);
		if(strIssue_id == null)
		{
			return null;
		}
		int issue_id = Integer.parseInt(strIssue_id);
		
		
		int user_id = Integer.parseInt(request.getParameter(Issue.USER_ID));
		String type = request.getParameter(Issue.TYPE);
		String subject = request.getParameter(Issue.SUBJECT);
		String contents = request.getParameter(Issue.CONTENTS);
		boolean display = Boolean.parseBoolean(request.getParameter(Issue.DISPLAY));
		int recommand = Integer.parseInt(request.getParameter(Issue.RECOMMAND));
		String reg_date = request.getParameter(Issue.REG_DATE);
		
		
		Issue issue = new Issue(issue_id,user_id,type,subject,contents,display,recommand,reg_date);
		
		return issue;
	}

}
