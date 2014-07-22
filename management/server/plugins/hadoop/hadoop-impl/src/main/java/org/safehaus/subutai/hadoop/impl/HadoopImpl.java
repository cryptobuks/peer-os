package org.safehaus.subutai.hadoop.impl;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.lxcmanager.LxcManager;
import org.safehaus.subutai.api.networkmanager.NetworkManager;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.hadoop.api.Config;
import org.safehaus.subutai.hadoop.api.Hadoop;
import org.safehaus.subutai.hadoop.impl.operation.Adding;
import org.safehaus.subutai.hadoop.impl.operation.Deletion;
import org.safehaus.subutai.hadoop.impl.operation.Installation;
import org.safehaus.subutai.hadoop.impl.operation.configuration.*;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daralbaev on 02.04.14.
 */
public class HadoopImpl implements Hadoop {
	public static final String MODULE_NAME = "Hadoop";
	private static CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private LxcManager lxcManager;
	private NetworkManager networkManager;
	private ExecutorService executor;

	public void init() {
		executor = Executors.newCachedThreadPool();
	}

	public void destroy() {
		executor.shutdown();
		commandRunner = null;
	}

	public static CommandRunner getCommandRunner() {
		return commandRunner;
	}

	public void setCommandRunner(CommandRunner commandRunner) {
		HadoopImpl.commandRunner = commandRunner;
	}

	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}

	public void setDbManager(DbManager dbManager) {
		this.dbManager = dbManager;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public void setLxcManager(LxcManager lxcManager) {
		this.lxcManager = lxcManager;
	}

	public void setNetworkManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public DbManager getDbManager() {
		return dbManager;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public LxcManager getLxcManager() {
		return lxcManager;
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public AgentManager getAgentManager() {
		return agentManager;
	}

	@Override
	public UUID installCluster(final Config config) {
		return new Installation(this, config).execute();
	}

	@Override
	public UUID uninstallCluster(final String clusterName) {
		return new Deletion(this).execute(clusterName);
	}

	@Override
	public UUID startNameNode(Config config) {
		return new NameNode(this, config).start();
	}

	@Override
	public UUID stopNameNode(Config config) {
		return new NameNode(this, config).stop();
	}

	@Override
	public UUID restartNameNode(Config config) {
		return new NameNode(this, config).restart();
	}

	@Override
	public UUID statusNameNode(Config config) {
		return new NameNode(this, config).status();
	}

	@Override
	public UUID statusSecondaryNameNode(Config config) {
		return new SecondaryNameNode(this, config).status();
	}

	@Override
	public UUID statusDataNode(Agent agent) {
		return new DataNode(this, null).status(agent);
	}

	@Override
	public UUID startJobTracker(Config config) {
		return new JobTracker(this, config).start();
	}

	@Override
	public UUID stopJobTracker(Config config) {
		return new JobTracker(this, config).stop();
	}

	@Override
	public UUID restartJobTracker(Config config) {
		return new JobTracker(this, config).restart();
	}

	@Override
	public UUID statusJobTracker(Config config) {
		return new JobTracker(this, config).status();
	}

	@Override
	public UUID statusTaskTracker(Agent agent) {
		return new TaskTracker(this, null).status(agent);
	}

	@Override
	public UUID addNode(String clusterName) {
		return new Adding(this, clusterName).execute();
	}

	@Override
	public UUID blockDataNode(Config config, Agent agent) {
		return new DataNode(this, config).block(agent);
	}

	@Override
	public UUID blockTaskTracker(Config config, Agent agent) {
		return new TaskTracker(this, config).block(agent);
	}

	@Override
	public UUID unblockDataNode(Config config, Agent agent) {
		return new DataNode(this, config).unblock(agent);
	}

	@Override
	public UUID unblockTaskTracker(Config config, Agent agent) {
		return new TaskTracker(this, config).unblock(agent);
	}

	@Override
	public List<Config> getClusters() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}
}
