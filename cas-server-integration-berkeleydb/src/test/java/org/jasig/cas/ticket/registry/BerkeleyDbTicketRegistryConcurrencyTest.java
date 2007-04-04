package org.jasig.cas.ticket.registry;

import java.io.File;
import java.util.Collection;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.BerkeleyDbTicketRegistry;
import org.jasig.cas.ticket.support.TimeoutExpirationPolicy;

import com.clarkware.junitperf.LoadTest;

public class BerkeleyDbTicketRegistryConcurrencyTest extends TestCase {

    static BerkeleyDbTicketRegistry registry;

    public BerkeleyDbTicketRegistryConcurrencyTest(String name) {
        super(name);
    }

    public static Test suite() {
        BerkeleyDbTicketRegistryConcurrencyTest testCase = new BerkeleyDbTicketRegistryConcurrencyTest(
            "testBasicFunctionality");

        return new BerkleyDbTicketRegistryTestSetup(new LoadTest(testCase, 100));
    }

    public void testBasicFunctionality() throws Exception {

        Ticket originalTicket = generateRandomTicket();
        String id = originalTicket.getId();
        long createTime = originalTicket.getCreationTime();

        registry.addTicket(originalTicket);

        Ticket retrievedTicket = registry.getTicket(id);

        assertEquals(originalTicket, retrievedTicket);
        assertEquals(id, retrievedTicket.getId());
        assertEquals(createTime, retrievedTicket.getCreationTime());

        Collection allTickets = registry.getTickets();
        assertTrue(allTickets.contains(originalTicket));

    }

    private TicketGrantingTicketImpl generateRandomTicket() {
        final String id = RandomStringUtils.randomAlphanumeric(20);
        final SimplePrincipal principal = new SimplePrincipal(id);

        TicketGrantingTicketImpl ticket = new TicketGrantingTicketImpl(id,
            new ImmutableAuthentication(principal),
            new TimeoutExpirationPolicy(500));

        return ticket;
    }

    private static class BerkleyDbTicketRegistryTestSetup extends TestSetup {

        public BerkleyDbTicketRegistryTestSetup(Test test) {
            super(test);
        }

        protected void setUp() throws Exception {
            super.setUp();
            registry = new BerkeleyDbTicketRegistry();
            registry.afterPropertiesSet();
        }

        protected void tearDown() throws Exception {
            registry.destroy();
            registry = null;
            new File("00000000.jdb").delete();
            new File("je.lck").delete();
            super.tearDown();
        }
    }
}
