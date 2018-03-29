package com.hianzuo.logger;

import com.hianzuo.logger.IDeleteLogCallback;

interface ILogService {
	boolean config(in String path,in String prefix,in int flushCount,in int maxCacheCount);
	boolean path(in String path,in String prefix);
	boolean append(in String line);
	boolean appendLines(in List<String> lines);
	boolean flush();
	boolean delete(in int beforeDay);
	boolean splitTime(long time);
	void deleteAll(in IDeleteLogCallback callback);
}