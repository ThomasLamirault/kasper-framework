// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.core.component.command.aggregate.ddd.specification.impl;

import com.viadeo.kasper.core.component.command.aggregate.ddd.specification.ISpecification;
import com.viadeo.kasper.core.component.command.aggregate.ddd.specification.SpecificationErrorMessage;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @param <T> Object class
 * 
 * @see EntitySpecification
 * @see com.viadeo.kasper.core.component.command.aggregate.ddd.Entity
 */
public class AndSpecification<T> extends AbstractCompositeSpecification<T> {

	public AndSpecification(final ISpecification<T> spec1, final ISpecification<T> spec2) {
		super(spec1, spec2);
	}

	// ----------------------------------------------------------------------

	/**
	 * @see Specification#isSatisfiedBy(Object)
	 */
	@Override
	public boolean isSatisfiedBy(final T object) {
		checkNotNull(object);
		return this.spec1.isSatisfiedBy(object) && this.spec2.isSatisfiedBy(object);
	}

	// ----------------------------------------------------------------------
	
	/**
	 * @see AbstractCompositeSpecification#isSatisfiedBy(Object, com.viadeo.kasper.core.component.command.aggregate.ddd.specification.SpecificationErrorMessage)
	 */
	@Override
	public boolean isSatisfiedBy(final T object, final SpecificationErrorMessage errorMessage) {
		
		final SpecificationErrorMessage errorMessage1 = new SpecificationErrorMessage();
		final boolean isSatisfied1 = this.spec1.isSatisfiedBy(object, errorMessage1);

		final SpecificationErrorMessage errorMessage2 = new SpecificationErrorMessage();
		final boolean isSatisfied2 = this.spec2.isSatisfiedBy(object, errorMessage2);

		if (isSatisfied1 && isSatisfied2) {
			return true;
		}
		
		final StringBuffer sb = new StringBuffer();
		if ( ! isSatisfied1) {
			sb.append(errorMessage1.getMessage()).append("\n");
		}
		if ( ! isSatisfied2) {
			sb.append(errorMessage2.getMessage());
		}
		errorMessage.setMessage(sb.toString());
		
		return false;
	}

}