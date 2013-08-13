// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.test.core.boot;

import com.viadeo.kasper.core.boot.AnnotationProcessor;
import com.viadeo.kasper.core.boot.AnnotationRootProcessor;
import junit.framework.TestCase;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.mockito.Mockito.*;

public class KasperAnnotationRootProcessorTest extends TestCase {

    public static interface TestInterface { }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface XTestAnnotation { }

    @XTestAnnotation
    public static class TestClass implements TestInterface {}

    @XTestAnnotation
    public static final class TestChildClass extends TestClass { }

	// ------------------------------------------------------------------------
	// TEST SIMPLE WORKING PROCESSOR/CLASS SCANNING
	// ------------------------------------------------------------------------
	
	public static class TestProcessor implements AnnotationProcessor<XTestAnnotation, TestInterface> {

        @Override
        public boolean isAnnotationMandatory() {
            return true;
        }

		@Override
		public void process(final Class<?> clazz) {
			// Do nothing, we are just interested by execution
		}
		
	}
	
	// ------------------------------------------------------------------------
	
	@Test
	public void testShouldRunProcessOnNewRegisterProcessor() {
		final AnnotationRootProcessor rootProcessor = new AnnotationRootProcessor();
		
		final TestProcessor realProcessor = new TestProcessor();
		final AnnotationProcessor<?,?> processor = spy(realProcessor);
		
		rootProcessor.registerProcessor(processor);
		rootProcessor.setDoNotScanDefaultPrefix(true);
		rootProcessor.addScanPrefix(this.getClass().getPackage().getName());

		rootProcessor.boot();
		
		verify(processor).process(TestClass.class);
        verify(processor).process(TestChildClass.class);
	}

    @Test
    public void testShouldNotScanInPackageWithoutAnySetPrefix() {
        final AnnotationRootProcessor rootProcessor = new AnnotationRootProcessor();

        final TestProcessor realProcessor = new TestProcessor();
        final AnnotationProcessor<?,?> processor = spy(realProcessor);

        rootProcessor.registerProcessor(processor);
        rootProcessor.setDoNotScanDefaultPrefix(true);

        rootProcessor.boot();

        verify(processor,never()).process(TestClass.class);
    }
	
}
