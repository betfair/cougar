/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.core.api.mediatype;

import com.betfair.cougar.core.api.mediatype.MediaTypeHeaderProvider;
import junit.framework.Assert;
import junit.framework.TestCase;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Test case for MediaTypeHeaderProvider & MediaTypeUtils classes
 *
 */
public class MediaTypesTest extends TestCase {

	public void testProvider() {
		MediaTypeHeaderProvider hp = new MediaTypeHeaderProvider();
		Assert.assertEquals("*/*", hp.fromString("*").toString());
		Assert.assertEquals("*/*", hp.fromString("   *  ").toString());
		Assert.assertEquals("*/*;q=0.4", hp.fromString("   *  ; q =  0.4").toString());
		Assert.assertEquals("*/*;q=1", hp.fromString("*/* ; q=1").toString());
		Assert.assertEquals("foo/bar", hp.fromString("fOo/BAR").toString());
		Assert.assertEquals("foo/bar", hp.fromString("foo/BAR;").toString());

		Assert.assertEquals("foo/bar;foo=bar", hp.fromString("foo/bar;FOO=bAr").toString());
		Assert.assertEquals("foo/bar;foo=bar", hp.fromString("foo/bar;FOO=bAr;foo=bar").toString());
		Assert.assertEquals("foo/bar;bar=foo;foo=bar", hp.fromString("foo/bar;FOO=bAr;bar=foo;").toString());

		try {hp.fromString("foo"); Assert.fail();} catch (IllegalArgumentException e) {}
		try {hp.fromString("foo/bar;foo"); Assert.fail();} catch (IllegalArgumentException e) {}
	}

	public void testParseMediaTypes() {
		checkMediaTypes(MediaTypeUtils.parseMediaTypes(null), "*/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes(""), "*/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/bar,bar/foo"), "bar/foo", "foo/bar");
	}

	public void testGetMediaTypes() {
		checkMediaTypes(MediaTypeUtils.getMediaTypes(null), "*/*");
		checkMediaTypes(MediaTypeUtils.getMediaTypes(new String[] {}), "*/*");
		checkMediaTypes(MediaTypeUtils.getMediaTypes(new String[] {"foo/bar","bar/foo"}), "bar/foo", "foo/bar");
	}


	public void testIsValid() {
		List<MediaType> open = MediaTypeUtils.getMediaTypes(null);
		Assert.assertTrue(MediaTypeUtils.isValid(open, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertFalse(MediaTypeUtils.isValid(MediaTypeUtils.getMediaTypes(new String[]{"foo/bar"}), MediaType.APPLICATION_JSON_TYPE));
	}

	public void testGetResponseMediaTypeSimple() {
		Assert.assertEquals("image/gif",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("image/gif"),
                        MediaTypeUtils.parseMediaTypes("image/gif"))
                        .toString());
	}
	public void testGetResponseMediaTypePreferSpecific() {
		Assert.assertEquals("image/gif",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("*/*"),
                        MediaTypeUtils.parseMediaTypes("image/gif,text/*;q=2"))
                        .toString());
	}
	public void testGetResponseMediaTypePreferSpecificWildcard() {
		Assert.assertEquals("text/*",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("*/*"),
                        MediaTypeUtils.parseMediaTypes("*/*;q=3,text/*;q=2"))
                        .toString());
	}
	public void testGetResponseMediaTypeAlphabetic() {
		Assert.assertEquals("application/xml",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("*/*"),
                        MediaTypeUtils.parseMediaTypes("text/xml,application/xml,*"))
                        .toString());
	}
	public void testGetResponseMediaTypeDefaultQLow() {
		Assert.assertEquals("text/xml",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("*/*"),
                        MediaTypeUtils.parseMediaTypes("text/xml,application/xml;q=0.99"))
                        .toString());
	}
	public void testGetResponseMediaTypeDefaultQHigh() {
		Assert.assertEquals("text/xml",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("*/*"),
                        MediaTypeUtils.parseMediaTypes("text/xml;q=1.01,application/xml"))
                        .toString());
	}
	public void testGetResponseMediaTypeCheckQ() {
		Assert.assertEquals("application/xml",
                MediaTypeUtils.getResponseMediaType(
                        MediaTypeUtils.parseMediaTypes("application/xml,text/xml,applcation/json"),
                        MediaTypeUtils.parseMediaTypes("application/xml;q=1.5,application/json;q=1.1"))
                        .toString());
	}

	public void testCheckMediaTypeSorting() {
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/bar;q=0.6,foo/bar;q=0.7"), "foo/bar;q=0.7","foo/bar;q=0.6");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/bar;q=0.8,foo/bar;q=0.7"), "foo/bar;q=0.8","foo/bar;q=0.7");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("text/xml, text/html, *"), "text/html","text/xml","*/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("*/*,*/*"), "*/*", "*/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("*/*,foo/*"), "foo/*", "*/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/*,*/*"), "foo/*", "*/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/*,foo/bar"), "foo/bar", "foo/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/bar,foo/*"), "foo/bar", "foo/*");
		checkMediaTypes(MediaTypeUtils.parseMediaTypes("foo/bar,foo/*;q=10"), "foo/bar", "foo/*;q=10");
	}
	private void checkMediaTypes(List<MediaType> mt, String... expected) {
		Assert.assertEquals(expected.length, mt.size());
		for (int i = 0; i < expected.length; ++i) {
			Assert.assertEquals(expected[i], mt.get(i).toString());
		}
	}

}

