package fi.bitrite.android.ws.host.impl;

import fi.bitrite.android.ws.model.HostBriefInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: johannes
 * Date: 08.01.2013
 */
public class MapSearchJsonParserTest {

    @Rule
    public ResourceFile incomplete = new ResourceFile("map_search_incomplete.json");

    @Rule
    public ResourceFile sixHosts = new ResourceFile("map_search_six_hosts.json");

    @Rule
    public ResourceFile singleHost = new ResourceFile("map_search_single_host.json");

    @Rule
    public ResourceFile unknownHost = new ResourceFile("map_search_unknown.json");

    @Rule
    public ResourceFile hostWithStreet = new ResourceFile("map_search_host_with_street.json");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testIncomplete() throws Exception {
        exception.expect(IncompleteResultsException.class);
        MapSearchJsonParser parser = new MapSearchJsonParser(incomplete.getContent(), 1);
        List<HostBriefInfo> hosts = parser.getHosts();
    }

    @Test
    public void hostsCutoff() throws Exception {
        exception.expect(TooManyHostsException.class);
        MapSearchJsonParser parser = new MapSearchJsonParser(sixHosts.getContent(), 5);
        List<HostBriefInfo> hosts = parser.getHosts();
    }

    @Test
    public void testSingleHost() throws Exception {
        MapSearchJsonParser parser = new MapSearchJsonParser(singleHost.getContent(), 1);
        List<HostBriefInfo> hosts = parser.getHosts();

        assertEquals(1, hosts.size());
        HostBriefInfo host = hosts.get(0);
        assertEquals(18496, host.getId());
        assertEquals("Johannes Staffans", host.getFullname());
        assertEquals("Helsinki, 00650, FI", host.getLocation());
        assertEquals("60.184443", host.getLatitude());
        assertEquals("25.006599", host.getLongitude());
    }

    @Test
    public void testUnknownHost() throws Exception {
        MapSearchJsonParser parser = new MapSearchJsonParser(unknownHost.getContent(), 1);
        List<HostBriefInfo> hosts = parser.getHosts();
        HostBriefInfo host = hosts.get(0);
        assertEquals("(Unknown host)", host.getFullname());
    }


    @Test
    public void testStreet() throws Exception {
        MapSearchJsonParser parser = new MapSearchJsonParser(hostWithStreet.getContent(), 1);
        List<HostBriefInfo> hosts = parser.getHosts();
        HostBriefInfo host = hosts.get(0);
        assertEquals("Street 1", host.getLocation());
    }
}
