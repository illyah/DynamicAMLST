package com.wowza.wms.plugin.module;

import com.wowza.wms.medialist.*;
import com.wowza.wms.module.*;
import com.wowza.wms.stream.*;
import com.wowza.util.HTTPUtils;
import com.wowza.wms.application.*;
import com.wowza.wms.httpstreamer.cupertinostreaming.file.HTTPStreamerCupertinoIndexFile;
import com.wowza.wms.httpstreamer.cupertinostreaming.file.IHTTPStreamerCupertinoIndex;
import com.wowza.wms.httpstreamer.cupertinostreaming.file.IMediaReaderCupertino;
import com.wowza.wms.httpstreamer.model.IHTTPStreamerSession;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class DynamicAMLST extends ModuleBase {
	
	private IApplicationInstance appIns = null;
	private IMediaReaderCupertino mediaReader = null; 
	public static final String PROPERTY_format = "format";
	
	class MyMediaListProvider implements IMediaListProvider
	{
		public MediaList resolveMediaList(IMediaListReader mediaListReader, IMediaStream stream, String streamName)
		{		
			getLogger().warn("DynamicAMLST.resolveMediaList");
			
			String formatStr = "default";
			IHTTPStreamerSession HTTPClient = null;
			
			try {HTTPClient = stream.getHTTPStreamerSession(); } catch (Exception client) {}
			
			if (HTTPClient != null) 
			{
				String queryStr = HTTPClient.getQueryStr();
				Map<String, String> queryParams = HTTPUtils.splitQueryStr(queryStr);
	
				String indexStr = PROPERTY_format;
				
				if (queryParams.containsKey(indexStr))
				{
					formatStr = queryParams.get(indexStr) != null ? queryParams.get(indexStr) : "default";
					if(formatStr.isEmpty()) { formatStr =  "default"; }
				}
			}
			
			List<String> renditions = getRenditions(streamName, formatStr);
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
					rendition.setName(res);
					rendition.setTitle("" + info.getCodecInfoVideo().getFrameHeight());
					rendition.setBitrateAudio(info.getAudioBitrate());
					rendition.setBitrateVideo(info.getVideoBitrateAverage());					
					rendition.setWidth(info.getCodecInfoVideo().getFrameWidth());
					rendition.setHeight(info.getCodecInfoVideo().getFrameHeight());
					rendition.setAudioCodecId(info.getCodecInfoAudio().toCodecsStr());
					rendition.setVideoCodecId(info.getCodecInfoVideo().toCodecsStr());
					segment.addRendition(rendition);		
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
	
	
	public List<String> getRenditions(String streamName, String formatStr)
	{	
		String baseName = streamName.substring(0, streamName.lastIndexOf('.'));
		String extension = streamName.substring(streamName.lastIndexOf('.'), streamName.length());
		
		if(formatStr != "default"  && !formatStr.isEmpty()) { baseName += "_" + formatStr; }
		
		List<String> files = new ArrayList<String>();
		File streamFile = new File(this.appIns.getStreamStorageDir() + "/" + streamName);

		if(streamFile.exists() && streamFile.isFile())
		{
			File storageDir = new File(this.appIns.getStreamStorageDir());
		
			for(File file: storageDir.listFiles())
			{	
				if(file.getName().startsWith(baseName) && file.getName().endsWith(extension))
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
