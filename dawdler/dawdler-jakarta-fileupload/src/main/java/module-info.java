module dawdler.jakarta.fileupload {
	exports org.apache.commons.fileupload;
	exports org.apache.commons.fileupload.disk;
	exports org.apache.commons.fileupload.servlet;
	exports org.apache.commons.fileupload.util;
	exports org.apache.commons.fileupload.util.mime;
	requires java.base;
	requires commons.io;
	requires jakarta.servlet;
}