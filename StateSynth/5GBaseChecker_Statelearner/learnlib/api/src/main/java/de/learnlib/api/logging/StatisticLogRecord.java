/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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

package de.learnlib.api.logging;

import de.learnlib.api.statistic.StatisticData;

import java.util.logging.Level;

/**
 *
 * @author falkhowar
 */
public class StatisticLogRecord extends LearnLogRecord {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final StatisticData data;
    
    public StatisticLogRecord(Level lvl, StatisticData data, Category category) {
        super(lvl, data.getSummary(), category);
        this.data = data;
    }
 
    public StatisticData getData() {
        return data;
    }
    
}
