package com.anywide.springboot.filter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
/**
 * Servlet Filter implementation class ValidateFilter
 */
@Component
@Order(1)
@WebFilter(filterName = "ValidateFilter", urlPatterns = "/*")
public class ValidateFilter implements Filter {

    /**
     * Default constructor. 
     */
    public ValidateFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hrequest = (HttpServletRequest) request;
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		String type = hrequest.getHeader("Content-Type");
		boolean isJson = type!=null&&type.contains("application/json");
		if(isJson) {
			 ServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(hrequest);
//             String body = getBodyString(requestWrapper);  
             chain.doFilter(requestWrapper, response);
		}
		else {
			chain.doFilter(request, response);
		}
		
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
	
	public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

	    private String body;
	    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
	        super(request);
	        body = getBodyString(request);
	    }

	    @Override
	    public BufferedReader getReader() throws IOException {
	        return new BufferedReader(new InputStreamReader(getInputStream()));
	    }
	    public String getBody() {
	    	return body;
	    }
	    @Override
	    public ServletInputStream getInputStream() throws IOException {
	        final ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes(Charset.forName("UTF-8")));
	        return new ServletInputStream() {

	            @Override
	            public int read() throws IOException {
	                return bais.read();
	            }

	            @Override
	            public boolean isFinished() {
	                return false;
	            }

	            @Override
	            public boolean isReady() {
	                return false;
	            }

	            @Override
	            public void setReadListener(ReadListener readListener) {

	            }
	        };
	    }
	    
	}
	 public static String getBodyString(ServletRequest request) {
	        StringBuilder sb = new StringBuilder();
	        InputStream inputStream = null;
	        BufferedReader reader = null;
	        try {
	            inputStream = request.getInputStream();
	            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line);
	            }
	        } catch (IOException e) {
	        } finally {
	            if (inputStream != null) {
	                try {
	                    inputStream.close();
	                } catch (IOException e) {
	                }
	            }
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e) {
	                }
	            }
	        }
	        return sb.toString();
	    }
}
