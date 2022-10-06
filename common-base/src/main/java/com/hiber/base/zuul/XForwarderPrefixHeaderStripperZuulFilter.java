package com.hiber.base.zuul;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * Filter for removing header "X-Forwarded-Prefix" from {@link #locationPrefix} locations.
 */
@RequiredArgsConstructor
public class XForwarderPrefixHeaderStripperZuulFilter extends ZuulFilter {
	private final String locationPrefix;

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		// it should be after default decoration filter to clean "X-Forwarded-Prefix" header added in that filter
		return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRequest().getRequestURI() != null &&
				ctx.getRequest().getRequestURI().startsWith(locationPrefix);
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.getZuulRequestHeaders().remove(FilterConstants.X_FORWARDED_PREFIX_HEADER.toLowerCase());
		return null;
	}
}
