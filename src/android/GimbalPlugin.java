/*
 Copyright 2009-2015 Urban Airship Inc. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.urbanairship.cordova.gimbal;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;

import com.gimbal.android.Gimbal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GimbalPlugin extends Service {
	
	static final String GIMBAL_KEY = "com.urbanairship.gimbal_api_key";
	static final String UA_PREFIX = "com.urbanairship";
	
	private PluginConfig pluginConfig;
	
	@Override
    public void onCreate(){
		Context context = UAirship.getApplicationContext();
		PluginConfig pluginConfig = getPluginConfig(context);
		
		String gimbalKey = pluginConfig.getString(GIMBAL_KEY, "");
		if (gimbalKey.equals("")){
			Logger.error("No Gimbal API key found, Gimbal cordova plugin initialization failed.");
			return;
		}
		Logger.info("Initializing Urban Airship Gimbal cordova plugin.");
		
		Gimbal.setApiKey(this.getApplication(), gimbalKey);
		
		GimbalAdapter.getInstance().startAdapter();
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }
	
	/**
     * Gets the config for the Urban Airship plugin.
     *
     * @param context The application context.
     * @return The plugin config.
     */
    public PluginConfig getPluginConfig(Context context) {
        if (pluginConfig == null) {
            pluginConfig = new PluginConfig(context);
        }

        return pluginConfig;
    }
	
	/**
     * Helper class to parse the Urban Airship plugin config from the Cordova config.xml file.
     */
    class PluginConfig {
        private Map<String, String> configValues = new HashMap<String, String>();

        /**
         * Constructor for the PluginConfig.
         * @param context The application context.
         */
        PluginConfig(Context context) {
            parseConfig(context);
        }

        /**
         * Gets a String value from the config.
         *
         * @param key The config key.
         * @param defaultValue Default value if the key does not exist.
         * @return The value of the config, or default value.
         */
        String getString(String key, String defaultValue) {
            return configValues.containsKey(key) ? configValues.get(key) : defaultValue;
        }

        /**
         * Gets a Boolean value from the config.
         *
         * @param key The config key.
         * @param defaultValue Default value if the key does not exist.
         * @return The value of the config, or default value.
         */
        boolean getBoolean(String key, boolean defaultValue) {
            return configValues.containsKey(key) ?
                   Boolean.parseBoolean(configValues.get(key)) : defaultValue;
        }

        /**
         * Parses the config.xml file.
         * @param context The application context.
         */
        private void parseConfig(Context context) {
            int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
            if (id == 0) {
                return;
            }

            XmlResourceParser xml = context.getResources().getXml(id);

            int eventType = -1;
            while (eventType != XmlResourceParser.END_DOCUMENT) {

                if (eventType == XmlResourceParser.START_TAG) {
                    if (xml.getName().equals("preference")) {
                        String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.US);
                        String value = xml.getAttributeValue(null, "value");

                        if (name.startsWith(UA_PREFIX) && value != null) {
                            configValues.put(name, value);
                            Logger.verbose("Found " + name + " in config.xml with value: " + value);
                        }
                    }
                }

                try {
                    eventType = xml.next();
                } catch (Exception e) {
                    Logger.error("Error parsing config file", e);
                }
            }
        }

    }
}