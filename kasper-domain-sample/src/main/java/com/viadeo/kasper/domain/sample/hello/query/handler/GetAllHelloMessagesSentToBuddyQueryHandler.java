// ----------------------------------------------------------------------------
//  This file is part of the Kasper framework.
//
//  The Kasper framework is free software: you can redistribute it and/or 
//  modify it under the terms of the GNU Lesser General Public License as 
//  published by the Free Software Foundation, either version 3 of the 
//  License, or (at your option) any later version.
//
//  Kasper framework is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with the framework Kasper.  
//  If not, see <http://www.gnu.org/licenses/>.
// --
//  Ce fichier fait partie du framework logiciel Kasper
//
//  Ce programme est un logiciel libre ; vous pouvez le redistribuer ou le 
//  modifier suivant les termes de la GNU Lesser General Public License telle 
//  que publiée par la Free Software Foundation ; soit la version 3 de la 
//  licence, soit (à votre gré) toute version ultérieure.
//
//  Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS 
//  AUCUNE GARANTIE ; sans même la garantie tacite de QUALITÉ MARCHANDE ou 
//  d'ADÉQUATION à UN BUT PARTICULIER. Consultez la GNU Lesser General Public 
//  License pour plus de détails.
//
//  Vous devez avoir reçu une copie de la GNU Lesser General Public License en 
//  même temps que ce programme ; si ce n'est pas le cas, consultez 
//  <http://www.gnu.org/licenses>
// ----------------------------------------------------------------------------
// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.domain.sample.hello.query.handler;

import com.google.common.collect.Lists;
import com.viadeo.kasper.api.component.query.QueryResponse;
import com.viadeo.kasper.api.exception.KasperQueryException;
import com.viadeo.kasper.api.id.KasperID;
import com.viadeo.kasper.core.component.query.AutowiredQueryHandler;
import com.viadeo.kasper.core.component.query.annotation.XKasperQueryFilter;
import com.viadeo.kasper.core.component.query.annotation.XKasperQueryHandler;
import com.viadeo.kasper.domain.sample.hello.api.HelloDomain;
import com.viadeo.kasper.domain.sample.hello.api.query.GetAllHelloMessagesSentToBuddyQuery;
import com.viadeo.kasper.domain.sample.hello.api.query.results.HelloMessageResult;
import com.viadeo.kasper.domain.sample.hello.api.query.results.HelloMessagesResult;
import com.viadeo.kasper.domain.sample.hello.common.db.HelloMessagesIndexStore;
import com.viadeo.kasper.domain.sample.hello.common.db.KeyValueStore;
import com.viadeo.kasper.domain.sample.hello.query.handler.adapters.NormalizeBuddyQueryInterceptor;

import java.util.Collection;
import java.util.Map;

/** Required annotation to define the sticked domain */
@XKasperQueryHandler(domain = HelloDomain.class)
/** Optional annotation to define which interceptors will be applied on each query before handling */
@XKasperQueryFilter({NormalizeBuddyQueryInterceptor.class})
public class GetAllHelloMessagesSentToBuddyQueryHandler
        extends AutowiredQueryHandler<GetAllHelloMessagesSentToBuddyQuery, HelloMessagesResult> {

    private KeyValueStore store = HelloMessagesIndexStore.db;

    @Override
    @SuppressWarnings("unchecked")
    public QueryResponse<HelloMessagesResult> handle(final GetAllHelloMessagesSentToBuddyQuery query) throws KasperQueryException {

        final String forBuddy = query.getForBuddy();
        Collection<HelloMessageResult> ret = Lists.newArrayList();

        if (store.has(forBuddy)) {
            /** Index directly contains the structure to be returned, no manipulation needed here */
            ret = ((Map<KasperID, HelloMessageResult>) store.get(forBuddy).get()).values();
        }

        return QueryResponse.of(new HelloMessagesResult(Lists.newArrayList(ret)));
    }

}
