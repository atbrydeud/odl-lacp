/*
 *  * * Copyright (c) 2014 Dell Inc. and others.  All rights reserved.
 *   * This program and the accompanying materials are made available under the
 *    * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *     * and is available at http://www.eclipse.org/legal/epl-v10.html
 *      *
 *       */

package org.opendaylight.lacp.core;

import java.util.Date;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
 
import org.opendaylight.yang.gen.v1.urn.opendaylight.lacp.packet.rev150210.LacpPacketPdu;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lacp.packet.rev150210.LacpPacketPduBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lacp.packet.rev150210.lacp.packet.field.ActorInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lacp.packet.rev150210.lacp.packet.field.ActorInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lacp.packet.rev150210.lacp.packet.field.PartnerInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.lacp.packet.rev150210.lacp.packet.field.PartnerInfoBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.lacp.Utils.*;
import org.opendaylight.lacp.util.LacpUtil;
import org.opendaylight.lacp.queue.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.math.BigInteger;


public class LacpBpduInfo implements LacpPDUPortStatusContainer {
	
	private static final Logger log = LoggerFactory.getLogger(LacpBpduInfo.class);
	static final int LACP_BPDU_TYPE= 0;
	static final int LACP_MARK_REQUEST = 1;
	static final int LACP_MARK_RESPONSE = 2;
	
	private long swId;
	private short portId;
	private int type;
	private LacpBpduSysInfo actorSystemInfo;
	private LacpBpduSysInfo partnerSystemInfo;
	//private LacpBpduMarkInfo markInfo;
	private short collectorMaxDelay;
	private Date receivedDate;
	private NodeConnectorRef nodeConnRef;
	/*
	public MacAddress srcAddr;
	public MacAddress destAddr;
	
	public MacAddress getSrcAddr(){
		return srcAddr;
	}
	public MacAddress getDestAddr(){
		return destAddr;
	}
	*/
        public MessageType getMessageType(){
         	return LacpPDUPortStatusContainer.MessageType.LACP_PDU_MSG;
        }

	public LacpBpduInfo(long swId,short portId, LacpBpduSysInfo actor, LacpBpduSysInfo partner, short delay) {
		super();
		this.type = LACP_BPDU_TYPE;
		this.swId = swId;
		this.portId = portId;
		actorSystemInfo = new LacpBpduSysInfo(actor);
		partnerSystemInfo = new LacpBpduSysInfo(partner);
		this.collectorMaxDelay = delay;
		receivedDate = new Date();
		nodeConnRef = null;
		/*
		srcAddr = null;
		destAddr = null;
		*/
	}

	public LacpBpduInfo() {
		super();
		this.swId = 0;
		this.portId = 0;
		this.type = LACP_BPDU_TYPE;
		actorSystemInfo = new LacpBpduSysInfo();
		partnerSystemInfo = new LacpBpduSysInfo();
		//markInfo = new LacpBpduMarkInfo();
		this.collectorMaxDelay = 0;
		receivedDate = new Date();
		nodeConnRef = null;
		/*
		srcAddr = null;
		destAddr = null;
		*/
	}

	public LacpBpduInfo(LacpPacketPdu pktPdu){
		System.out.println("Entering In LacpBpduInfo");
		nodeConnRef = pktPdu.getIngressPort();
		if(nodeConnRef == null){
			log.error("LacpBpduInfo constructor, nodeConnRef is null");
		}
		/*
		srcAddr = pktPdu.getSrcAddress();
		destAddr = pktPdu.getDestAddress();
		*/
		this.swId=NodePort.getSwitchId(nodeConnRef);
		this.portId=NodePort.getPortId(nodeConnRef);
		this.type = LACP_BPDU_TYPE;
		setActorInfoFromPkt(pktPdu.getActorInfo());
		setPartnerInfoFromPkt(pktPdu.getPartnerInfo());
		this.setCollectorMaxDelay((pktPdu.getCollectorMaxDelay()).shortValue());
		receivedDate = new Date();
		log.info("In LacpBpduInfo constructor -  after converting LacpPacketPdu to LacpBpduInfo the values are = {}", this.toString());

		System.out.println("Exiting In LacpBpduInfo");
        }

	public NodeConnectorRef getNCRef(){
		return nodeConnRef;
	}

	public void setActorInfoFromPkt(ActorInfo actInfo){
		//int len = 6;
		//byte[] nodeSysAddr = new byte[len];
		final byte[] nodeSysAddr;
		short portNum = actInfo.getPort().shortValue();
		byte  portState = (byte)actInfo.getState().shortValue();
		short portPri = actInfo.getPortPriority().shortValue();
		short nodeKey = actInfo.getKey().shortValue();
		//convertStringtoByte(macToString(new String (actInfo.getSystemId()));
		System.out.println("Entering- setActorInfoFromPkt");
		//nodeSysAddr = Arrays.copyOf(HexEncode.bytesFromHexString(actInfo.getSystemId().toString()), LacpConst.ETH_ADDR_LEN);
		System.out.println("actInfo.getSystemId  " + actInfo.getSystemId());
		System.out.println("actInfo.getSystemId  .getValue()" + actInfo.getSystemId().getValue());
		nodeSysAddr = HexEncode.bytesFromHexString(actInfo.getSystemId().getValue());
		System.out.println("LacpBpduInfo - setActorInfoFromPkt - The nodeSysAddr is :" + HexEncode.bytesToHexString(nodeSysAddr));
		log.info("LacpBpduInfo - setActorInfoFromPkt - The nodeSysAddr is = {}" , HexEncode.bytesToHexString(nodeSysAddr));
		short sysPri =  actInfo.getSystemPriority().shortValue();
                actorSystemInfo = new LacpBpduSysInfo(sysPri, nodeSysAddr, nodeKey, portPri, portNum, portState);
	}

