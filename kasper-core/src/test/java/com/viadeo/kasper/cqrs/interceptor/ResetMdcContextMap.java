// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.interceptor;

import java.util.Collections;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.MDC;

import com.google.common.base.Objects;

public class ResetMdcContextMap implements TestRule {
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Map original = Objects.firstNonNull(MDC.getCopyOfContextMap(), Collections.emptyMap());
                try {
                    base.evaluate();
                } finally {
                    MDC.setContextMap(original);
                }
            }
        };
    }
}
