## DynamicAMLST

DynamicAMLST is a Wowza Streaming Engine plugin, based on http://thewowza.guru/how-to-get-file-information-codecbitrate 

It generates M3U8 playlists for HLS VOD delivery of pre-rendered mp4 files with different quality levels. 

A base stream name has to be specified, e.g. http://localhost:1935/vod/amlst:sample.mp4/playlist.m3u8

Pre-rendered files (e.g. sample_720p.mp4, sample_1080p.mp4) should be placed in the content directory and will be added with their codec info to the resulting playlist dynamically:

```
#EXTM3U
#EXT-X-VERSION:3
#EXT-X-STREAM-INF:BANDWIDTH=2259904,CODECS="avc1.100.30,mp4a.40.2",RESOLUTION=640x360
chunklist_w573994235_b2259904.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=4328269,CODECS="avc1.100.40,mp4a.40.2",RESOLUTION=1920x1080
chunklist_w573994235_b4328269.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=3295056,CODECS="avc1.100.31,mp4a.40.2",RESOLUTION=1280x720
chunklist_w573994235_b3295056.m3u8
```


## Setup

Add to the Modules section of your Application.xml:

```
<Module>
  <Name>DynamicAMLST</Name>
  <Description>DynamicAMLST</Description>
  <Class>com.wowza.wms.plugin.module.DynamicAMLST</Class>
</Module>
```
