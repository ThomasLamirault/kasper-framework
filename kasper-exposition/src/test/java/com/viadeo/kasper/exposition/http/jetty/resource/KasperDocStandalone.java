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
package com.viadeo.kasper.exposition.http.jetty.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.viadeo.kasper.api.component.event.Event;
import com.viadeo.kasper.core.component.event.saga.SagaIdReconciler;
import com.viadeo.kasper.core.component.event.saga.step.Scheduler;
import com.viadeo.kasper.core.component.event.saga.step.Steps;
import com.viadeo.kasper.core.component.event.saga.step.facet.SchedulingStep;
import com.viadeo.kasper.doc.element.DocumentedPlatform;
import com.viadeo.kasper.doc.initializer.DefaultDocumentedElementInitializer;
import com.viadeo.kasper.doc.web.ObjectMapperKasperResolver;
import com.viadeo.kasper.domain.sample.applications.api.Applications;
import com.viadeo.kasper.domain.sample.applications.api.event.ApplicationCreatedEvent;
import com.viadeo.kasper.domain.sample.applications.api.event.MemberHasDeclaredToBeFanOfAnApplicationEvent;
import com.viadeo.kasper.domain.sample.applications.command.listener.ApplicationCreatedEventListener;
import com.viadeo.kasper.domain.sample.root.api.Facebook;
import com.viadeo.kasper.domain.sample.root.api.command.AddConnectionToMemberCommand;
import com.viadeo.kasper.domain.sample.root.api.event.*;
import com.viadeo.kasper.domain.sample.root.api.query.GetAllMemberQueryHandler;
import com.viadeo.kasper.domain.sample.root.api.query.GetMemberQueryHandler;
import com.viadeo.kasper.domain.sample.root.api.query.GetMembersQueryHandler;
import com.viadeo.kasper.domain.sample.root.command.handler.AddConnectionToMemberHandler;
import com.viadeo.kasper.domain.sample.root.command.listener.MemberCreatedEventListener;
import com.viadeo.kasper.domain.sample.root.command.listener.NewFanOfAnApplicationEventListener;
import com.viadeo.kasper.domain.sample.root.command.model.entity.Member;
import com.viadeo.kasper.domain.sample.root.command.model.entity.Member_connectedTo_Member;
import com.viadeo.kasper.domain.sample.root.command.repository.MemberConnectionsRepository;
import com.viadeo.kasper.domain.sample.root.command.repository.MemberRepository;
import com.viadeo.kasper.domain.sample.root.command.saga.ConfirmEmailSaga;
import com.viadeo.kasper.domain.sample.timelines.api.Timelines;
import com.viadeo.kasper.platform.bundle.descriptor.*;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

public class KasperDocStandalone {

