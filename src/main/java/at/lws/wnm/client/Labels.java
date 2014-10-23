package at.lws.wnm.client;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;

public interface Labels extends Messages {
	String children();
	String child();
	String sections();
	String section();
	String subSection();
	String category();
	String user();
	String admin();
	String teacher();
	String sectionAdmin();
	String create();
	String delete();
	String cancel();
	String rights();
	String change();
	String childDelWarning();
	String sectionDelWarning();
	String observationDelWarning();
	String firstName();
	String lastName();
	String birthday();
	String save();
	String ok();
	String date();
	String name();
	String duration();
	String socialForm();
	String show();
	String configure();
	String serverError();
	String close();
	String notSavedWarning();
	String noChild();
	String noChildWithName(String name);
	String noSection();
	String noDate();
	String noObservation();
	String notLoggedInWarning();
	String toLoginPage();
	String filter();
	String print();
	String logout();
	String title();
	String developementDialogueDates();
	String noChildSelected();
	String noDateGiven();
	String summary(String childName, String sectionName, int count, Date firstDate, Date lastDate);
	String developementDialogueDate();
}