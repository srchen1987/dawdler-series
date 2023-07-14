module dawdler.jakarta.fileupload {
	requires java.base;
	requires transitive jakarta.servlet;
	requires org.apache.commons.io;
	requires portlet.api;

	exports org.apache.commons.fileupload;
	exports org.apache.commons.fileupload.disk;
	exports org.apache.commons.fileupload.servlet;
	exports org.apache.commons.fileupload.util;
	exports org.apache.commons.fileupload.util.mime;
}