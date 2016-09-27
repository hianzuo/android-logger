package com.hianzuo.logger;

import com.hianzuo.logger.IDeleteLogCallback;

interface ILogService {
	boolean path(in String path,in String prefix);
	boolean append(in String line);
	boolean flush();
	boolean delete(in int beforeDay);
	boolean splitTime(long time);
	void deleteAll(in IDeleteLogCallback callback);
}