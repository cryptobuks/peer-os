package org.safehaus.kiskis.mgmt.server.persistence;

import com.datastax.driver.core.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:57 PM
 */
public class Persistence implements PersistenceInterface {

    private static final Logger LOG = Logger.getLogger(Persistence.class.getName());
    private Cluster cluster;
    private Session session;
    private long requestsequencenumber = 0l;
    private String cassandraHost;
    private String cassandraKeyspace;
    private int cassandraPort;

    public void setCassandraKeyspace(String cassandraKeyspace) {
        this.cassandraKeyspace = cassandraKeyspace;
    }

    public void setCassandraHost(String cassandraHost) {
        this.cassandraHost = cassandraHost;
    }

    public void setCassandraPort(int cassandraPort) {
        this.cassandraPort = cassandraPort;
    }

    @Override
    public List<Agent> getAgentList() {
        List<Agent> list = new ArrayList<Agent>();
        ResultSet rs = session.execute("select * from agents");
        Iterator<Row> it = rs.iterator();
        while (it.hasNext()) {
            Agent agent = new Agent();
            Row row = it.next();
            agent.setUuid(row.getString("uuid"));
            agent.setHostname(row.getString("hostname"));
            agent.setLXC(row.getBool("islxc"));
            agent.setListIP(row.getList("listip", String.class));
            agent.setMacAddress(row.getString("macaddress"));
            list.add(agent);
        }
        return list;
    }

    /**
     * Saved Agent data into Cassandra agents table
     *
     * @param agent
     * @return the result in boolean
     */
    @Override
    public boolean saveAgent(Agent agent) {
        String cql = "insert into agents (uuid, hostname, islxc, listip, macaddress) "
                + "values (?,?,?,?,?)";
        PreparedStatement stmt = session.prepare(cql);
        BoundStatement boundStatement = new BoundStatement(stmt);
        ResultSet rs = session.execute(boundStatement.bind(agent.getUuid(),
                agent.getHostname(), agent.isLXC(), agent.getListIP(), agent.getMacAddress()));
        return rs != null;
    }

    // TODO Remove this method and reference in blueprint
    public void init() {
        System.out.println("Persistence initialized");
        cluster = Cluster.builder().withPort(cassandraPort).addContactPoint(cassandraHost).build();
        session = cluster.connect(cassandraKeyspace);
    }

    public void destroy() {
        try {
            session.shutdown();
        } catch (Exception e) {
        }
        try {
            cluster.shutdown();
        } catch (Exception e) {
        }
        System.out.println(this.getClass().getName() + " stopped");
    }

    public List<Command> getCommandList(Agent agent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Saves command into cassandra
     *
     * @param command
     * @return boolean result
     */
    @Override
    public boolean saveCommand(Command command) {
        String cql = "insert into request (source, requestsequencenumber, type, uuid, "
                + "workingdirectory, program, outputredirectionstdout, outputredirectionstderr, "
                + "stdoutpath, erroutpath, runsas, args, environment, pid, timeout) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement stmt = session.prepare(cql);
        BoundStatement boundStatement = new BoundStatement(stmt);

        Request request = command.getCommand();
        System.out.println(request);
        ResultSet rs = session.execute(boundStatement.bind(request.getSource(), requestsequencenumber++,
                request.getType().toString(), request.getUuid(), request.getWorkingDirectory(),
                request.getProgram(), request.getStdOut().toString(), request.getStdErr().toString(),
                request.getStdOutPath(), request.getStdErrPath(), request.getRunAs(), request.getArgs(),
                request.getEnvironment(), request.getPid(), request.getTimeout()));
        return rs != null;
    }

    /**
     * Saves response to cassandra
     *
     * @param response
     * @return boolean result
     */
    @Override
    public boolean saveResponse(Response response) {
        String cql = "insert into response (uuid, responsesequencenumber, exitcode, errout, hostname, ips, "
                + "macaddress, pid, requestsequencenumber, responsetype, source, stdout) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        PreparedStatement stmt = session.prepare(cql);
        BoundStatement boundStatement = new BoundStatement(stmt);
        ResultSet rs = session.execute(boundStatement.bind(response.getUuid(),
                (Long) response.getResponseSequenceNumber() == null ? -1l : response.getResponseSequenceNumber(),
                response.getExitCode(), response.getStdOut(), response.getHostname(), response.getIps(),
                response.getMacAddress(), response.getPid(), response.getRequestSequenceNumber(), response.getType().toString(),
                response.getSource(), response.getStdOut()));
        return rs != null;
    }

    @Override
    public List<Response> getResponses() {
        throw new UnsupportedOperationException("not done yet");
    }

    public boolean updateAgent(Agent agent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
