/*
 * Copyright 2015 POSEIDON Project
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

package org.poseidon_project.context.reasoner;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for holding aggregate rules, and handling temporal literals.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class AggregateRule {

    public final static String[] propSymbols = new String[] { "iff",
            "implies", "or", "and", "not", "(", ")" };

    private String mPropRule;

    //          Literal, Temporal Element
    private Map<String, String> mTemporalLiterals;


    public AggregateRule(String rule) {
        mTemporalLiterals = new HashMap<>();
        rule = checkForTemporalLiterals(rule);
    }

    private String checkForTemporalLiterals(String rule) {
        rule = insertWhitespacesAtBrackets(rule);
        rule = reduceWhiteSpaces(rule);

        while (rule.contains("] ")) {

            int indEnd = rule.indexOf("] ");
            int indStart = rule.substring(0,indEnd).lastIndexOf("[");

            String temporalValue = rule.substring(indStart + 1, indEnd).trim();

            String temp = rule.substring(0, indStart - 1);

            String[] splittedString = temp.split(" ");

            int length = splittedString.length;
            StringBuilder sb = new StringBuilder();

            for (int i= length - 1; i >= 0; i--) {

                String sub = splittedString[i];

                if (sub.equals(propSymbols[0]) || sub.equals(propSymbols[1]) ||
                        sub.equals(propSymbols[2]) || sub.equals(propSymbols[3]) ||
                        sub.equals(propSymbols[4]) || sub.equals(propSymbols[5]) ||
                        sub.equals(propSymbols[6])) {
                    break;

                } else {
                    if (sb.length() > 0) {
                        sb.insert(0, " ");
                    }

                    sb.insert(0, sub);
                }
            }

            mTemporalLiterals.put(sb.toString(), temporalValue);
            rule = removeCharRange(rule, indStart, indEnd);
        }

        return rule;
    }

    private static String removeCharRange(String s, int start, int end) {
        int diff = end - start;
        StringBuilder sb = new StringBuilder(s.length() - diff);
        sb.append(s.substring(0, start - 1)).append(s.substring(end + 1));
        return sb.toString();
    }

    private static String insertWhitespacesAtBrackets(String str) {
        str = str.replaceAll("\\]", " ] ");
        str = str.replaceAll("\\[", " [ ");
        return str;

    }

    public static String reduceWhiteSpaces(String str) {

        if (str.length() < 2)
            return str;
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(str.charAt(0));
        for (int i = 1; i < str.length(); i++) {
            if (!(Character.isWhitespace(str.charAt(i - 1)) && Character
                    .isWhitespace(str.charAt(i)))) {
                strBuf.append(str.charAt(i));
            }
        }
        return strBuf.toString();
    }
}
