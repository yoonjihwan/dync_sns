package dync.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.oreilly.servlet.MultipartRequest;

import dync.db.IssuePersistentManager;
import dync.db.TagPersistentManager;
import dync.model.Issue;
import dync.model.Tag;
import dync.model.User;

/**
 * Servlet implementation class IssueServlet
 */
@MultipartConfig
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

	MultipartRequest multi;
	String fullFileName = "";

	private static final String ACTION_GET_ISSUE = "get_issue";

	private IssuePersistentManager ipm = new IssuePersistentManager();
	private TagPersistentManager tpm = new TagPersistentManager();
	/**
	 * Default constructor.
	 */
	public IssueServlet() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);

	}

	private void processRequest(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods",
				"GET, PUT, POST, OPTIONS, DELETE");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
		response.addHeader("Access-Control-Max-Age", "86400");
		String action_request = request.getParameter(REQ_ACTION);
		//response.setCharacterEncoding("utf-8");
		//response.setContentType("text/html;charset=UTF-8");

		System.out.println(action_request);

		request_action(action_request, request, response);
	}
	
	private void print_json_message(HttpServletResponse response, String key,String value) throws UnsupportedEncodingException, IOException{
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));
		response.setContentType("text/html;charset=utf-8");
		
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(key, value);
		jsonArray.add(jsonObject);
		out.write(jsonArray.toString());
	}
	
	private void request_action(String action, HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
		response.getOutputStream(), "UTF8"));
		response.setContentType("text/html;charset=utf-8");

		// System.out.println(action);
		if (action == null) {
			System.out.println("action = null");
			return;
		}

		if (action.equals(ACTION_INSERT)) {
			System.out.println("insert 요청");
			
			Issue issue = makeIssueBean(request);
			if (issue != null) {
				if (uploadFile(request, response)) {

					if (ipm.insertIssue(issue)) {
						//이슈 작성시 태그 삽입[일단 보류]
						if (issue.getTag() != null && ipm.getAutoId() != 0) {
							String tagNames = issue.getTag();
							tagNames.replaceAll("\\p{Space}", "");
							String tag_name[] = tagNames.split(",");
							for (String tagName : tag_name) {
								Tag tag = new Tag();

								tag.setIssue_id(ipm.getAutoId());
								tag.setTag_name(tagName);
								tag.setUser_id(issue.getUser_id());

								tpm.insertTag(tag);
							}
						}
						print_json_message(response, "result", "ok");
					} else {
						print_json_message(response, "result", "no");
					}
				} else {
					if (ipm.insertIssue(issue)) {
						//이슈 작성 시 태그 삽입[일단 보류]
						if (issue.getTag() != null && ipm.getAutoId() != 0) {
							String tagNames = issue.getTag();
							tagNames.replaceAll("\\p{Space}", "");
							String tag_name[] = tagNames.split(",");
							for (String tagName : tag_name) {
								Tag tag = new Tag();

								tag.setIssue_id(ipm.getAutoId());
								tag.setTag_name(tagName);
								tag.setUser_id(issue.getUser_id());

								tpm.insertTag(tag);
							}
						}
						print_json_message(response, "result", "ok");
					} else {
						System.out.println("DB Insert Fail");
						print_json_message(response, "result", "no");
					}
					System.out.println("파일 업로드 실패");
				}
			} else {
				System.out.println("Issue User 정보 불러오기 실패");
				print_json_message(response, "result", "no");
			}
			/*
			 * Issue issue = makeIssueBean(multi); PrintWriter out =
			 * response.getWriter(); try { Enumeration files = null; File file =
			 * null; fileName = multi.getFilesystemName("UPLOAD"); // 파일의 이름 얻기
			 * if(fileName.equals(null)){ System.out.println("업로드된 파일 없음");
			 * 
			 * }else{ files = multi.getFileNames(); String name = (String)
			 * files.nextElement(); file = multi.getFile(name); } } catch
			 * (Exception e) { System.out.print("예외 발생 : " + e); }
			 */

		} else if (action.equals(ACTION_DELETE)) {
			System.out.println("delete 요청");
			String columnName = request.getParameter("COLUMN_NAME");
			int columnValue = Integer.parseInt(request
					.getParameter("COLUMN_VALUE"));
			if (ipm.deleteIssue(columnName, columnValue)) {
				print_json_message(response, "result", "ok");
			} else {
				print_json_message(response, "result", "no");
			}
		} else if (action.equals(ACTION_EDIT)) {
			System.out.println("edit 요청");
			String issue_id = request.getParameter("ISSUE_ID");
			Issue issue = ipm.getIssue(Integer.parseInt(issue_id));
			request.setAttribute("issue", issue);
			String jspPath = "/jsp/dbTest.jsp";
			RequestDispatcher dispatcher = getServletContext()
					.getRequestDispatcher(jspPath);
			dispatcher.forward(request, response);
		} else if (action.equals(ACTION_UPDATE)) {
			System.out.println("update 요청");
			if (uploadFile(request, response)) {
				Issue issue = makeIssueBean(request);
				if (ipm.updateIssue(issue)) {
					print_json_message(response, "result", "ok");
				} else {
					print_json_message(response, "result", "no");
				}
			} else {
				print_json_message(response, "result", "no");
				System.out.println("파일 업로드 실패");
			}
		} else if (action.equals(ACTION_GET_ISSUE)) {
			System.out.println("getIssue 요청");
			int issue_id = Integer.parseInt(request.getParameter("ISSUE_ID"));
			ArrayList<Issue> issueList = ipm.getIssueList();
			boolean flag = true;
			for (Issue issue : issueList) {
				if (issue.getIssue_id() == issue_id) {
					flag = false;
					break;
				}

			}
			if (flag) {
				request.setAttribute("errorMessage", "유효하지 않은 ISSUE_ID");
				gotoJsp(request, response, "/jsp/errorPage.jsp");
				return;
			}

			Issue issue = ipm.getIssue(issue_id);
			JSONArray json = new JSONArray();
			json.add(issue);
			out.print(json.toString());
			System.out.println(json);
		} else if (action.equals(ACTION_LIST)) {
			System.out.println("list 요청");
			ArrayList<Issue> issueList = ipm.getIssueList();
			JSONObject json = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			jsonArray.addAll(issueList);
			out.print(jsonArray.toString());
		}
		out.close();
	}

	private boolean uploadFile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		final String path = "C:/DEVSNS/saveFile/"
				+ request.getParameter("ISSUE_ID");
		File dir = new File(path);
		if (!dir.isDirectory()) {
			System.out.println("폴더를 생성 합니다.");
			if (!dir.mkdirs()) {
				System.out.println("폴더 생성 실패");
			}
		}
		final Part filePart = request.getPart("UPLOAD");
		final String fileName = getFileName(filePart);
		fullFileName = path + File.separator + fileName;
		System.out.println(fileName);

		OutputStream out = null;
		InputStream filecontent = null;

		try {
			out = new FileOutputStream(new File(fullFileName));
			filecontent = filePart.getInputStream();

			int read = 0;
			final byte[] bytes = new byte[1024];

			while ((read = filecontent.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			// writer.println("New file " + fileName + " created at " + path);
		} catch (FileNotFoundException fne) {
			System.out.println("파일 업로드 실패");
			return false;

		} finally {
			if (out != null) {
				out.close();
			}
			if (filecontent != null) {
				filecontent.close();
			}
			/*
			 * if (writer != null) { writer.close(); }
			 */
		}

		return true;
	}

	private static String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				String filename = cd.substring(cd.indexOf('=') + 1).trim()
						.replace("\"", "");
				return filename.substring(filename.lastIndexOf('/') + 1)
						.substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
			}
		}
		return null;
	}

	private static String getValue(Part part) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				part.getInputStream(), "UTF-8"));
		StringBuilder value = new StringBuilder();
		char[] buffer = new char[1024];
		for (int length = 0; (length = reader.read(buffer)) > 0;) {
			value.append(buffer, 0, length);
		}
		return value.toString();
	}
	
	private int user_session_check(HttpServletRequest request){
		HttpSession session = request.getSession(false);
		if (session != null) {
		    session = request.getSession(true);
			User auth = (User) session.getAttribute("auth_session");
			return auth.getUser_id();
		} else {
			return -1;
		}
	}

	private Issue makeIssueBean(HttpServletRequest request) {
		int user_id = user_session_check(request);
		System.out.println(user_id);
		if (user_id != -1) {
			String type = request.getParameter(Issue.TYPE);
			String subject = request.getParameter(Issue.SUBJECT);
			String contents = request.getParameter(Issue.CONTENTS);
			System.out.println(contents);
			boolean display = Boolean.parseBoolean(request
					.getParameter(Issue.DISPLAY));
			int recommand = Integer.parseInt(request
					.getParameter(Issue.RECOMMAND));
			String tag = request.getParameter(Issue.TAG);
			// String reg_date = request.getParameter(Issue.REG_DATE);

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();

			String reg_date = dateFormat.format(cal.getTime());
			String upload = fullFileName;
			Issue issue = new Issue(0, user_id, type, subject, contents,
					display, recommand, tag, reg_date, upload);
			return issue;
		} else {
			return null;
		}
	}


	private void gotoJsp(HttpServletRequest request,
			HttpServletResponse response, String path) throws ServletException,
			IOException {
		String jspPath = path;
		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher(jspPath);
		dispatcher.forward(request, response);
	}
}
