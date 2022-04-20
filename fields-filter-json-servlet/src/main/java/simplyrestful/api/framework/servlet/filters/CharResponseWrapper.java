package simplyrestful.api.framework.servlet.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CharResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

    public String toString() {
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    public CharResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(outputStream);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public boolean isReady() {
                return false;
            }
        };
    }
}
