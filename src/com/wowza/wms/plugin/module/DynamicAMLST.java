package com.wowza.wms.plugin.module;

import com.wowza.wms.medialist.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.*;
import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.*;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class DynamicAMLST extends ModuleBase {
	private IApplicationInstance appIns = null;
	class MyMediaListProvider implements IMediaListProvider
	{
		public MediaList resolveMediaList(IMediaListReader mediaListReader, IMediaStream stream, String streamName)
		{
			List<String> resolutions = new ArrayList<String>();
			
			resolutions.add(".mp4");
			resolutions.add("_720p.mp4");
			resolutions.add("_1080p.mp4");
			
			streamName = streamName.substring(0, streamName.lastIndexOf('.'));
			MediaList mediaList = new MediaList();
			MediaListSegment segment = new MediaListSegment();
			mediaList.addSegment(segment);
			
			Iterator<String> iter = resolutions.iterator(); 
			while(iter.hasNext())
			{
				String res = iter.next(); 
				ICodecInfoRetrieve info = getInfo(streamName + res, stream);
				if(info != null)
				{
					MediaListRendition rendition = new MediaListRendition();
					segment.addRendition(rendition);
					rendition.setName("mp4:" + streamName + res);
					rendition.setBitrateAudio(info.getAudioBitrate());
					rendition.setBitrateVideo(info.getVideoBitrate());
					
					rendition.setWidth(info.getVideoCodec().getFrameHeight());
					rendition.setHeight(info.getVideoCodec().getFrameWidth());
					rendition.setAudioCodecId(info.getAudioCodec().toCodecsStr());
					rendition.setVideoCodecId(info.getVideoCodec().toCodecsStr());
				}
			}

			return mediaList;
		}
	}

	public void onAppStart(IApplicationInstance appInstance)
	{
		getLogger().info("onAppStart: DynamicAMLST");
		this.appIns = appInstance;
		appInstance.setMediaListProvider(new MyMediaListProvider());
	}
	
	public void onAppStop(IApplicationInstance appInstance)
	{
		getLogger().info("onAppStop: DynamicAMLST");
	}
	
	public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}

	public void onStreamCreate(IMediaStream stream) {
		getLogger().info("onStreamCreate: " + stream.getSrc());
	}

	public void onStreamDestroy(IMediaStream stream) {
		getLogger().info("onStreamDestroy: " + stream.getSrc());
	}

	public void onHTTPSessionCreate(IHTTPStreamerSession httpSession) {
		getLogger().info("onHTTPSessionCreate: " + httpSession.getSessionId());
	}

	public ICodecInfoRetrieve getInfo(String streamName, IMediaStream stream)
	{
		getLogger().debug("stream: " + streamName);
		
		ICodecInfoRetrieve thisRetrieve = new FileCodecRetrieve(this.appIns, stream, streamName);
		thisRetrieve.open();
		if (thisRetrieve.getCompleted() == true)
		{
			return thisRetrieve;
		}
		return null;
	}
}
