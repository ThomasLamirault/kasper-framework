// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.domain.sample.root.api.query;

import com.viadeo.kasper.api.annotation.XKasperQueryResult;
import com.viadeo.kasper.api.component.query.CollectionQueryResult;
import com.viadeo.kasper.api.component.query.Query;
import com.viadeo.kasper.core.component.query.AutowiredQueryHandler;
import com.viadeo.kasper.core.component.query.annotation.XKasperQueryHandler;
import com.viadeo.kasper.domain.sample.root.api.Facebook;

@XKasperQueryHandler(domain=Facebook.class)
public class GetAllMembersQueryHandler extends AutowiredQueryHandler<GetAllMembersQueryHandler.GetAllMembersQuery, GetAllMembersQueryHandler.AllMembersResult> {

	public static class GetAllMembersQuery implements Query {
		private static final long serialVersionUID = -6513893864054353478L;
	}
	
	@XKasperQueryResult
	public static class AllMembersResult extends CollectionQueryResult<GetMembersQueryHandler.MembersResult> {
		private static final long serialVersionUID = -2174693040511999516L;
	}

}