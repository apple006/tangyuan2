package org.xson.tangyuan.web.xml.vo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.apply.CacheUseVo;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.tangyuan.web.DataConverter;
import org.xson.tangyuan.web.RequestContext;
import org.xson.tangyuan.web.RequestContext.RequestTypeEnum;
import org.xson.tangyuan.web.util.ServletUtils;

public class ControllerVo {

	protected String				url;
	protected String				transfer;
	protected String				validate;
	protected MethodObject			execMethod;
	protected List<MethodObject>	assemblyMethods;
	protected List<MethodObject>	beforeMethods;
	protected List<MethodObject>	afterMethods;

	// 权限设置: 用户可自行处理
	protected String				permission;
	protected CacheUseVo			cacheUse;
	protected boolean				cacheInAop;

	// 数据转换器
	protected DataConverter			dataConverter;
	protected RequestTypeEnum		requestType;

	public ControllerVo(String url, RequestTypeEnum requestType, String transfer, String validate, MethodObject execMethod,
			List<MethodObject> assemblyMethods, List<MethodObject> beforeMethods, List<MethodObject> afterMethods, String permission,
			CacheUseVo cacheUse, DataConverter dataConverter, boolean cacheInAop) {
		this.url = url;
		this.requestType = requestType;
		this.transfer = transfer;
		this.validate = validate;
		this.execMethod = execMethod;
		this.assemblyMethods = assemblyMethods;
		this.beforeMethods = beforeMethods;
		this.afterMethods = afterMethods;

		this.permission = permission;
		this.cacheUse = cacheUse;
		this.dataConverter = dataConverter;
		this.cacheInAop = cacheInAop;
	}

	public String getUrl() {
		return url;
	}

	public String getTransfer() {
		return transfer;
	}

	public String getValidate() {
		return validate;
	}

	public String getPermission() {
		return permission;
	}

	public CacheUseVo getCacheUse() {
		return cacheUse;
	}

	public boolean isCacheInAop() {
		return cacheInAop;
	}

	public boolean existDataConverter() {
		return null == this.dataConverter ? false : true;
	}

	public RequestTypeEnum getRequestType() {
		return requestType;
	}

	public void dataConvert(RequestContext context) throws Throwable {
		if (null != this.dataConverter) {
			this.dataConverter.convert(context, this);
			return;
		}

		DataConverter tempConverter = ServletUtils.getDefaultDataConverter(this, context.getContextType());
		if (null != tempConverter) {
			tempConverter.convert(context, this);
		}
	}

	public void assembly(RequestContext context) throws Throwable {
		if (null != this.assemblyMethods) {
			for (MethodObject mo : this.assemblyMethods) {
				mo.getMethod().invoke(mo.getInstance(), context);
			}
		}
	}

	public void before(RequestContext context) throws Throwable {
		try {
			if (null != this.beforeMethods) {
				for (MethodObject mo : this.beforeMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	public void after(RequestContext context) throws Throwable {
		try {
			if (null != this.afterMethods) {
				for (MethodObject mo : this.afterMethods) {
					mo.getMethod().invoke(mo.getInstance(), context);
				}
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	public void exec(RequestContext context) throws Throwable {
		try {
			if (null != this.execMethod) {
				Object retObj = this.execMethod.getMethod().invoke(this.execMethod.getInstance(), context);
				if (null == context.getResult() && null != retObj) {
					context.setResult(TangYuanUtil.retObjToXco(retObj));
				}
			} else {
				XCO request = (XCO) context.getArg();
				if (null == request) {
					request = new XCO();
				}
				XCO result = null;
				// if (WebComponent.getInstance().isRemoteServiceMode()) {
				// result = RpcProxy.call(transfer, request);
				// } else {
				// Object retObj = ServiceActuator.execute(transfer, request);
				// result = TangYuanUtil.retObjToXco(retObj);
				// }
				// 统一服务调用
				Object retObj = ServiceActuator.execute(transfer, request);
				result = TangYuanUtil.retObjToXco(retObj);
				context.setResult(result);
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw ((InvocationTargetException) e).getTargetException();
			}
			throw e;
		}
	}

	public boolean cacheGet(RequestContext context) throws Throwable {
		if (null != cacheUse) {
			Object result = cacheUse.getObject(context.getArg());
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}
		return false;
	}

	public void cachePut(RequestContext context) throws Throwable {
		if (null != cacheUse) {
			cacheUse.putObject(context.getArg(), context.getResult());
		}
	}

}
