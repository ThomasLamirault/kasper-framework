package com.viadeo.kasper.test.applications.entities;

import com.viadeo.kasper.IDomain;
import com.viadeo.kasper.IKasperID;
import com.viadeo.kasper.er.IRootConcept;
import com.viadeo.kasper.er.annotation.XKasperConcept;
import com.viadeo.kasper.locators.IDomainLocator;
import com.viadeo.kasper.test.applications.Applications;
import org.axonframework.domain.DomainEventStream;
import org.axonframework.domain.EventRegistrationCallback;
import org.joda.time.DateTime;

@XKasperConcept(domain = Applications.class, label = Application.NAME)
public class Application implements IRootConcept {
	private static final long serialVersionUID = 7663957891087399105L;

	public static final String NAME = "Application";
	
	@Override
	public IDomain getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomainLocator(IDomainLocator domainLocator) {
		// TODO Auto-generated method stub

	}

	@Override
	public <I extends IKasperID> I getEntityId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime getCreationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime getModificationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializeState(DomainEventStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEventRegistrationCallback(EventRegistrationCallback arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commitEvents() {
		// TODO Auto-generated method stub

	}

	@Override
	public IKasperID getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getUncommittedEventCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DomainEventStream getUncommittedEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

}
