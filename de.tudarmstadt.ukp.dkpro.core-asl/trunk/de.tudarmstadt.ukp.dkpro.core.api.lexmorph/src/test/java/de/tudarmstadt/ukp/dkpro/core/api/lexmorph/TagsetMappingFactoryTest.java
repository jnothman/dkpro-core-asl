/*******************************************************************************
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;


public class TagsetMappingFactoryTest
{
	@Test
	public void test()
	{
		Map<String, String> mapping = TagsetMappingFactory.getMapping("tagger", "de", null);
		assertEquals(57, mapping.size());
	}
}