    public static void main(final String [] args) throws IOException, InterruptedException, NoSuchMethodException {
        final String baseUri = "http://localhost:9988/";

        final DomainDescriptor domainDescriptor = new DomainDescriptor(
                Facebook.NAME,
                Facebook.class,
                ImmutableList.<QueryHandlerDescriptor>of(
                        new QueryHandlerDescriptor(
                                GetMembersQueryHandler.class,
                                GetMembersQueryHandler.GetMembersQuery.class,
                                GetMembersQueryHandler.MembersResult.class),
                        new QueryHandlerDescriptor(
                                GetAllMemberQueryHandler.class,
                                GetAllMemberQueryHandler.GetAllMemberQuery.class,
                                GetAllMemberQueryHandler.AllMemberResult.class),
                        new QueryHandlerDescriptor(
                                GetMemberQueryHandler.class,
                                GetMemberQueryHandler.GetMemberQuery.class,
                                GetAllMemberQueryHandler.MemberResult.class)
                ),
                ImmutableList.<CommandHandlerDescriptor>of(new CommandHandlerDescriptor(
                        AddConnectionToMemberHandler.class,
                        AddConnectionToMemberCommand.class)
                ),
                ImmutableList.<RepositoryDescriptor>of(
                        new RepositoryDescriptor(
                                MemberRepository.class,
                                DomainDescriptorFactory.toAggregateDescriptor(Member.class)
                        ),
                        new RepositoryDescriptor(
                                MemberConnectionsRepository.class,
                                DomainDescriptorFactory.toAggregateDescriptor(Member_connectedTo_Member.class)
                        )
                ),
                ImmutableList.<EventListenerDescriptor>of(
                        new EventListenerDescriptor(MemberCreatedEventListener.class,MemberCreatedEvent.class),
                        new EventListenerDescriptor(MemberCreatedEventListener.class,MemberCreatedEvent.class),
                        new EventListenerDescriptor(NewFanOfAnApplicationEventListener.class,MemberHasDeclaredToBeFanOfAnApplicationEvent.class),
                        new EventListenerDescriptor(ApplicationCreatedEventListener.class,ApplicationCreatedEvent.class)
                ),
                Lists.<SagaDescriptor>newArrayList(
                        new SagaDescriptor(ConfirmEmailSaga.class, Lists.newArrayList(
                                new SagaDescriptor.StepDescriptor(
                                        "onMemberCreated",
                                        MemberCreatedEvent.class,
                                        new SchedulingStep(
                                                new Steps.StartStep(ConfirmEmailSaga.class.getMethod("onMemberCreated", MemberCreatedEvent.class), "getEntityId", mock(SagaIdReconciler.class)),
                                                new SchedulingStep.ScheduleOperation(mock(Scheduler.class), ConfirmEmailSaga.class, "notConfirmed", 60L, TimeUnit.MINUTES, false)
                                        ).getActions()
                                ),
                                new SagaDescriptor.StepDescriptor(
                                        "onConfirmedEvent",
                                        MemberHasConfirmedEmailEvent.class,
                                        new Steps.EndStep(ConfirmEmailSaga.class.getMethod("onConfirmedEvent", MemberHasConfirmedEmailEvent.class), "getId", mock(SagaIdReconciler.class)).getActions()
                                )
                        ))
                ),
                ImmutableList.<Class<? extends Event>>of(
                        FacebookEvent.class,
                        FacebookMemberEvent.class,
                        MemberCreatedEvent.class,
                        NewMemberConnectionEvent.class
                )
        );

        final DocumentedPlatform documentedPlatform = new DocumentedPlatform();
        documentedPlatform.registerDomain(Facebook.NAME, domainDescriptor);
        documentedPlatform.registerDomain(Applications.NAME, new DomainDescriptor(
                Applications.NAME,
                Applications.class,
                Lists.<QueryHandlerDescriptor>newArrayList(),
                ImmutableList.<CommandHandlerDescriptor>of(new CommandHandlerDescriptor(
                        AddConnectionToMemberHandler.class,
                        AddConnectionToMemberCommand.class)
                ),
                Lists.<RepositoryDescriptor>newArrayList(),
                Lists.<EventListenerDescriptor>newArrayList(
                        new EventListenerDescriptor(MemberCreatedEventListener.class,MemberCreatedEvent.class)
                ),
                Lists.<SagaDescriptor>newArrayList(),
                ImmutableList.<Class<? extends Event>>of(ApplicationCreatedEvent.class)
        ));
        documentedPlatform.registerDomain(Timelines.NAME, new DomainDescriptor(
                Timelines.NAME,
                Timelines.class,
                Lists.<QueryHandlerDescriptor>newArrayList(),
                Lists.<CommandHandlerDescriptor>newArrayList(),
                Lists.<RepositoryDescriptor>newArrayList(),
                Lists.<EventListenerDescriptor>newArrayList(),
                Lists.<SagaDescriptor>newArrayList(),
                Lists.<Class<? extends Event>>newArrayList()
        ));
        documentedPlatform.accept(new DefaultDocumentedElementInitializer(documentedPlatform));

        final KasperDocResource res = new KasperDocResource(documentedPlatform);

        final ResourceConfig rc = new PackagesResourceConfig("com.viadeo.kasper.test.doc.web");
        rc.getSingletons().add(res);
        rc.getProviderClasses().add(ObjectMapperKasperResolver.class);

        System.out.println("Starting grizzly...");

        final HttpServer server = GrizzlyServerFactory.createHttpServer(baseUri, rc);

        server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("src/main/resources/META-INF/resources/doc/"),"/doc");

        System.out.println(String.format("Try out %skasper/doc/domains \nAccess UI at %sdoc/index.html", baseUri, baseUri));

        System.in.read();

        server.stop();
        System.exit(0);
    }

}
