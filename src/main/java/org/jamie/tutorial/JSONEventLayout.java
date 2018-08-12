package org.jamie.tutorial;

/**
 * Created by hai on 2018/7/1.
 */
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.UUID;

public class JSONEventLayout extends Layout {
    private final static int VERSION = 1;

    private boolean locationInfo = false;
    private String serviceId;
    private boolean ignoreThrowable = false;
    private boolean activeIgnoreThrowable = this.ignoreThrowable;
    private LocationInfo info;

    private JSONObject jsonEvent;

    public JSONEventLayout() {
        this(true);
    }

    public JSONEventLayout(boolean locationInfo) {
        this.locationInfo = locationInfo;
        this.jsonEvent = new JSONObject();
    }

    @Override
    public String format(LoggingEvent loggingEvent) {
        if (this.jsonEvent == null) {
            this.jsonEvent = new JSONObject();
        } else {
            this.jsonEvent.clear();
        }

        this.jsonEvent.put(LogFields.VERSION, VERSION);
        this.jsonEvent.put(LogFields.UUID, UUID.randomUUID().toString());
        this.jsonEvent.put(LogFields.TIMESTAMP, System.currentTimeMillis());
        this.jsonEvent.put(LogFields.SERVICE_ID, this.serviceId);
        this.jsonEvent.put(LogFields.MESSAGE, loggingEvent.getRenderedMessage());
        this.jsonEvent.put(LogFields.LEVEL, loggingEvent.level.toString());
        this.jsonEvent.put(LogFields.THREAD_NAME, loggingEvent.getThreadName());
        this.jsonEvent.put(LogFields.THREAD_ID, Long.valueOf(Thread.currentThread().getId()));
        this.jsonEvent.put(LogFields.IP, NetworkUtils.getLocalIpV4());
        this.jsonEvent.put(LogFields.HOST_NAME, NetworkUtils.getHostName());

        if (loggingEvent.getThrowableInformation() != null) {
            ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
            if (throwableInformation.getThrowable().getClass().getCanonicalName() != null) {
                this.jsonEvent.put(LogFields.EXCEPTION_CLASS, throwableInformation.getThrowable().getClass().getCanonicalName());
            }
            if (throwableInformation.getThrowable().getMessage() != null) {
                this.jsonEvent.put(LogFields.EXCEPTION_MESSAGE, throwableInformation.getThrowable().getMessage());
            }
            if (throwableInformation.getThrowableStrRep() != null) {
                String stackTrace = String.join("\n", throwableInformation.getThrowableStrRep());
                this.jsonEvent.put(LogFields.EXCEPTION_STACK, stackTrace);
            }
        }

        if (this.locationInfo) {
            this.info = loggingEvent.getLocationInformation();
            this.jsonEvent.put(LogFields.LINE, this.info.getLineNumber());
            this.jsonEvent.put(LogFields.CLAZZ, this.info.getClassName());
            this.jsonEvent.put(LogFields.METHOD, this.info.getMethodName());
        }

        return this.jsonEvent.toString() + "\n";
    }


    @Override
    public boolean ignoresThrowable() {
        return this.ignoreThrowable;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void activateOptions() {
        this.activeIgnoreThrowable = this.ignoreThrowable;
    }
}
