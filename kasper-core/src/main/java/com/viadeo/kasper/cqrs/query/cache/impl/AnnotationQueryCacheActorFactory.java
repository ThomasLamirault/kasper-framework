// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.query.cache.impl;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import com.viadeo.kasper.cqrs.query.Query;
import com.viadeo.kasper.cqrs.query.QueryResult;
import com.viadeo.kasper.cqrs.query.QueryResponse;
import com.viadeo.kasper.cqrs.query.QueryHandler;
import com.viadeo.kasper.cqrs.query.annotation.XKasperQueryCache;
import com.viadeo.kasper.cqrs.query.annotation.XKasperQueryHandler;
import com.viadeo.kasper.cqrs.query.cache.QueryCacheKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class AnnotationQueryCacheActorFactory<QUERY extends Query, RESULT extends QueryResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationQueryCacheActorFactory.class);

    private CacheManager cacheManager;

    // ------------------------------------------------------------------------

    public AnnotationQueryCacheActorFactory() {
        // uses the default configured cache manager
        try {
            this.cacheManager = Caching.getCacheManager();
        } catch (final IllegalStateException ise) {
            LOGGER.info("No cache manager available, if you want to enable cache support please provide an implementation of JCache - jsr 107.");
        }
    }

    public AnnotationQueryCacheActorFactory(final CacheManager cacheManager) {
        this.cacheManager = checkNotNull(cacheManager);
    }

    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <QUERY extends Query, RESULT extends QueryResult> Optional<QueryCacheActor<QUERY, RESULT>> make(
            final Class<QUERY> queryClass,
            final Class<? extends QueryHandler<QUERY, RESULT>> queryHandlerClass) {

        if (null != cacheManager) {
            final XKasperQueryHandler queryHandlerAnnotation =
                    queryHandlerClass.getAnnotation(XKasperQueryHandler.class);

            if (null != queryHandlerAnnotation) {
                final XKasperQueryCache kasperQueryCache = queryHandlerAnnotation.cache();

                if (kasperQueryCache.enabled()) {
                    final Cache<Serializable, QueryResponse<RESULT>> cache =
                            cacheManager.<Serializable, QueryResponse<RESULT>>
                             createCacheBuilder(queryClass.getName())
                            .setStoreByValue(false)
                            .setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                    new CacheConfiguration.Duration(
                                            TimeUnit.SECONDS,
                                            kasperQueryCache.ttl()
                                    )
                            )
                            .build();

                    return Optional.of(
                            new QueryCacheActor<>(
                                    kasperQueryCache,
                                    cache,
                                    createKeyGenerator(
                                            queryClass,
                                            (Class<? extends QueryCacheKeyGenerator<QUERY>>)
                                                    kasperQueryCache.keyGenerator()
                                    )
                            )
                    );
                }
            }
        }

        return Optional.absent();
    }

    // ------------------------------------------------------------------------

    private <QUERY extends Query> QueryCacheKeyGenerator<QUERY> createKeyGenerator(
            final Class<QUERY> queryClass,
            final Class<? extends QueryCacheKeyGenerator<QUERY>> keyGenClass) {

        try {

            final TypeToken typeOfQuery = TypeToken
                    .of(keyGenClass)
                    .getSupertype(QueryCacheKeyGenerator.class)
                    .resolveType(
                            QueryCacheKeyGenerator.class.getTypeParameters()[0]
                    );

            if (!typeOfQuery.getRawType().isAssignableFrom(queryClass)) {
                throw new IllegalStateException(
                        String.format("Type %s in %s is not assignable from %s",
                            typeOfQuery.getRawType().getName(),
                            keyGenClass.getName(),
                            queryClass.getName()));
            }

            return keyGenClass.newInstance();

        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
