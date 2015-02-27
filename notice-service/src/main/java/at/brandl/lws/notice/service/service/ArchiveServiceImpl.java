package at.brandl.lws.notice.service.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.brandl.lws.notice.service.dao.ds.NoticeArchiveDsDao;

public class ArchiveServiceImpl extends HttpServlet {
	
	private static final int AUGUST = 7;
	private static final long serialVersionUID = -7318489147891141902L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		NoticeArchiveDsDao beobachtungsDao = new NoticeArchiveDsDao();
		Date endDate = calcEndLastSchoolYear();
		int moved = beobachtungsDao.moveAllToArchiveBefore(endDate);
		System.err.println("moved " + moved + " beobachtungen");
	}

	private Date calcEndLastSchoolYear() {
		Calendar calendar = new GregorianCalendar();
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		if(month < AUGUST) {
			year--;
		}
		calendar.set(year, AUGUST, 1, 0, 0, 0);
		return calendar.getTime();
	}

}
