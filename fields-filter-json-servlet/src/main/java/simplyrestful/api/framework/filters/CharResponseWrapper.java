package simplyrestful.api.framework.filters;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CharResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter output;

    public String toString() {
	return output.toString();
    }

    public CharResponseWrapper(HttpServletResponse response) {
	super(response);
	output = new CharArrayWriter();
    }

    @Override
    public PrintWriter getWriter() {
	return new PrintWriter(output);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
	return new ServletOutputStream() {
	    private OutputStream outputStream = new ByteArrayOutputStream(1024);
	    @Override
	    public void write(int b) throws IOException {
		outputStream.write(b);
		output.write(b);
	    }

	    @Override
	    public void setWriteListener(WriteListener writeListener) { }

	    @Override
	    public boolean isReady() {
		return false;
	    }
	};
    }
}
