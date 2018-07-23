package org.xson.tangyuan.rpc;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * RPC代理服务
 */
public class RpcServiceNode extends AbstractServiceNode {

	public RpcServiceNode(String serviceURI) {
		this.serviceKey = serviceURI;
		this.serviceType = TangYuanServiceType.PRCPROXY;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		//		Object result = RpcProxy.call(serviceKey, (XCO) arg);
		//		context.setResult(result);
		//		return true;

		context.addTrackingHeader(arg);
		try {
			Object result = RpcProxy.call(serviceKey, (XCO) arg);
			context.setResult(result);
			return true;
		} catch (Throwable e) {
			throw e;
		} finally {
			context.cleanTrackingHeader(arg);
		}
	}

}
