/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.lxcmanager;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class should be extended by all lxc placement strategies
 *
 * @author dilshat
 */
public abstract class LxcPlacementStrategy {

    private final Map<Agent, Map<String, Integer>> placementInfoMap = new HashMap<Agent, Map<String, Integer>>();

    /**
     * Optional method to implement for calculating total number of lxc slots
     * each physical server can accommodate
     *
     * @param serverMetrics - metrics from all connected physical servers
     * @return map where key is a physical agent and value is a number of lxcs
     * this physical server can accommodate
     */
    public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> serverMetrics) {
        return null;
    }

    /**
     * This method calculates placement of lxcs on physical servers. Code should
     * check passed server metrics to figure out strategy for lxc placement This
     * is done by calling addPlacementInfo method.This method calculates on
     * which physical server to places lxc, the number of lxcs to place and
     * their type
     *
     * @param serverMetrics - map where key is a physical agent and value is a
     *                      metric
     * @throws LxcCreateException
     */
    public abstract void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException;

    public final void addPlacementInfo(Agent physicalNode, String nodeType, int numberOfLxcsToCreate) throws LxcCreateException {
        if (physicalNode == null) {
            throw new LxcCreateException("Physical node is null");
        }
        if (Strings.isNullOrEmpty(nodeType)) {
            throw new LxcCreateException("Node type is null or empty");
        }
        if (numberOfLxcsToCreate <= 0) {
            throw new LxcCreateException("Number of lxcs must be greater than 0");
        }

        Map<String, Integer> placementInfo = placementInfoMap.get(physicalNode);
        if (placementInfo == null) {
            placementInfo = new HashMap<String, Integer>();
            placementInfoMap.put(physicalNode, placementInfo);
        }

        placementInfo.put(nodeType, numberOfLxcsToCreate);
    }

    /**
     * Returns placement map
     *
     * @return map where key is a physical server and value is a map where key
     * is type of node and value is a number of lxcs to place on this server
     */
    public Map<Agent, Map<String, Integer>> getPlacementInfoMap() {
        return Collections.unmodifiableMap(placementInfoMap);
    }

}
