package com.movie.movie.controller;

import lombok.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/**
 * MediaPlayerController.
 *
 * @author legion
 * @version 5.0
 * @since 14/05/2023
 */
@NoArgsConstructor
@RestController
public class MediaPlayerController {

    @ResponseBody
    @GetMapping(value = "/play/media/v01/{vid}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ResourceRegion> playMediaV02(@PathVariable String vid,
                                                       @RequestHeader HttpHeaders headers) throws IOException {

        String videoFilePath = "C:/Users/legion/Downloads/атака" + vid + ".mp4";
        File video = new File(videoFilePath);
        if (!video.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource videoResource = new FileSystemResource(video);
        ResourceRegion region = resourceRegion(videoResource, headers);

        return ResponseEntity.status(PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(videoResource)
                        .orElse(APPLICATION_OCTET_STREAM))
                .body(new ResourceRegion(videoResource, region.getPosition(), region.getCount()));
    }

    private ResourceRegion resourceRegion(Resource video, HttpHeaders headers) throws IOException {
        long contentLength = video.contentLength();
        List<HttpRange> httpRanges = headers.getRange();
        HttpRange range = httpRanges.stream().findFirst().orElse(null);
        if (range != null) {
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end - start + 1);
            return new ResourceRegion(video, start, rangeLength);
        } else {
            long rangeLength = Math.min(1024 * 1024, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }
    }

}
