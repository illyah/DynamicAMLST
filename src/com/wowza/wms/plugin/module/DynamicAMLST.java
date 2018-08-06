package com.wowza.wms.plugin.module;

import com.wowza.wms.medialist.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.stream.*;
import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.application.*;
import com.wowza.wms.client.IClient;
import com.wowza.wms.httpstreamer.cupertinostreaming.file.HTTPStreamerCupertinoIndexFile;
import com.wowza.wms.httpstreamer.cupertinostreaming.file.IHTTPStreamerCupertinoIndex;
import com.wowza.wms.httpstreamer.cupertinostreaming.file.IMediaReaderCupertino;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class DynamicAMLST extends ModuleBase {
	
	private IApplicationInstance appIns = null;
	private IMediaReaderCupertino mediaReader = null;
	
	class MyMediaListProvider implements IMediaListProvider
	{
		public MediaList resolveMediaList(IMediaListReader mediaListReader, IMediaStream stream, String streamName)
		{
			List<String> renditions = getRenditions(streamName);
			MediaList mediaList = new MediaList();
			MediaListSegment segment = new MediaListSegment();
			mediaList.addSegment(segment);
			
			Iterator<String> iter = renditions.iterator(); 
			while(iter.hasNext())
			{
				String res = iter.next(); 
				IHTTPStreamerCupertinoIndex info = readMedia(stream, res);
				if(info != null)
				{
					MediaListRendition rendition = new MediaListRendition();
					segment.addRendition(rendition);
					rendition.setName("mp4:" + res);
					rendition.setTitle("" + info.getCodecInfoVideo().getFrameHeight());
					rendition.setBitrateAudio(info.getAudioBitrate());
					rendition.setBitrateVideo(info.getVideoBitrateAverage());					
					rendition.setWidth(info.getCodecInfoVideo().getFrameWidth());
					rendition.setHeight(info.getCodecInfoVideo().getFrameHeight());
					rendition.setAudioCodecId(info.getCodecInfoAudio().toCodecsStr());
					rendition.setVideoCodecId(info.getCodecInfoVideo().toCodecsStr());
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
	
	public List<String> getRenditions(String streamName)
	{
		String baseName = streamName.substring(0, streamName.lastIndexOf('.'));
		List<String> files = new ArrayList<String>();
		
		File streamFile = new File(this.appIns.getStreamStorageDir() + "/" + streamName);
		
		if(streamFile.exists() && streamFile.isFile())
		{
			File storageDir = new File(this.appIns.getStreamStorageDir());
		
			for(File file: storageDir.listFiles())
			{
				if(file.getName().startsWith(baseName))
				{
					files.add(file.getName());
				}
			}
		}
		return files;	
	}
	
	
	public IHTTPStreamerCupertinoIndex readMedia(IMediaStream stream, String streamName)
	{
		try
		{
			IMediaReader reader = MediaReaderFactory.getInstance(this.appIns, this.appIns.getVHost().getMediaReaders(), "mp4cupertino");
			if (reader != null)
			{
		    	String basePath = appIns.getStreamStoragePath();
		    	reader.init(this.appIns, stream, "mp4cupertino", basePath, streamName);
		    	reader.open(basePath, streamName);
		    	mediaReader = (IMediaReaderCupertino) reader;
			}
		}
		catch (Exception e)
		{
			getLogger().info("Broke it " + e.toString());
		}
					
		IHTTPStreamerCupertinoIndex indexer = null;
		indexer = new HTTPStreamerCupertinoIndexFile();
					
		mediaReader.indexFile(indexer);			
		mediaReader.close();

		return indexer;
	}
}
