package pablocos.gestor_biblioteca.kinal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Compatibilidad con clientes (por ejemplo curl en la consola de Windows) que envian el JSON
 * en Windows-1252 en lugar de UTF-8: si el cuerpo no es UTF-8 valido, se transcodifica a UTF-8
 * antes de que Jackson lo lea. Si ya es UTF-8 valido, pasa sin cambios.
 */
@Component
public class JsonBodyEncodingFilter extends OncePerRequestFilter {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final int MAX_BYTES = 1_048_576;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String metodo = request.getMethod();
        boolean conCuerpo = "POST".equals(metodo) || "PUT".equals(metodo) || "PATCH".equals(metodo);
        String tipo = request.getContentType();
        boolean json = tipo != null && tipo.toLowerCase(Locale.ROOT).contains("json");
        return !(conCuerpo && json);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        byte[] original = request.getInputStream().readNBytes(MAX_BYTES + 1);
        if (original.length > MAX_BYTES) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        byte[] utf8 = esUtf8Valido(original)
                ? original
                : new String(original, WINDOWS_1252).getBytes(StandardCharsets.UTF_8);
        filterChain.doFilter(new CuerpoRequest(request, utf8), response);
    }

    private boolean esUtf8Valido(byte[] datos) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(datos));
            return true;
        } catch (CharacterCodingException ex) {
            return false;
        }
    }

    /** Request que entrega el cuerpo ya leido (y normalizado a UTF-8). */
    private static final class CuerpoRequest extends HttpServletRequestWrapper {

        private final byte[] cuerpo;

        CuerpoRequest(HttpServletRequest request, byte[] cuerpo) {
            super(request);
            this.cuerpo = cuerpo;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream origen = new ByteArrayInputStream(cuerpo);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return origen.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("Lectura asincrona no soportada");
                }

                @Override
                public int read() {
                    return origen.read();
                }

                @Override
                public int read(byte[] destino, int desde, int longitud) {
                    return origen.read(destino, desde, longitud);
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return cuerpo.length;
        }

        @Override
        public long getContentLengthLong() {
            return cuerpo.length;
        }

        @Override
        public String getCharacterEncoding() {
            return "UTF-8";
        }
    }
}
