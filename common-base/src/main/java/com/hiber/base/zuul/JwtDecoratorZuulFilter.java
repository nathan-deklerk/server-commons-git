package com.hiber.base.zuul;

import com.hiber.base.security.InternalSecurityGenerator;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper.IGNORED_HEADERS;

/**
 * Filter for adding header "Authorization" with jwt "Bearer ..." value.
 */
@RequiredArgsConstructor
public class JwtDecoratorZuulFilter extends ZuulFilter {
	private final InternalSecurityGenerator internalSecurityGenerator;

	@Override
	public String filterType() {
		return FilterConstants.ROUTE_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.PRE_DECORATION_FILTER_ORDER;
	}

	@Override
	public boolean shouldFilter() {
		return SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
				!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
	}

	@Override
	public Object run() {
		setAuthorizationHeader();
		removeAuthorizationHeaderFromIgnored();
		return null;
	}

	private void setAuthorizationHeader() {
		RequestContext ctx = RequestContext.getCurrentContext();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String token = internalSecurityGenerator.createInternalJwtAccessToken(authentication);
		ctx.getZuulRequestHeaders().put("Authorization", "Bearer " + token);
	}

	private void removeAuthorizationHeaderFromIgnored() {
		// There is not good place to add own 'Authorization' header and at the same time keep 'Authorization' header as
		// sensitive, so any incoming header is removed. In spring-cloud-netflix-zuul 2.1.0 ProxyRequestHelper run by
		// SimpleHostRoutingFilter removes sensitive/ignored headers but the request is send shortly after. The workaround
		// is to remove 'Authorization header from ignored list when we are adding the header explicitly. An alternative
		// would be to a) remove 'Authorization' from the list of sensitive headers or b) use other header, e.g.
		// 'X-Authorize', but this in turn requires changes in other modules.
		RequestContext ctx = RequestContext.getCurrentContext();
		Object ignoredHeadersObject = ctx.get(IGNORED_HEADERS);
		if (ignoredHeadersObject instanceof Collection)
			((Collection) ignoredHeadersObject).remove("authorization");
	}
}