	public void setPartnerInfoFromPkt(PartnerInfo partInfo){
		//int len = 6;
		//byte[] nodeSysAddr = new byte[len];
		final byte[] nodeSysAddr;
		short portNum = partInfo.getPort().shortValue();
		byte  portState = (byte)partInfo.getState().shortValue();
		short portPri = partInfo.getPortPriority().shortValue();
		short nodeKey = partInfo.getKey().shortValue();
//		nodeSysAddr = Arrays.copyOf(HexEncode.bytesFromHexString((partInfo.getSystemId().getValue())), LacpConst.ETH_ADDR_LEN);
		nodeSysAddr = HexEncode.bytesFromHexString((partInfo.getSystemId().getValue()));
		System.out.println("LacpBpduInfo - setPartnerInfoFromPkt - The nodeSysAddr is :" + HexEncode.bytesToHexString(nodeSysAddr));
		log.info("LacpBpduInfo - setPartnerInfoFromPkt - The nodeSysAddr is = {}" , HexEncode.bytesToHexString(nodeSysAddr));
		short sysPri =  partInfo.getSystemPriority().shortValue();
                partnerSystemInfo = new LacpBpduSysInfo(sysPri, nodeSysAddr, nodeKey, portPri, portNum, portState);
	}

	/*
	public LacpBpduInfo(long swId,short portId, LacpBpduMarkInfo markInfo) {
		super();
		this.type = LACP_MARK_REQUEST;
		this.swId = swId;
		this.portId = portId;
		actorSystemInfo = new LacpBpduSysInfo();
		partnerSystemInfo = new LacpBpduSysInfo();
		this.markInfo = new LacpBpduMarkInfo(markInfo);
		this.collectorMaxDelay = 0;
		receivedDate = new Date();
	}
	*/
	
	public long getSwId() {
		return swId;
	}


	public void setSwId(long swId) {
		this.swId = swId;
	}


	public short getPortId() {
		return portId;
	}


	public void setPortId(short portId) {
		this.portId = portId;
	}


	public LacpBpduSysInfo getActorSystemInfo() {
		return actorSystemInfo;
	}


	public void setActorSystemInfo(LacpBpduSysInfo actorSystemInfo) {
		this.actorSystemInfo = actorSystemInfo;
	}


	public LacpBpduSysInfo getPartnerSystemInfo() {
		return partnerSystemInfo;
	}


	public void setPartnerSystemInfo(LacpBpduSysInfo partnerSystemInfo) {
		this.partnerSystemInfo = partnerSystemInfo;
	}


	public short getCollectorMaxDelay() {
		return collectorMaxDelay;
	}


	public void setCollectorMaxDelay(short collectorMaxDelay) {
		this.collectorMaxDelay = collectorMaxDelay;
	}




	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/*
	public LacpBpduMarkInfo getMarkInfo() {
		return markInfo;
	}

	public void setMarkInfo(LacpBpduMarkInfo markInfo) {
		this.markInfo = markInfo;
	}
	*/

	@Override
	public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof LacpBpduInfo))
            return false;
        LacpBpduInfo other = (LacpBpduInfo) obj;
        if (this.swId != other.swId)
        	return false;
        if (this.portId != other.portId)
        	return false;
        if (this.type != other.type)
        	return false;
        if (!this.actorSystemInfo.equals(other.actorSystemInfo))
        	return false;
        if (!this.partnerSystemInfo.equals(other.partnerSystemInfo))
        	return false;
	/*
        if (!this.markInfo.equals(other.markInfo))
        	return false;
	*/
        if (this.collectorMaxDelay != other.collectorMaxDelay)
        	return false;
        if (!this.receivedDate.equals(other.receivedDate))
        	return false;
        return true;	
	}


	@Override
	public int hashCode() {
		/* Update Prime Number */
        final int prime = 1121;
        int result = super.hashCode();
        result = prime * result + (int) this.swId;
        result = prime * result + this.portId;
        result = prime * result + this.actorSystemInfo.hashCode();
        result = prime * result + this.partnerSystemInfo.hashCode();
        //result = prime * result * this.markInfo.hashCode();
        result = prime * result + this.collectorMaxDelay;
        result = prime * result + this.receivedDate.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "LacpBpduInfo [swId=" + (swId) + ", portId=" + String.format("%04x",portId) + ", type="
				+ String.format("%02x",type) + ", actorSystemInfo=" + actorSystemInfo
				+ ", partnerSystemInfo=" + partnerSystemInfo + ", collectorMaxDelay=" + String.format("%05x",collectorMaxDelay)
				+ ", receivedDate=" + receivedDate + "]";
	}
}