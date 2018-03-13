package org.xson.tangyuan.es.datasource;

import java.util.Map;
import java.util.Map.Entry;

public class EsSourceManager {

	private static EsSourceVo				defaultVo		= null;
	private static Map<String, EsSourceVo>	esSourceVoMap	= null;

	public static void setEsSourceVoMap(EsSourceVo essVo, Map<String, EsSourceVo> voMap) {
		if (null != essVo) {
			defaultVo = essVo;
		} else {
			esSourceVoMap = voMap;
		}
	}

	public static String getDefaultEsKey() {
		if (null != defaultVo) {
			return defaultVo.getId();
		}
		return null;
	}

	public static boolean isValidEsKey(String esKey) {
		if (null != defaultVo) {
			return defaultVo.getId().equals(esKey);
		}
		return esSourceVoMap.containsKey(esKey);
	}

	public static EsSourceVo getEsSource(String esKey) {
		if (null != defaultVo) {
			return defaultVo;
		}
		return esSourceVoMap.get(esKey);
	}

	public static void start() throws Throwable {
		if (null != defaultVo) {
			defaultVo.start();
		} else {
			for (Entry<String, EsSourceVo> entry : esSourceVoMap.entrySet()) {
				entry.getValue().start();
			}
		}
	}

	public static void stop() {
		if (null != defaultVo) {
			defaultVo.stop();
		} else {
			for (Entry<String, EsSourceVo> entry : esSourceVoMap.entrySet()) {
				entry.getValue().stop();
			}
		}
	}
}
