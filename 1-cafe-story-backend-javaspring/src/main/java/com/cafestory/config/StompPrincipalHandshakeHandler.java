package com.cafestory.config;

import com.cafestory.until.security.JwtAuthenticationFilter;
import com.cafestory.until.security.JwtClaims;
import com.cafestory.until.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Arrays;
import java.util.Map;

/**
 * Gắn {@link Principal} cho mỗi phiên STOMP, lấy từ JWT trong cookie của
 * request handshake.
 *
 * <p>Không có nó thì mọi phiên socket đều ẩn danh, và
 * {@code SimpMessagingTemplate.convertAndSendToUser(userId, ...)} không tra ra
 * được phiên nào thuộc userId đó — message bị loại bỏ IM LẶNG, không log, không
 * lỗi. Đó là lý do thông báo duyệt/từ chối bài chỉ hiện sau khi reload: bản ghi
 * đã lưu DB nhưng frame realtime không bao giờ tới. Ack {@code message_sent} của
 * chat cũng rơi vì cùng lý do.
 *
 * <p>{@code getName()} phải trả về đúng {@code userId.toString()} vì đó là khoá
 * mà các lời gọi {@code convertAndSendToUser} trong dự án đang dùng.
 *
 * <p>Handshake thiếu hoặc sai token vẫn được cho qua dưới dạng ẩn danh: các
 * destination {@code /topic/**} (chat theo cuộc trò chuyện) không cần định danh,
 * chặn ở đây sẽ làm hỏng những trang đang mở socket khi chưa đăng nhập.
 */
@Component
public class StompPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    private static final Logger log = LoggerFactory.getLogger(StompPrincipalHandshakeHandler.class);

    private final JwtService jwtService;

    public StompPrincipalHandshakeHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String token = readAccessToken(request);
        if (token == null) {
            return null;
        }
        try {
            JwtClaims claims = jwtService.validateAccessToken(token);
            if (claims.userId() == null) {
                return null;
            }
            return new StompUserPrincipal(claims.userId().toString());
        } catch (RuntimeException ex) {
            log.debug("Handshake WebSocket mang token không hợp lệ, coi như ẩn danh: {}", ex.getMessage());
            return null;
        }
    }

    private String readAccessToken(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return null;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    /** Principal tối giản: chỉ cần {@code getName()} để định tuyến user destination. */
    private record StompUserPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
