module dawdler.jakarta.fileupload {
	requires java.base;
	requires  jakarta.servlet;
	requires org.apache.commons.io;
	requires transitive portlet.api;

	exports org.apache.commons.fileupload;
	exports org.apache.commons.fileupload.disk;
	exports org.apache.commons.fileupload.servlet;
	exports org.apache.commons.fileupload.util;
	exports org.apache.commons.fileupload.util.mime;
}