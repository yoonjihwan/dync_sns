package dync.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import dync.db.RepositoryPersistentManager;
import dync.db.UserPersistentManager;
import dync.model.Code_Repository;
import dync.model.User;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String REQ_ACTION = "action";

	private static final String ACTION_LIST = "list";
	private static final String ACTION_TAG_VIEW = "tag_view";
	private static final String ALL = "all";
	private static final String ACTION_INSERT = "insert";
	private static final String ACTION_EDIT = "edit";
	private static final String ACTION_UPDATE = "update";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_LOGIN = "login";
	private static final String ACTION_AUTH = "check";
	private static final String ACTION_LOGOUT = "logout";

	UserPersistentManager upm = new UserPersistentManager();
	RepositoryPersistentManager rpm = new RepositoryPersistentManager();

	/**
	 * Default constructor.
	 */
	public UserServlet() {
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
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));
		HttpSession session = request.getSession(true);

		System.out.println("UserServlet 실행");
		String contextPath = getServletContext().getContextPath();
		response.setContentType("text/html; charset=utf-8");
		String action = request.getParameter(REQ_ACTION);

		if (action == null) {
			System.out.println("action = null");
			return;
		}

		if (action.equals(ACTION_DELETE)) {
			System.out.println("delete 요청");
			String columnName = request.getParameter("COLUMN_NAME");
			int columnValue = Integer.parseInt(request
					.getParameter("COLUMN_VALUE"));
			if (upm.deleteUser(columnName, columnValue)) {
				String jspPath = "/jsp/dbTest.jsp";
				RequestDispatcher dispatcher = getServletContext()
						.getRequestDispatcher(jspPath);
				dispatcher.forward(request, response);
			} else {
				throw new ServletException("DB Query Error");
			}
		} else if (action.equals(ACTION_LOGIN)) {

			// Request API Hash Data
			String token = request.getParameter("kakao_hash");
			User loginuser = upm.getAuth("USER_KAKAOHASH", token);

			// Check Exist
			if (loginuser.getexist()) {
				// Already User Account
				System.out.println("LOGIN SESSION"
						+ loginuser.getUser_kakaohash());
				createSession(session, loginuser);
				// Return Json
				JSONObject obj = new JSONObject();
				obj.put("logged", "ok");
				obj.put("naver_hash", loginuser.getUser_naverhash());
				obj.put("kakao_hash", loginuser.getUser_kakaohash());
				out.print(obj.toString());
			} else {
				// Create New Account
				if (createAccount(request, token, loginuser)) {
					createSession(session, loginuser);
					// Return Json
					JSONObject obj = new JSONObject();
					obj.put("logged", "ok");
					obj.put("naver_hash", loginuser.getUser_naverhash());
					obj.put("kakao_hash", loginuser.getUser_kakaohash());
					out.print(obj.toString());
				} else {
					print_json_message(response, "result", "no");
				}
			}
		} else if (action.equals(ACTION_AUTH)) {
			User loginuser = new User();
			loginuser = getSessions(session);
			if (loginuser != null) {
				// Return Json
				JSONObject obj = new JSONObject();
				obj.put("logged", "ok");
				obj.put("naver_hash", loginuser.getUser_naverhash());
				obj.put("kakao_hash", loginuser.getUser_kakaohash());
				out.print(obj.toString());
				System.out.println(obj.toString());
			} else {
				JSONObject obj = new JSONObject();
				obj.put("logged", "no");
				obj.put("naver_hash", null);
				obj.put("kakao_hash", null);
				out.print(obj.toString());
				System.out.println(obj.toString());
			}
		} else if (action.equals(ACTION_LOGOUT)) {

			System.out.println("LOGOUT SESSION");
			boolean Result = destroySession(session);
			if (Result == true) {
				// Return Json
				JSONObject obj = new JSONObject();
				obj.put("logged", "no");
				obj.put("naver_hash", "");
				obj.put("kakao_hash", "");
				out.print(obj.toString());
			}
		}
		out.close();
	}

	private boolean createAccount(HttpServletRequest request, String token,
			User loginuser) {
		try {
			// Repository New Code (Time Code)
			Calendar c = Calendar.getInstance();
			long REPOSITORY_CODE = c.getTimeInMillis() / 1000;

			// Create User Account
			System.out.println("NEW USER SESSION");
			String username = request.getParameter("user_name");
			User NewUser = new User();
			NewUser.setUser_kakaohash(token);
			NewUser.setUser_name(username);
			NewUser.setCode_repository(REPOSITORY_CODE);
			upm.insertUser(NewUser);

			// Request User Account
			loginuser = upm.getAuth("USER_KAKAOHASH",
					NewUser.getUser_kakaohash());

			// Create Repository
			Code_Repository REPOSITORY = new Code_Repository();
			REPOSITORY.setUser_id(loginuser.getUser_id());
			REPOSITORY.setCode_repository(loginuser.getCode_repository());
			rpm.insertRepository(REPOSITORY);
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}

	private boolean createSession(HttpSession session, User auth) {
		try {
			session.setAttribute("auth_session", auth);
		} catch (Exception e) {
			System.out.println(e);
		}
		return true;
	}

	private boolean destroySession(HttpSession session) {
		try {
			session.removeAttribute("auth_session");
		} catch (Exception e) {
			System.out.println(e);
		}
		return true;
	}

	private User getSessions(HttpSession session) {
		if (session != null) {
			Object GetSession = session.getAttribute("auth_session");
			User auth = (User) GetSession;
			if (GetSession != null) {
				return auth;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private User makeUserBean(HttpServletRequest request) {
		String strUser_id = request.getParameter(User.USER_ID);
		if (strUser_id == null) {
			return null;
		}
		int user_id = Integer.parseInt(strUser_id);

		String user_naverhash = request.getParameter(User.USER_NAVERHASH);
		String user_kakaohash = request.getParameter(User.USER_KAKAOHASH);
		String user_name = request.getParameter(User.USER_NAME);
		String user_description = request.getParameter(User.USER_DESCRIPTION);
		int code_repository = Integer.parseInt(request
				.getParameter(User.CODE_REPOSITORY));

		User user = new User(user_id, user_naverhash, user_kakaohash,
				user_name, user_description, code_repository);

		return user;
	}

	private void print_json_message(HttpServletResponse response, String key,
			String value) throws UnsupportedEncodingException, IOException {

		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				response.getOutputStream(), "UTF8"));
		response.setContentType("text/html;charset=utf-8");

		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(key, value);
		jsonArray.add(jsonObject);
		out.write(jsonArray.toString());
	}

}
