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
package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

import gnu.trove.map.hash.TObjectLongHashMap;

/**
 * Inspired by nltk.probability.FreqDist
 * 
 * @author zesch
 */
public class TroveFrequencyDistribution<T> {
    
	private TObjectLongHashMap<T> freqDist;
	
	private long n;
	
	public TroveFrequencyDistribution() {
		freqDist = new TObjectLongHashMap<T>();
		n = 0;
	}
	
	public TroveFrequencyDistribution(Iterable<T> iterable) {
		this();
		for (T o : iterable) {
			addSample(o, 1);
		}
	}
	
	public boolean contains(T o) {
	    return this.freqDist.containsKey(o);
	}
	
	public void inc(T o) {
		addSample(o, 1);
	}
	
	public void incAll(Iterable<T> iterable ) {
		for (T o : iterable) {
			addSample(o, 1);
		}	
	}
	
	/**
	 * @return The total number of sample outcomes that have been recorded by this FreqDist.
	 */
	public long getN() {
		return n;
	}

	/**
	 * @return The total number of sample values (or bins) that have counts greater than zero.
	 */
	public long getB() {
	    return this.freqDist.size();
	}
	
	public long getCount(T o) {
		if (freqDist.containsKey(o)) {
			return freqDist.get(o);
		}
		else {
			return 0;
		}
	}

    public Object[] getKeys() {
	    return freqDist.keys();
	}
	
    public void addSample(T o, long number) {
        this.n = this.n + number;
        if (freqDist.containsKey(o)) {
            freqDist.put(o, freqDist.get(o) + number);
        }
        else {
            freqDist.put(o, number);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Object o : getKeys()) {
            sb.append((T) o.toString());
            sb.append(" - ");
            sb.append(freqDist.get(o));
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}