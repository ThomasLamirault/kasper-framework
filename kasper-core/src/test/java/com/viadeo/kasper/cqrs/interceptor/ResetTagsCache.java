// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.interceptor;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Set;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ResetTagsCache implements TestRule {
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Map<Class<?>, Set<String>> original = newHashMap(TagsHolder.CACHE_TAGS);
                try {
                    base.evaluate();
                } finally {
                    TagsHolder.CACHE_TAGS.clear();
                    TagsHolder.CACHE_TAGS.putAll(original);
                }
            }
        };
    }
}
