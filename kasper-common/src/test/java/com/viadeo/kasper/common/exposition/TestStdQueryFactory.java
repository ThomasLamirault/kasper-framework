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
// KASPER - Kasper is the treasure keeper
// www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
// Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.common.exposition;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.viadeo.kasper.api.component.query.Query;
import com.viadeo.kasper.common.exposition.adapters.DefaultTypeAdapters;
import com.viadeo.kasper.common.exposition.adapters.NullSafeTypeAdapter;
import com.viadeo.kasper.common.exposition.adapters.TypeAdapterFactory;
import com.viadeo.kasper.common.exposition.query.*;
import org.joda.time.DateTime;
import org.junit.Test;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

public class TestStdQueryFactory {

    public static class SomeQuery implements Query {
        private static final long serialVersionUID = -6763165103363988454L;

        public int getDummy() { return 1; }

        public void setDummy(final int dummyInt) { }

    }

    // ------------------------------------------------------------------------

    @Test
    public void testSkipNull() throws Exception {
        new NullSafeTypeAdapter<SomeQuery>(create()).adapt(null, new QueryBuilder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithoutSkipNull() throws Exception {
        create().adapt(null, null);
    }

    @Test
    public void testCustomQueryAdapterResolution() {
        final TypeAdapter<SomeQuery> adapter = create();
        assertEquals(adapter, createQueryFactory(adapter).create(TypeToken.of(SomeQuery.class)));
    }

    @Test
    public void testCustomQueryAdapterOutput() throws Exception {

        // Given
        final TypeAdapter<SomeQuery> adapter = create();
        final QueryBuilder builder = new QueryBuilder();

        // When
        createQueryFactory(adapter).create(TypeToken.of(SomeQuery.class)).adapt(new SomeQuery(), builder);

        // Then
        assertEquals("bar", builder.first("foo"));
    }

    @Test
    public void testBeanQueryAdapterOutputWithPrimitiveIntAdapter() throws Exception {

        // Given
        final QueryBuilder builder = new QueryBuilder();
        final TypeAdapter<SomeQuery> adapter = new DefaultQueryFactory(
                new FeatureConfiguration(),
                ImmutableMap.<Type, TypeAdapter>of(int.class, DefaultTypeAdapters.INT_ADAPTER),
                ImmutableMap.<Type, BeanAdapter>of(),
                new ArrayList<TypeAdapterFactory>(),
                VisibilityFilter.PACKAGE_PUBLIC).create(TypeToken.of(SomeQuery.class));

        // When
        adapter.adapt(new SomeQuery(), builder);

        // Then
        assertEquals("1", builder.first("dummy"));
    }

    @Test
    public void testQueryFactoryOutputWithCollectionAdapter() throws Exception {

        // Given
        final QueryBuilder builder = new QueryBuilder();
        final DateTime firstDate = new DateTime();
        final DateTime secondDate = new DateTime().plusDays(1);
        final QueryOfDateTimeCollection query = new QueryOfDateTimeCollection(Arrays.asList(firstDate, secondDate));
        final QueryFactory queryFactory = createQueryFactory(DefaultTypeAdapters.COLLECTION_ADAPTER_FACTORY, DefaultTypeAdapters.DATETIME_ADAPTER);

        // When
        queryFactory.create(TypeToken.of(QueryOfDateTimeCollection.class)).adapt(query, builder);

        // Then
        final Iterator<String> it = builder.values("listOfDateTime").iterator();
        assertEquals("" + firstDate.getMillis(), it.next());
        assertEquals("" + secondDate.getMillis(), it.next());
    }

    @Test
    public void testCustomTypeAdapterFactoryWithDeepGenerics() throws Exception {

        // Given
        final QueryBuilder builder = new QueryBuilder();
        final String key1 = "key1";
        final String key2 = "key2";
        final List<DateTime> key1Values = Arrays.asList(new DateTime(), new DateTime().plusDays(1));
        final QueryWithMap query = new QueryWithMap(ImmutableMap.of(key1, key1Values, key2, new ArrayList<DateTime>()));
        final QueryFactory factory = createQueryFactory(createTypeAdapterFactory(), DefaultTypeAdapters.COLLECTION_ADAPTER_FACTORY, DefaultTypeAdapters.DATETIME_ADAPTER);

        // When
        factory.create(TypeToken.of(QueryWithMap.class)).adapt(query, builder);

        // Then
        assertFalse(builder.has(key2));
        assertTrue(builder.has(key1));

        final Collection<String> builderValues = builder.values(key1);
        assertEquals(2, builderValues.size());

        final Iterator<DateTime> itDateTime = key1Values.iterator();
        final Iterator<String> itKey1 = builderValues.iterator();
        while (itDateTime.hasNext()) {
            assertEquals(
                    String.valueOf(itDateTime.next().getMillis()),
                    itKey1.next()
            );
        }
    }

    @Test
    public void beanQueryMapper_shouldAlphabeticallySortFields() throws Exception {
        // Given
        final QueryBuilder builder = new QueryBuilder();
        final TypeAdapter<SomeQueryWithUnsortedFields> adapter = new DefaultQueryFactory(
                new FeatureConfiguration(),
                ImmutableMap.<Type, TypeAdapter>of(int.class, DefaultTypeAdapters.INT_ADAPTER),
                ImmutableMap.<Type, BeanAdapter>of(),
                new ArrayList<TypeAdapterFactory>(),
                VisibilityFilter.PACKAGE_PUBLIC).create(TypeToken.of(SomeQueryWithUnsortedFields.class)
        );

        // When
        adapter.adapt(new SomeQueryWithUnsortedFields(), builder);

        // Then
        // We want to test order is deterministic. Testing 20 times.
        for (int i = 0; i < 20; ++i) {
            assertEquals(
                    new URI("http://test.com/test?aaa=2&bbb=1").toASCIIString(),
                    builder.build(new URI("http://test.com/test")).toASCIIString()
            );
        }
    }

    // ========================================================================

    public static class SomeQueryWithUnsortedFields implements Query {
        public int getBbb() { return 1; }

        public void setBbb(final int dummyInt) { }

        public int getAaa() { return 2; }

        public void setAaa(final int dummyInt) { }

    }

    private TypeAdapterFactory<Map<String, List<DateTime>>> createTypeAdapterFactory() {
        return new TypeAdapterFactory<Map<String, List<DateTime>>>() {
            @Override
            public Optional<TypeAdapter<Map<String, List<DateTime>>>> create(TypeToken<Map<String, List<DateTime>>> typeToken, final QueryFactory adapterFactory) {
                @SuppressWarnings("serial")
                final TypeAdapter<List<DateTime>> dateTimeListAdapter = adapterFactory.create(
                        new TypeToken<List<DateTime>>() { }
                );

                final TypeAdapter<Map<String, List<DateTime>>> adapter = new TypeAdapter<Map<String, List<DateTime>>>() {
                    @Override
                    public void adapt(final Map<String, List<DateTime>> value, final QueryBuilder builder) throws Exception {
                        for (final Map.Entry<String, List<DateTime>> entry : value.entrySet()) {
                            builder.begin(entry.getKey());
                            dateTimeListAdapter.adapt(entry.getValue(), builder);
                            builder.end();
                        }
                    }

                    @Override
                    public Map<String, List<DateTime>> adapt(QueryParser parser) {
                        return null;
                    }
                };
                return Optional.of(adapter);
            }
        };
    }

    private QueryFactory createQueryFactory(final Object... queryFactoryParameters) {
        final Map<Type, TypeAdapter> adaptersMap = new HashMap<Type, TypeAdapter>();
        final List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>();

        for (final Object parameter : queryFactoryParameters) {
            if (parameter instanceof TypeAdapter) {
                final TypeAdapter adapter = (TypeAdapter) parameter;
                final TypeToken adapterForType = TypeToken.of(adapter.getClass()).resolveType(TypeAdapter.class.getTypeParameters()[0]);
                adaptersMap.put(adapterForType.getType(), adapter);
            } else if (parameter instanceof TypeAdapterFactory) {
                factories.add((TypeAdapterFactory) parameter);
            } else {
                throw new IllegalArgumentException("Only TypeAdapter or TypeAdapter factories are allowed.");
            }
        }

        return new DefaultQueryFactory(
                new FeatureConfiguration(),
                adaptersMap,
                ImmutableMap.<Type, BeanAdapter>of(),
                factories,
                VisibilityFilter.PACKAGE_PUBLIC
        );
    }

    private TypeAdapter<SomeQuery> create() {
        return new TypeAdapter<SomeQuery>() {
            @Override
            public void adapt(final SomeQuery value, final QueryBuilder builder) {
                if (null == value) {
                    throw new IllegalArgumentException();
                }
                builder.addSingle("foo", "bar");
            }

            @Override
            public SomeQuery adapt(final QueryParser parser) {
                return null;
            }
        };
    }

    public static class QueryWithMap implements Query {
        private static final long serialVersionUID = 1914912257262499643L;
        private final Map<String, List<DateTime>> mapOfDateTime;

        public QueryWithMap(final Map<String, List<DateTime>> mapOfDateTime) {
            this.mapOfDateTime = ImmutableMap.copyOf(mapOfDateTime);
        }

        public Map<String, List<DateTime>> getMapOfDateTime() {
            return this.mapOfDateTime;
        }
    }

    public static class QueryOfDateTimeCollection implements Query {
        private static final long serialVersionUID = -6933354147082294343L;
        private final List<DateTime> listOfDateTime;

        public QueryOfDateTimeCollection(final List<DateTime> listOfDateTime) {
            this.listOfDateTime = listOfDateTime;
        }

        public List<DateTime> getListOfDateTime() {
            return this.listOfDateTime;
        }
    }

}
