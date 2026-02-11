package com.example.ratelimiting.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserContextTest {

    @Test
    void shouldUseUserIdHeaderWhenPresent() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-USER-ID")).thenReturn("alice");

        var ctx = new UserContext(request);

        assertThat(ctx.getIdentifier()).isEqualTo("user:alice");
    }

    @Test
    void shouldIgnoreBlankUserIdAndFallToIp() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-USER-ID")).thenReturn("  ");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        var ctx = new UserContext(request);

        assertThat(ctx.getIdentifier()).isEqualTo("ip:10.0.0.1");
    }

    @Test
    void shouldFallBackToRemoteAddrWhenNoHeaders() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-USER-ID")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        var ctx = new UserContext(request);

        assertThat(ctx.getIdentifier()).isEqualTo("ip:192.168.1.100");
    }

    @Test
    void shouldUseFirstIpFromXForwardedForChain() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-USER-ID")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 70.41.3.18, 150.172.238.178");

        var ctx = new UserContext(request);

        assertThat(ctx.getIdentifier()).isEqualTo("ip:203.0.113.5");
    }

    @Test
    void shouldUseXForwardedForOverRemoteAddr() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-USER-ID")).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        var ctx = new UserContext(request);

        assertThat(ctx.getIdentifier()).isEqualTo("ip:1.2.3.4");
    }
}
